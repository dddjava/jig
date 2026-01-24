let packageSummaryCache = null;
let packageDiagramNodeIdToFqn = new Map();
let currentPackageDepth = 0;
let currentPackageFilterMode = 'scope';

function readPackageSummaryData() {
    if (packageSummaryCache) return packageSummaryCache;
    const jsonText = document.getElementById('package-data').textContent;
    /** @type {{packages?: Array<{fqn: string, name: string, classCount: number, description: string}>, relations?: Array<{from: string, to: string}>} | Array<{fqn: string, name: string, classCount: number, description: string}>} */
    const packageData = JSON.parse(jsonText);
    packageSummaryCache = {
        packages: Array.isArray(packageData) ? packageData : (packageData.packages ?? []),
        relations: Array.isArray(packageData) ? [] : (packageData.relations ?? []),
    };
    return packageSummaryCache;
}

function packageDepthOf(fqn) {
    if (!fqn || fqn === '(default)') return 0;
    return fqn.split('.').length;
}

function aggregatePackageFqn(fqn, depth) {
    if (!depth || depth <= 0) return fqn;
    if (!fqn || fqn === '(default)') return fqn;
    const parts = fqn.split('.');
    if (parts.length <= depth) return fqn;
    return parts.slice(0, depth).join('.');
}

function writePackageTable() {
    const {packages, relations} = readPackageSummaryData();
    const incomingCounts = new Map();
    const outgoingCounts = new Map();
    relations.forEach(relation => {
        outgoingCounts.set(relation.from, (outgoingCounts.get(relation.from) ?? 0) + 1);
        incomingCounts.set(relation.to, (incomingCounts.get(relation.to) ?? 0) + 1);
    });

    const tbody = document.querySelector('#package-table tbody');
    //tbody.innerHTML = '';

    packages.forEach(item => {
        const tr = document.createElement('tr');

        const fqnTd = document.createElement('td');
        fqnTd.textContent = item.fqn;
        fqnTd.className = 'fqn';
        tr.appendChild(fqnTd);

        const nameTd = document.createElement('td');
        nameTd.textContent = item.name;
        tr.appendChild(nameTd);

        const classCountTd = document.createElement('td');
        classCountTd.textContent = String(item.classCount);
        classCountTd.className = 'number';
        tr.appendChild(classCountTd);

        const incomingCountTd = document.createElement('td');
        incomingCountTd.textContent = String(incomingCounts.get(item.fqn) ?? 0);
        incomingCountTd.className = 'number';
        tr.appendChild(incomingCountTd);

        const outgoingCountTd = document.createElement('td');
        outgoingCountTd.textContent = String(outgoingCounts.get(item.fqn) ?? 0);
        outgoingCountTd.className = 'number';
        tr.appendChild(outgoingCountTd);

        const descTd = document.createElement('td');
        descTd.textContent = item.description;
        descTd.className = 'description hidden markdown';
        tr.appendChild(descTd);

        tbody.appendChild(tr);
    });
}

