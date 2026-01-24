let packageSummaryCache = null;
let packageDiagramNodeIdToFqn = new Map();
let currentPackageDepth = 0;
let currentPackageFilterMode = 'scope';
let currentDiagramElement = null;
let pendingDiagramRender = null;
let lastDiagramText = '';
let lastDiagramEdgeCount = 0;
const DEFAULT_MAX_EDGES = 500;

function ensureDiagramErrorBox(diagram) {
    let errorBox = document.getElementById('package-diagram-error');
    if (errorBox) return errorBox;
    errorBox = document.createElement('div');
    errorBox.id = 'package-diagram-error';
    errorBox.setAttribute('role', 'alert');
    errorBox.style.display = 'none';
    errorBox.style.whiteSpace = 'pre-wrap';
    errorBox.style.border = '1px solid #cc3333';
    errorBox.style.background = '#fff5f5';
    errorBox.style.color = '#222222';
    errorBox.style.padding = '8px 12px';
    errorBox.style.margin = '12px 0';

    const message = document.createElement('pre');
    message.id = 'package-diagram-error-message';
    message.style.whiteSpace = 'pre-wrap';
    message.style.margin = '0 0 8px 0';

    const action = document.createElement('button');
    action.id = 'package-diagram-error-action';
    action.type = 'button';
    action.style.display = 'none';
    action.textContent = '描画する';

    errorBox.appendChild(message);
    errorBox.appendChild(action);
    diagram.parentNode.insertBefore(errorBox, diagram);
    return errorBox;
}

function showDiagramError(message, withAction) {
    const diagram = currentDiagramElement;
    if (!diagram) return;
    const errorBox = ensureDiagramErrorBox(diagram);
    const messageNode = document.getElementById('package-diagram-error-message');
    const actionNode = document.getElementById('package-diagram-error-action');
    if (messageNode) messageNode.textContent = message;
    if (actionNode) {
        actionNode.style.display = withAction ? '' : 'none';
        if (withAction) {
            actionNode.onclick = function () {
                if (!pendingDiagramRender) return;
                renderPackageDiagram(pendingDiagramRender.text, pendingDiagramRender.maxEdges);
                pendingDiagramRender = null;
            };
        } else {
            actionNode.onclick = null;
        }
    }
    errorBox.style.display = '';
    diagram.style.display = 'none';
}

function hideDiagramError(diagram) {
    const errorBox = ensureDiagramErrorBox(diagram);
    const messageNode = document.getElementById('package-diagram-error-message');
    const actionNode = document.getElementById('package-diagram-error-action');
    if (messageNode) messageNode.textContent = '';
    if (actionNode) {
        actionNode.style.display = 'none';
        actionNode.onclick = null;
    }
    errorBox.style.display = 'none';
    diagram.style.display = '';
}

function renderPackageDiagram(text, maxEdges) {
    const diagram = currentDiagramElement;
    if (!diagram || !window.mermaid) return;
    hideDiagramError(diagram);
    diagram.removeAttribute('data-processed');
    diagram.textContent = text;
    mermaid.initialize({startOnLoad: false, securityLevel: 'loose', maxEdges: maxEdges});
    mermaid.run({nodes: [diagram]});
}

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

function computeAggregationStats(packages, relations, maxDepth) {
    const stats = new Map();
    for (let depth = 0; depth <= maxDepth; depth += 1) {
        const aggregatedPackages = new Set(packages.map(item => aggregatePackageFqn(item.fqn, depth)));
        const relationKeys = new Set();
        relations.forEach(relation => {
            const from = aggregatePackageFqn(relation.from, depth);
            const to = aggregatePackageFqn(relation.to, depth);
            if (from === to) return;
            relationKeys.add(`${from}::${to}`);
        });
        stats.set(depth, {
            packageCount: aggregatedPackages.size,
            relationCount: relationKeys.size,
        });
    }
    return stats;
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

    const input = document.getElementById('package-filter-input');
    const applyFilter = fqn => {
        if (input) {
            input.value = fqn;
        }
        currentPackageFilterMode = 'scope';
        writePackageRelationDiagram(fqn, currentPackageFilterMode);
    };

    packages.forEach(item => {
        const tr = document.createElement('tr');

        const actionTd = document.createElement('td');
        const actionButton = document.createElement('button');
        actionButton.type = 'button';
        actionButton.className = 'filter-icon';
        actionButton.setAttribute('aria-label', 'このパッケージで絞り込み');
        const actionText = document.createElement('span');
        actionText.className = 'screen-reader-only';
        actionText.textContent = '絞り込み';
        actionButton.appendChild(actionText);
        actionButton.addEventListener('click', () => applyFilter(item.fqn));
        actionTd.appendChild(actionButton);
        tr.appendChild(actionTd);

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

        tbody.appendChild(tr);
    });
}