function writePackageRelationDiagram(filterFqn, mode) {
    const diagram = document.getElementById('package-relation-diagram');
    if (!diagram) return;

    const {packages, relations} = readPackageSummaryData();
    const escapeMermaidText = text => text.replace(/"/g, '\\"');
    const nameByFqn = new Map(packages.map(item => [item.fqn, item.name || item.fqn]));
    const lines = ['graph TD'];
    const aggregatedRoot = filterFqn ? aggregatePackageFqn(filterFqn, currentPackageDepth) : null;
    const scopePrefix = filterFqn ? `${filterFqn}.` : null;
    const withinScope = fqn => !filterFqn || fqn === filterFqn || fqn.startsWith(scopePrefix);
    const visiblePackages = mode === 'scope'
        ? packages.filter(item => withinScope(item.fqn))
        : packages;
    const visibleSet = new Set(visiblePackages.map(item => aggregatePackageFqn(item.fqn, currentPackageDepth)));
    const filteredRelations = relations.filter(relation => {
        if (mode !== 'scope') return true;
        return withinScope(relation.from) && withinScope(relation.to);
    });
    const visibleRelations = filteredRelations
        .map(relation => ({
            from: aggregatePackageFqn(relation.from, currentPackageDepth),
            to: aggregatePackageFqn(relation.to, currentPackageDepth),
        }))
        .filter(relation => relation.from !== relation.to);
    const uniqueRelationMap = new Map();
    visibleRelations.forEach(relation => {
        uniqueRelationMap.set(`${relation.from}::${relation.to}`, relation);
    });
    let uniqueRelations = Array.from(uniqueRelationMap.values());

    if (aggregatedRoot && mode === 'related') {
        const relatedSet = new Set([aggregatedRoot]);
        uniqueRelations.forEach(relation => {
            if (relation.from === aggregatedRoot) relatedSet.add(relation.to);
            if (relation.to === aggregatedRoot) relatedSet.add(relation.from);
        });
        uniqueRelations = uniqueRelations.filter(relation =>
            relation.from === aggregatedRoot || relation.to === aggregatedRoot
        );
        visibleSet.clear();
        relatedSet.forEach(value => visibleSet.add(value));
    } else if (aggregatedRoot && mode === 'scope') {
        const scopedSet = new Set();
        visibleSet.forEach(value => {
            if (value === aggregatedRoot || value.startsWith(`${aggregatedRoot}.`)) {
                scopedSet.add(value);
            }
        });
        visibleSet.clear();
        scopedSet.forEach(value => visibleSet.add(value));
    }
    uniqueRelations.forEach(relation => {
        visibleSet.add(relation.from);
        visibleSet.add(relation.to);
    });

    const nodeIdByFqn = new Map();
    packageDiagramNodeIdToFqn = new Map();
    let nodeIndex = 0;
    const ensureNodeId = fqn => {
        if (nodeIdByFqn.has(fqn)) return nodeIdByFqn.get(fqn);
        const nodeId = `P${nodeIndex++}`;
        nodeIdByFqn.set(fqn, nodeId);
        packageDiagramNodeIdToFqn.set(nodeId, fqn);
        const label = nameByFqn.get(fqn) || fqn;
        lines.push(`${nodeId}["${escapeMermaidText(label)}"]`);
        lines.push(`click ${nodeId} filterPackageDiagram`);
        return nodeId;
    };

    Array.from(visibleSet).sort().forEach(ensureNodeId);
    const relationKey = (from, to) => `${from}::${to}`;
    const canonicalPairKey = (from, to) => (from < to ? `${from}::${to}` : `${to}::${from}`);
    const relationSet = new Set(uniqueRelations.map(relation => relationKey(relation.from, relation.to)));
    const mutualPairs = new Set();
    uniqueRelations.forEach(relation => {
        if (relationSet.has(relationKey(relation.to, relation.from))) {
            mutualPairs.add(canonicalPairKey(relation.from, relation.to));
        }
    });

    const linkStyles = [];
    let linkIndex = 0;
    uniqueRelations.forEach(relation => {
        const fromId = ensureNodeId(relation.from);
        const toId = ensureNodeId(relation.to);
        const pairKey = canonicalPairKey(relation.from, relation.to);
        if (mutualPairs.has(pairKey)) {
            if (relation.from > relation.to) {
                return;
            }
            lines.push(`${fromId} <--> ${toId}`);
            linkStyles.push(`linkStyle ${linkIndex} stroke:red,stroke-width:2px`);
            linkIndex += 1;
            return;
        }
        lines.push(`${fromId} --> ${toId}`);
        linkIndex += 1;
    });
    linkStyles.forEach(styleLine => lines.push(styleLine));

    diagram.removeAttribute('data-processed');
    diagram.textContent = lines.join('\n');
    if (window.mermaid) {
        mermaid.initialize({startOnLoad: false, securityLevel: 'loose'});
        mermaid.run({nodes: [diagram]});
    }
}

function filterPackageDiagramByFqn(fqn) {
    currentPackageFilterMode = 'related';
    writePackageRelationDiagram(fqn, currentPackageFilterMode);
}

window.filterPackageDiagram = function (nodeId) {
    const fqn = packageDiagramNodeIdToFqn.get(nodeId);
    if (!fqn) return;
    filterPackageDiagramByFqn(fqn);
};

function setupPackageFilterInput() {
    const input = document.getElementById('package-filter-input');
    const applyButton = document.getElementById('apply-package-filter');
    const clearButton = document.getElementById('clear-package-filter');
    if (!input || !applyButton || !clearButton) return;

    const applyFilter = () => {
        const value = input.value.trim();
        currentPackageFilterMode = 'scope';
        writePackageRelationDiagram(value || null, currentPackageFilterMode);
    };

    applyButton.addEventListener('click', applyFilter);
    clearButton.addEventListener('click', () => {
        input.value = '';
        currentPackageFilterMode = 'scope';
        writePackageRelationDiagram(null, currentPackageFilterMode);
    });
    input.addEventListener('keydown', event => {
        if (event.key === 'Enter') {
            event.preventDefault();
            applyFilter();
        }
    });
}

function setupPackageDepthControl() {
    const select = document.getElementById('package-depth-select');
    if (!select) return;
    const {packages} = readPackageSummaryData();
    const maxDepth = packages.reduce((max, item) => Math.max(max, packageDepthOf(item.fqn)), 0);

    select.innerHTML = '';
    const noAggregationOption = document.createElement('option');
    noAggregationOption.value = '0';
    noAggregationOption.textContent = '集約なし';
    select.appendChild(noAggregationOption);

    for (let depth = 1; depth <= maxDepth; depth += 1) {
        const option = document.createElement('option');
        option.value = String(depth);
        option.textContent = `深さ${depth}`;
        select.appendChild(option);
    }

    select.value = String(currentPackageDepth);
    select.addEventListener('change', () => {
        currentPackageDepth = Number(select.value);
        const value = document.getElementById('package-filter-input')?.value.trim() || null;
        writePackageRelationDiagram(value, currentPackageFilterMode);
    });
}

document.addEventListener("DOMContentLoaded", function () {
    if (!document.body.classList.contains("package-list")) return;
    document.getElementById("toggle-description-btn").addEventListener("click", toggleDescription);
    setupSortableTables();
    currentPackageFilterMode = 'scope';
    writePackageRelationDiagram(null, currentPackageFilterMode);
    writePackageTable();
    setupPackageFilterInput();
    setupPackageDepthControl();
});