function writePackageRelationDiagram(filterFqn, mode) {
    const diagram = document.getElementById('package-relation-diagram');
    if (!diagram) return;
    currentDiagramElement = diagram;

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

    lastDiagramText = lines.join('\n');
    lastDiagramEdgeCount = uniqueRelations.length;
    if (lastDiagramEdgeCount > DEFAULT_MAX_EDGES) {
        pendingDiagramRender = {text: lastDiagramText, maxEdges: lastDiagramEdgeCount};
        const message = [
            '関連数が多すぎるため描画を省略しました。',
            `エッジ数: ${lastDiagramEdgeCount}（上限: ${DEFAULT_MAX_EDGES}）`,
            '描画する場合はボタンを押してください。',
        ].join('\n');
        showDiagramError(message, true);
        return;
    }
    diagram.removeAttribute('data-processed');
    diagram.textContent = lastDiagramText;
    if (window.mermaid) {
        if (!mermaid.parseError) {
            mermaid.parseError = function (err, hash) {
                const message = err && err.message ? err.message : String(err);
                const location = hash ? `\nLine: ${hash.line} Column: ${hash.loc}` : '';
                const isEdgeLimit = message.includes('Edge limit exceeded');
                if (isEdgeLimit) {
                    pendingDiagramRender = {text: lastDiagramText, maxEdges: lastDiagramEdgeCount};
                }
                showDiagramError(`Mermaid parse error: ${message}${location}`, isEdgeLimit);
                console.error('Mermaid parse error:', err);
                if (hash) {
                    console.error('Mermaid error location:', hash.line, hash.loc);
                }
            };
        }
        renderPackageDiagram(lastDiagramText, DEFAULT_MAX_EDGES);
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
    const resetButton = document.getElementById('reset-package-controls');
    const depthSelect = document.getElementById('package-depth-select');
    if (!input || !applyButton || !resetButton) return;

    const resetAll = () => {
        input.value = '';
        if (depthSelect) {
            depthSelect.value = '0';
        }
        currentPackageDepth = 0;
        currentPackageFilterMode = 'scope';
        pendingDiagramRender = null;
        writePackageRelationDiagram(null, currentPackageFilterMode);
    };

    const applyFilter = () => {
        const value = input.value.trim();
        currentPackageFilterMode = 'scope';
        writePackageRelationDiagram(value || null, currentPackageFilterMode);
    };

    applyButton.addEventListener('click', applyFilter);
    resetButton.addEventListener('click', resetAll);
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
    const {relations} = readPackageSummaryData();
    const aggregationStats = computeAggregationStats(packages, relations, maxDepth);

    select.innerHTML = '';
    const noAggregationOption = document.createElement('option');
    noAggregationOption.value = '0';
    const noAggregationStats = aggregationStats.get(0);
    noAggregationOption.textContent = `集約なし（P${noAggregationStats.packageCount} / R${noAggregationStats.relationCount}）`;
    select.appendChild(noAggregationOption);

    for (let depth = 1; depth <= maxDepth; depth += 1) {
        const option = document.createElement('option');
        option.value = String(depth);
        const stats = aggregationStats.get(depth);
        if (!stats || stats.relationCount === 0) {
            continue;
        }
        option.textContent = `深さ${depth}（P${stats.packageCount} / R${stats.relationCount}）`;
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
    setupSortableTables();
    currentPackageFilterMode = 'scope';
    writePackageRelationDiagram(null, currentPackageFilterMode);
    writePackageTable();
    setupPackageFilterInput();
    setupPackageDepthControl();
});
