const packageContext = {
    packageSummaryCache: null,
    diagramNodeIdToFqn: new Map(),
    aggregationDepth: 0,
    diagramElement: null,
    pendingDiagramRender: null,
    lastDiagramSource: '',
    lastDiagramEdgeCount: 0,
    DEFAULT_MAX_EDGES: 500,
    packageFilterFqn: null,
    relatedFilterMode: 'direct',
    relatedFilterFqn: null,
    diagramDirection: 'TD',
    transitiveReductionEnabled: true,
};

const dom = {
    getRelatedFilterTarget: () => document.getElementById('related-filter-target'),
    setRelatedFilterTargetText: (element, text) => { if (element) element.textContent = text; },

    getPackageTableBody: () => document.querySelector('#package-table tbody'),
    getPackageTableRows: () => document.querySelectorAll('#package-table tbody tr'),
    getPackageFilterInput: () => document.getElementById('package-filter-input'),
    getApplyPackageFilterButton: () => document.getElementById('apply-package-filter'),
    getClearPackageFilterButton: () => document.getElementById('clear-package-filter'),
    getDepthSelect: () => document.getElementById('package-depth-select'),
    getRelatedModeSelect: () => document.getElementById('related-mode-select'),
    getClearRelatedFilterButton: () => document.getElementById('clear-related-filter'),
    getDiagramDirectionRadios: () => document.querySelectorAll('input[name="diagram-direction"]'),
    getDiagramDirectionRadio: () => document.querySelector('input[name="diagram-direction"]'),
    getMutualDependencyList: () => document.getElementById('mutual-dependency-list'),
    getDiagram: () => document.getElementById('package-relation-diagram'),
    getDocumentBody: () => document.body,

    getDiagramErrorBox: () => document.getElementById('package-diagram-error'),
    createDiagramErrorBox: (diagram) => {
        let errorBox = document.createElement('div');
        errorBox.id = 'package-diagram-error';
        errorBox.setAttribute('role', 'alert');
        errorBox.style.display = 'none'; // Initially hidden
        errorBox.style.whiteSpace = 'pre-wrap';
        errorBox.style.border = '1px solid #cc3333';
        errorBox.style.background = '#fff5f5';
        errorBox.style.color = '#222222';
        errorBox.style.padding = '8px 12px';
        errorBox.style.margin = '12px 0';
        // Message and action nodes created here too and appended
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
        diagram.parentNode.insertBefore(errorBox, diagram); // Insert into DOM
        return errorBox;
    },
    getDiagramErrorMessageNode: () => document.getElementById('package-diagram-error-message'),
    getDiagramErrorActionNode: () => document.getElementById('package-diagram-error-action'),
    setNodeTextContent: (element, text) => { if (element) element.textContent = text; },
    setNodeDisplay: (element, display) => { if (element) element.style.display = display; },
    setNodeOnClick: (element, handler) => { if (element) element.onclick = handler; },
    setDiagramElementDisplay: (diagram, display) => { if (diagram) diagram.style.display = display; },
    setDiagramContent: (element, content) => { if (element) element.textContent = content; },
    removeDiagramAttribute: (element, attribute) => { if (element) element.removeAttribute(attribute); },
    getPackageDataScript: () => document.getElementById('package-data'),
    getNodeTextContent: (element) => { return element ? element.textContent : ''; },
};

function getOrCreateDiagramErrorBox(diagram) {
    let errorBox = dom.getDiagramErrorBox();
    if (errorBox) return errorBox;
    return dom.createDiagramErrorBox(diagram);
}

function showDiagramErrorMessage(message, withAction, err, hash, context) {
    const diagram = context.diagramElement;
    if (!diagram) return;
    console.error(message);
    if (err) {
        console.error(err);
    }
    if (hash) {
        console.error('Mermaid error location:', hash.line, hash.loc);
    }
    const errorBox = getOrCreateDiagramErrorBox(diagram);
    const messageNode = dom.getDiagramErrorMessageNode();
    const actionNode = dom.getDiagramErrorActionNode();
    dom.setNodeTextContent(messageNode, message);
    if (actionNode) {
        dom.setNodeDisplay(actionNode, withAction ? '' : 'none');
        if (withAction) {
            dom.setNodeOnClick(actionNode, function () {
                if (!context.pendingDiagramRender) return;
                renderDiagramSvg(context.pendingDiagramRender.text, context.pendingDiagramRender.maxEdges, context);
                context.pendingDiagramRender = null;
            });
        } else {
            dom.setNodeOnClick(actionNode, null);
        }
    }
    dom.setNodeDisplay(errorBox, '');
    dom.setDiagramElementDisplay(diagram, 'none');
}

function hideDiagramErrorMessage(diagram) {
    const errorBox = getOrCreateDiagramErrorBox(diagram);
    const messageNode = dom.getDiagramErrorMessageNode();
    const actionNode = dom.getDiagramErrorActionNode();
    dom.setNodeTextContent(messageNode, '');
    dom.setNodeDisplay(actionNode, 'none');
    dom.setNodeOnClick(actionNode, null);
    dom.setNodeDisplay(errorBox, 'none');
    dom.setDiagramElementDisplay(diagram, '');
}

function renderDiagramSvg(text, maxEdges, context) {
    const diagram = context.diagramElement;
    if (!diagram || !window.mermaid) return;
    hideDiagramErrorMessage(diagram);
    dom.removeDiagramAttribute(diagram, 'data-processed');
    dom.setDiagramContent(diagram, text);
    mermaid.initialize({startOnLoad: false, securityLevel: 'loose', maxEdges: maxEdges});
    mermaid.run({nodes: [diagram]});
}

function getPackageSummaryData(context) {
    if (context.packageSummaryCache) return context.packageSummaryCache;
    const jsonText = dom.getNodeTextContent(dom.getPackageDataScript());
    context.packageSummaryCache = parsePackageSummaryData(jsonText);
    return context.packageSummaryCache;
}

function parsePackageSummaryData(jsonText) {
    /** @type {{packages?: Array<{fqn: string, name: string, classCount: number, description: string}>, relations?: Array<{from: string, to: string}>, causeRelationEvidence?: Array<{from: string, to: string}>} | Array<{fqn: string, name: string, classCount: number, description: string}>} */
    const packageData = JSON.parse(jsonText);
    return {
        packages: Array.isArray(packageData) ? packageData : (packageData.packages ?? []),
        relations: Array.isArray(packageData) ? [] : (packageData.relations ?? []),
        causeRelationEvidence: Array.isArray(packageData) ? [] : (packageData.causeRelationEvidence ?? []),
    };
}

function getPackageDepth(fqn) {
    if (!fqn || fqn === '(default)') return 0;
    return fqn.split('.').length;
}

function getMaxPackageDepth(context) {
    const {packages} = getPackageSummaryData(context);
    return packages.reduce((max, item) => Math.max(max, getPackageDepth(item.fqn)), 0);
}

function getAggregatedFqn(fqn, depth) {
    if (!depth || depth <= 0) return fqn;
    if (!fqn || fqn === '(default)') return fqn;
    const parts = fqn.split('.');
    if (parts.length <= depth) return fqn;
    return parts.slice(0, depth).join('.');
}

function getCommonPrefixDepth(fqns) {
    if (!fqns || fqns.length === 0) return 0;
    const firstParts = fqns[0].split('.');
    let depth = firstParts.length;
    for (let i = 1; i < fqns.length; i += 1) {
        const parts = fqns[i].split('.');
        depth = Math.min(depth, parts.length);
        for (let j = 0; j < depth; j += 1) {
            if (parts[j] !== firstParts[j]) {
                depth = j;
                break;
            }
        }
    }
    return depth;
}

function getPackageFqnFromTypeFqn(typeFqn) {
    if (!typeFqn || !typeFqn.includes('.')) return '(default)';
    const parts = typeFqn.split('.');
    return parts.slice(0, parts.length - 1).join('.');
}

function buildAggregationStats(packages, relations, maxDepth) {
    const stats = new Map();
    for (let depth = 0; depth <= maxDepth; depth += 1) {
        const aggregatedPackages = new Set(packages.map(item => getAggregatedFqn(item.fqn, depth)));
        const relationKeys = new Set();
        relations.forEach(relation => {
            const from = getAggregatedFqn(relation.from, depth);
            const to = getAggregatedFqn(relation.to, depth);
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

function buildAggregationStatsForPackageFilter(packages, relations, packageFilterFqn, maxDepth) {
    const filterPrefix = packageFilterFqn ? `${packageFilterFqn}.` : null;
    const withinFilter = fqn => !packageFilterFqn || fqn === packageFilterFqn || fqn.startsWith(filterPrefix);
    const filteredPackages = packages.filter(item => withinFilter(item.fqn));
    const filteredRelations = relations.filter(relation => withinFilter(relation.from) && withinFilter(relation.to));
    return buildAggregationStats(filteredPackages, filteredRelations, maxDepth);
}

function buildAggregationStatsForFilters(packages, relations, packageFilterFqn, relatedFilterFqn, maxDepth, aggregationDepth, relatedFilterMode) {
    const withinPackageFilter = fqn => {
        if (!packageFilterFqn) return true;
        const prefix = `${packageFilterFqn}.`;
        return fqn === packageFilterFqn || fqn.startsWith(prefix);
    };
    let filteredPackages = packageFilterFqn ? packages.filter(item => withinPackageFilter(item.fqn)) : packages;
    let filteredRelations = packageFilterFqn
        ? relations.filter(relation => withinPackageFilter(relation.from) && withinPackageFilter(relation.to))
        : relations;

    if (relatedFilterFqn) {
        const aggregatedRoot = getAggregatedFqn(relatedFilterFqn, aggregationDepth);
        const relatedSet = collectRelatedSet(aggregatedRoot, filteredRelations, aggregationDepth, relatedFilterMode);
        filteredPackages = filteredPackages.filter(item =>
            relatedSet.has(getAggregatedFqn(item.fqn, aggregationDepth))
        );
        if (relatedFilterMode === 'direct') {
            filteredRelations = filteredRelations.filter(relation => {
                const from = getAggregatedFqn(relation.from, aggregationDepth);
                const to = getAggregatedFqn(relation.to, aggregationDepth);
                return from === aggregatedRoot || to === aggregatedRoot;
            });
        } else {
            filteredRelations = filteredRelations.filter(relation => {
                const from = getAggregatedFqn(relation.from, aggregationDepth);
                const to = getAggregatedFqn(relation.to, aggregationDepth);
                return relatedSet.has(from) && relatedSet.has(to);
            });
        }
    }
    return buildAggregationStats(filteredPackages, filteredRelations, maxDepth);
}

function buildAggregationStatsForRelated(packages, relations, rootFqn, maxDepth, aggregationDepth, relatedFilterMode) {
    if (!rootFqn) {
        return buildAggregationStats(packages, relations, maxDepth);
    }
    const aggregatedRoot = getAggregatedFqn(rootFqn, aggregationDepth);
    const relatedSet = collectRelatedSet(aggregatedRoot, relations, aggregationDepth, relatedFilterMode);
    const relatedPackages = packages.filter(item => relatedSet.has(getAggregatedFqn(item.fqn, aggregationDepth)));
    const relatedRelations = relations.filter(relation => {
        const from = getAggregatedFqn(relation.from, aggregationDepth);
        const to = getAggregatedFqn(relation.to, aggregationDepth);
        return relatedSet.has(from) && relatedSet.has(to);
    });
    return buildAggregationStats(relatedPackages, relatedRelations, maxDepth);
}

function renderPackageTable(context) {
    const {packages, relations} = getPackageSummaryData(context);
    const rows = buildPackageTableRows(packages, relations);
    const rowSpecs = buildPackageTableRowSpecs(rows);

    const tbody = dom.getPackageTableBody();

    const input = dom.getPackageFilterInput();
    const applyFilter = fqn => {
        if (input) {
            input.value = fqn;
        }
        context.packageFilterFqn = fqn;
        renderDiagramAndTable(context);
        renderRelatedFilterTarget(context);
    };
    const applyRelatedFilterForRow = fqn => {
        applyRelatedFilter(fqn, context);
    };

    rowSpecs.forEach(spec => {
        const tr = createPackageTableRow(spec, applyFilter, applyRelatedFilterForRow);
        tbody.appendChild(tr);
    });
}

function buildPackageTableRows(packages, relations) {
    const incomingCounts = new Map();
    const outgoingCounts = new Map();
    relations.forEach(relation => {
        outgoingCounts.set(relation.from, (outgoingCounts.get(relation.from) ?? 0) + 1);
        incomingCounts.set(relation.to, (incomingCounts.get(relation.to) ?? 0) + 1);
    });
    return packages.map(item => ({
        ...item,
        incomingCount: incomingCounts.get(item.fqn) ?? 0,
        outgoingCount: outgoingCounts.get(item.fqn) ?? 0,
    }));
}

function buildPackageTableRowSpecs(rows) {
    return rows.map(item => ({
        fqn: item.fqn,
        name: item.name,
        classCount: item.classCount,
        incomingCount: item.incomingCount ?? 0,
        outgoingCount: item.outgoingCount ?? 0,
    }));
}

function createPackageTableRow(spec, applyFilter, applyRelatedFilterForRow) {
    const tr = document.createElement('tr');

    const actionTd = document.createElement('td');
    const actionButton = document.createElement('button');
    actionButton.type = 'button';
    actionButton.className = 'package-filter-icon';
    actionButton.setAttribute('aria-label', 'このパッケージで絞り込み');
    const actionText = document.createElement('span');
    actionText.className = 'screen-reader-only';
    actionText.textContent = '絞り込み';
    actionButton.appendChild(actionText);
    actionButton.addEventListener('click', () => applyFilter(spec.fqn));
    actionTd.appendChild(actionButton);
    tr.appendChild(actionTd);

    const relatedTd = document.createElement('td');
    const relatedButton = document.createElement('button');
    relatedButton.type = 'button';
    relatedButton.className = 'related-icon';
    relatedButton.setAttribute('aria-label', '関連のみ表示');
    const relatedText = document.createElement('span');
    relatedText.className = 'screen-reader-only';
    relatedText.textContent = '関連のみ表示';
    relatedButton.appendChild(relatedText);
    relatedButton.addEventListener('click', () => applyRelatedFilterForRow(spec.fqn));
    relatedTd.appendChild(relatedButton);
    tr.appendChild(relatedTd);

    const fqnTd = document.createElement('td');
    fqnTd.textContent = spec.fqn;
    fqnTd.className = 'fqn';
    tr.appendChild(fqnTd);

    const nameTd = document.createElement('td');
    nameTd.textContent = spec.name;
    tr.appendChild(nameTd);

    const classCountTd = document.createElement('td');
    classCountTd.textContent = String(spec.classCount);
    classCountTd.className = 'number';
    tr.appendChild(classCountTd);

    const incomingCountTd = document.createElement('td');
    incomingCountTd.textContent = String(spec.incomingCount ?? 0);
    incomingCountTd.className = 'number';
    tr.appendChild(incomingCountTd);

    const outgoingCountTd = document.createElement('td');
    outgoingCountTd.textContent = String(spec.outgoingCount ?? 0);
    outgoingCountTd.className = 'number';
    tr.appendChild(outgoingCountTd);

    return tr;
}

function applyPackageFilterToTable(packageFilterFqn) {
    const rows = dom.getPackageTableRows();
    const rowFqns = Array.from(rows, row => {
        const fqnCell = row.querySelector('td.fqn');
        return fqnCell ? fqnCell.textContent : '';
    });
    const visibility = buildPackageRowVisibility(rowFqns, packageFilterFqn);
    rows.forEach((row, index) => {
        row.classList.toggle('hidden', !visibility[index]);
    });
}

function buildPackageRowVisibility(rowFqns, packageFilterFqn) {
    const filterPrefix = packageFilterFqn ? `${packageFilterFqn}.` : null;
    return rowFqns.map(fqn =>
        !packageFilterFqn || fqn === packageFilterFqn || fqn.startsWith(filterPrefix)
    );
}

function applyRelatedFilterToTable(fqn, context) {
    const rows = dom.getPackageTableRows();
    const {relations} = getPackageSummaryData(context);
    const rowFqns = Array.from(rows, row => {
        const fqnCell = row.querySelector('td.fqn');
        return fqnCell ? fqnCell.textContent : '';
    });
    const visibility = buildRelatedRowVisibility(
        rowFqns,
        relations,
        context.packageFilterFqn,
        context.aggregationDepth,
        context.relatedFilterMode,
        fqn
    );
    rows.forEach((row, index) => {
        row.classList.toggle('hidden', !visibility[index]);
    });
}

function buildRelatedRowVisibility(rowFqns, relations, packageFilterFqn, aggregationDepth, relatedFilterMode, relatedFilterFqn) {
    const packageFilterPrefix = packageFilterFqn ? `${packageFilterFqn}.` : null;
    const withinPackageFilter = rowFqn =>
        !packageFilterFqn || rowFqn === packageFilterFqn || rowFqn.startsWith(packageFilterPrefix);

    if (!relatedFilterFqn) {
        return rowFqns.map(rowFqn => withinPackageFilter(rowFqn));
    }

    const filteredRelations = packageFilterFqn
        ? relations.filter(relation =>
            withinPackageFilter(relation.from) && withinPackageFilter(relation.to)
        )
        : relations;
    const aggregatedRoot = getAggregatedFqn(relatedFilterFqn, aggregationDepth);
    const relatedSet = collectRelatedSet(aggregatedRoot, filteredRelations, aggregationDepth, relatedFilterMode);
    return rowFqns.map(rowFqn => {
        const aggregatedRow = getAggregatedFqn(rowFqn, aggregationDepth);
        return withinPackageFilter(rowFqn) && relatedSet.has(aggregatedRow);
    });
}

function renderRelatedFilterTarget(context) {
    const target = dom.getRelatedFilterTarget();
    dom.setRelatedFilterTargetText(target, context.relatedFilterFqn ? context.relatedFilterFqn : '未選択');
}

function collectRelatedSet(root, relations, aggregationDepth, relatedFilterMode) {
    if (!root) return new Set();
    if (relatedFilterMode === 'direct') {
        const relatedSet = new Set([root]);
        relations.forEach(relation => {
            const from = getAggregatedFqn(relation.from, aggregationDepth);
            const to = getAggregatedFqn(relation.to, aggregationDepth);
            if (from === root) relatedSet.add(to);
            if (to === root) relatedSet.add(from);
        });
        return relatedSet;
    }

    const adjacency = new Map();
    const addEdge = (from, to) => {
        if (!adjacency.has(from)) adjacency.set(from, new Set());
        adjacency.get(from).add(to);
    };
    relations.forEach(relation => {
        const from = getAggregatedFqn(relation.from, aggregationDepth);
        const to = getAggregatedFqn(relation.to, aggregationDepth);
        addEdge(from, to);
        if (relatedFilterMode === 'all') {
            addEdge(to, from);
        }
    });

    const relatedSet = new Set([root]);
    const queue = [root];
    while (queue.length) {
        const current = queue.shift();
        const nextSet = adjacency.get(current);
        if (!nextSet) continue;
        nextSet.forEach(next => {
            if (relatedSet.has(next)) return;
            relatedSet.add(next);
            queue.push(next);
        });
    }
    return relatedSet;
}

function renderDiagramAndTable(context) {
    renderPackageDiagram(context, context.packageFilterFqn, context.relatedFilterFqn);
    applyRelatedFilterToTable(context.relatedFilterFqn, context);
    updateAggregationDepthOptions(getMaxPackageDepth(context), context);
}

function renderMutualDependencyList(mutualPairs, causeRelationEvidence, context) {
    const container = dom.getMutualDependencyList();
    if (!container) return;
    if (!mutualPairs || mutualPairs.size === 0) {
        container.style.display = 'none';
        container.innerHTML = '';
        return;
    }
    const relationMap = new Map();
    causeRelationEvidence.forEach(relation => {
        const fromPackage = getAggregatedFqn(getPackageFqnFromTypeFqn(relation.from), context.aggregationDepth);
        const toPackage = getAggregatedFqn(getPackageFqnFromTypeFqn(relation.to), context.aggregationDepth);
        if (fromPackage === toPackage) return;
        const key = fromPackage < toPackage ? `${fromPackage}::${toPackage}` : `${toPackage}::${fromPackage}`;
        if (!relationMap.has(key)) {
            relationMap.set(key, new Set());
        }
        relationMap.get(key).add(`${relation.from} -> ${relation.to}`);
    });

    container.style.display = '';
    const details = document.createElement('details');
    const summary = document.createElement('summary');
    summary.textContent = '相互依存と原因';
    const list = document.createElement('ul');
    Array.from(mutualPairs).sort().forEach(key => {
        const parts = key.split('::');
        const pairLabel = `${parts[0]} <-> ${parts[1]}`;
        const item = document.createElement('li');
        const pair = document.createElement('div');
        pair.className = 'pair';
        pair.textContent = pairLabel;
        item.appendChild(pair);
        const causes = relationMap.get(key);
        if (causes && causes.size > 0) {
            const detailBody = document.createElement('pre');
            detailBody.textContent = Array.from(causes).sort().join('\n');
            item.appendChild(detailBody);
        }
        list.appendChild(item);
    });
    container.innerHTML = '';
    details.appendChild(summary);
    details.appendChild(list);
    container.appendChild(details);
}

function detectStronglyConnectedComponents(graph) {
    const indices = new Map();
    const lowLink = new Map();
    const stack = [];
    const onStack = new Set();
    const result = [];
    const index = {value: 0};

    function strongConnect(node) {
        indices.set(node, index.value);
        lowLink.set(node, index.value);
        index.value++;
        stack.push(node);
        onStack.add(node);

        (graph.get(node) || []).forEach(neighbor => {
            if (!indices.has(neighbor)) {
                strongConnect(neighbor);
                lowLink.set(node, Math.min(lowLink.get(node), lowLink.get(neighbor)));
            } else if (onStack.has(neighbor)) {
                lowLink.set(node, Math.min(lowLink.get(node), indices.get(neighbor)));
            }
        });

        if (lowLink.get(node) === indices.get(node)) {
            const scc = [];
            let current;
            do {
                current = stack.pop();
                onStack.delete(current);
                scc.push(current);
            } while (current !== node);
            result.push(scc);
        }
    }

    for (const node of graph.keys()) {
        if (!indices.has(node)) {
            strongConnect(node);
        }
    }
    return result;
}

function transitiveReduction(relations) {
    const graph = new Map();
    relations.forEach(relation => {
        if (!graph.has(relation.from)) graph.set(relation.from, []);
        graph.get(relation.from).push(relation.to);
    });

    const sccs = detectStronglyConnectedComponents(graph);
    const cyclicNodes = new Set(sccs.filter(scc => scc.length > 1).flat());
    const cyclicEdges = new Set(
        relations
            .filter(edge => cyclicNodes.has(edge.from) && cyclicNodes.has(edge.to))
            .map(edge => `${edge.from}::${edge.to}`)
    );

    const acyclicGraph = new Map();
    relations.forEach(edge => {
        if (cyclicEdges.has(`${edge.from}::${edge.to}`)) return;
        if (!acyclicGraph.has(edge.from)) acyclicGraph.set(edge.from, []);
        acyclicGraph.get(edge.from).push(edge.to);
    });

    function isReachableWithoutDirect(start, end) {
        const visited = new Set();

        function dfs(current, target, skipDirect) {
            if (current === target) return true;
            visited.add(current);
            const neighbors = acyclicGraph.get(current) || [];
            for (const neighbor of neighbors) {
                if (skipDirect && neighbor === target) continue;
                if (visited.has(neighbor)) continue;
                if (dfs(neighbor, target, false)) return true;
            }
            return false;
        }

        return dfs(start, end, true);
    }

    const toRemove = new Set();
    relations.forEach(edge => {
        if (cyclicEdges.has(`${edge.from}::${edge.to}`)) return;
        if (isReachableWithoutDirect(edge.from, edge.to)) {
            toRemove.add(`${edge.from}::${edge.to}`);
        }
    });

    return relations.filter(edge => !toRemove.has(`${edge.from}::${edge.to}`));
}

function buildMutualPairs(relations) {
    const relationKey = (from, to) => `${from}::${to}`;
    const canonicalPairKey = (from, to) => (from < to ? `${from}::${to}` : `${to}::${from}`);
    const relationSet = new Set(relations.map(relation => relationKey(relation.from, relation.to)));
    const mutualPairs = new Set();
    relations.forEach(relation => {
        if (relationSet.has(relationKey(relation.to, relation.from))) {
            mutualPairs.add(canonicalPairKey(relation.from, relation.to));
        }
    });
    return mutualPairs;
}

function buildParentFqns(visibleSet) {
    const parentFqns = new Set();
    Array.from(visibleSet).sort().forEach(fqn => {
        const parts = fqn.split('.');
        for (let i = 1; i < parts.length; i += 1) {
            const prefix = parts.slice(0, i).join('.');
            if (visibleSet.has(prefix)) parentFqns.add(prefix);
        }
    });
    return parentFqns;
}

function buildMermaidDiagramSource(visibleSet, uniqueRelations, nameByFqn, diagramDirection) {
    const escapeMermaidText = text => text.replace(/"/g, '\\"');
    const lines = [`graph ${diagramDirection}`];
    const {nodeIdByFqn, nodeIdToFqn, nodeLabelById, ensureNodeId} = buildDiagramNodeMaps(visibleSet, nameByFqn);
    const {edgeLines, linkStyles, mutualPairs} = buildDiagramEdgeLines(uniqueRelations, ensureNodeId);
    const {nodeLines, hasParentStyle} = buildDiagramNodeLines(
        visibleSet,
        nodeIdByFqn,
        nodeIdToFqn,
        nodeLabelById,
        escapeMermaidText
    );

    nodeLines.forEach(line => lines.push(line));
    if (hasParentStyle) {
        lines.push('classDef parentPackage fill:#ffffde,stroke:#aaaa00,stroke-width:2px');
    }
    edgeLines.forEach(line => lines.push(line));
    linkStyles.forEach(styleLine => lines.push(styleLine));

    return {source: lines.join('\n'), nodeIdToFqn, mutualPairs};
}

function buildDiagramNodeMaps(visibleSet, nameByFqn) {
    const nodeIdByFqn = new Map();
    const nodeIdToFqn = new Map();
    const nodeLabelById = new Map();
    let nodeIndex = 0;
    const ensureNodeId = fqn => {
        if (nodeIdByFqn.has(fqn)) return nodeIdByFqn.get(fqn);
        const nodeId = `P${nodeIndex++}`;
        nodeIdByFqn.set(fqn, nodeId);
        nodeIdToFqn.set(nodeId, fqn);
        const label = nameByFqn.get(fqn) || fqn;
        nodeLabelById.set(nodeId, label);
        return nodeId;
    };
    Array.from(visibleSet).sort().forEach(ensureNodeId);
    return {nodeIdByFqn, nodeIdToFqn, nodeLabelById, ensureNodeId};
}

function buildDiagramEdgeLines(uniqueRelations, ensureNodeId) {
    const mutualPairs = buildMutualPairs(uniqueRelations);
    const linkStyles = [];
    let linkIndex = 0;
    const edgeLines = [];
    uniqueRelations.forEach(relation => {
        const fromId = ensureNodeId(relation.from);
        const toId = ensureNodeId(relation.to);
        const pairKey = relation.from < relation.to
            ? `${relation.from}::${relation.to}`
            : `${relation.to}::${relation.from}`;
        if (mutualPairs.has(pairKey)) {
            if (relation.from > relation.to) {
                return;
            }
            edgeLines.push(`${fromId} <--> ${toId}`);
            linkStyles.push(`linkStyle ${linkIndex} stroke:red,stroke-width:2px`);
            linkIndex += 1;
            return;
        }
        edgeLines.push(`${fromId} --> ${toId}`);
        linkIndex += 1;
    });
    return {edgeLines, linkStyles, mutualPairs};
}

function buildDiagramNodeLines(visibleSet, nodeIdByFqn, nodeIdToFqn, nodeLabelById, escapeMermaidText) {
    const visibleFqns = Array.from(visibleSet).sort();
    const parentFqns = buildParentFqns(visibleSet);
    const rootGroup = buildDiagramGroupTree(visibleFqns, nodeIdByFqn);
    const addNodeLines = (lines, nodeId, parentSubgraphFqn) => {
        const fqn = nodeIdToFqn.get(nodeId);
        let displayLabel = nodeLabelById.get(nodeId);

        if (displayLabel === fqn && parentSubgraphFqn && fqn.startsWith(`${parentSubgraphFqn}.`)) {
            displayLabel = fqn.substring(parentSubgraphFqn.length + 1);
        }
        lines.push(`${nodeId}["${escapeMermaidText(displayLabel)}"]`);
        const tooltip = fqn ? escapeMermaidText(fqn) : '';
        lines.push(`click ${nodeId} filterPackageDiagram "${tooltip}"`);
        if (fqn && parentFqns.has(fqn)) {
        lines.push(`class ${nodeId} parentPackage`);
        }
    };
    const nodeLines = buildSubgraphLines(rootGroup, addNodeLines, escapeMermaidText);

    return {nodeLines, hasParentStyle: parentFqns.size > 0};
}

function buildDiagramGroupTree(visibleFqns, nodeIdByFqn) {
    const prefixDepth = getCommonPrefixDepth(visibleFqns);
    const baseDepth = Math.max(prefixDepth - 1, 0);
    const createGroupNode = key => ({key, children: new Map(), nodes: []});
    const rootGroup = createGroupNode('');
    visibleFqns.forEach(fqn => {
        const parts = fqn.split('.');
        const maxDepth = parts.length;
        let current = rootGroup;
        for (let depth = baseDepth + 1; depth <= maxDepth; depth += 1) {
            const key = parts.slice(0, depth).join('.');
            if (!current.children.has(key)) {
                current.children.set(key, createGroupNode(key));
            }
            current = current.children.get(key);
        }
        current.nodes.push(nodeIdByFqn.get(fqn));
    });
    return rootGroup;
}

function buildSubgraphLines(rootGroup, addNodeLines, escapeMermaidText) {
    const lines = [];
    let groupIndex = 0;
    const renderGroup = (group, isRoot, parentSubgraphFqnForNodes) => {
        group.nodes.forEach(nodeId => addNodeLines(lines, nodeId, parentSubgraphFqnForNodes));
        const childKeys = Array.from(group.children.keys()).sort();
        if (isRoot && group.nodes.length === 0 && childKeys.length === 1) {
            renderGroup(group.children.get(childKeys[0]), false, parentSubgraphFqnForNodes);
            return;
        }
        childKeys.forEach(key => {
            const child = group.children.get(key);
            const childNodeCount = child.nodes.length + child.children.size;
            if (childNodeCount <= 1) {
                renderGroup(child, false, parentSubgraphFqnForNodes);
                return;
            }
            const groupId = `G${groupIndex++}`;
            lines.push(`subgraph ${groupId}["${escapeMermaidText(child.key)}"]`);
            renderGroup(child, false, child.key);
            lines.push('end');
        });
    };
    renderGroup(rootGroup, true, rootGroup.key);
    return lines;
}

function getVisibleDiagramElements(packages, relations, causeRelationEvidence, packageFilterFqn, relatedFilterFqn, aggregationDepth, relatedFilterMode, transitiveReductionEnabled) {
    const base = buildFilteredDiagramRelations(
        packages,
        relations,
        causeRelationEvidence,
        packageFilterFqn,
        aggregationDepth,
        transitiveReductionEnabled
    );
    const aggregatedRoot = relatedFilterFqn ? getAggregatedFqn(relatedFilterFqn, aggregationDepth) : null;
    const {uniqueRelations, visibleSet} = applyRelatedFilterToDiagramRelations(
        base.uniqueRelations,
        base.visibleSet,
        aggregatedRoot,
        aggregationDepth,
        relatedFilterMode
    );
    return {
        uniqueRelations,
        visibleSet,
        filteredCauseRelationEvidence: base.filteredCauseRelationEvidence,
    };
}

function buildFilteredDiagramRelations(packages, relations, causeRelationEvidence, packageFilterFqn, aggregationDepth, transitiveReductionEnabled) {
    const packageFilterPrefix = packageFilterFqn ? `${packageFilterFqn}.` : null;
    const withinPackageFilter = fqn =>
        !packageFilterFqn || fqn === packageFilterFqn || fqn.startsWith(packageFilterPrefix);
    const visiblePackages = packageFilterFqn
        ? packages.filter(item => withinPackageFilter(item.fqn))
        : packages;
    const visibleSet = new Set(visiblePackages.map(item => getAggregatedFqn(item.fqn, aggregationDepth)));
    const filteredRelations = packageFilterFqn
        ? relations.filter(relation => withinPackageFilter(relation.from) && withinPackageFilter(relation.to))
        : relations;
    const filteredCauseRelationEvidence = packageFilterFqn
        ? causeRelationEvidence.filter(relation => {
            const fromPackage = getPackageFqnFromTypeFqn(relation.from);
            const toPackage = getPackageFqnFromTypeFqn(relation.to);
            return withinPackageFilter(fromPackage) && withinPackageFilter(toPackage);
        })
        : causeRelationEvidence;
    const visibleRelations = filteredRelations
        .map(relation => ({
            from: getAggregatedFqn(relation.from, aggregationDepth),
            to: getAggregatedFqn(relation.to, aggregationDepth),
        }))
        .filter(relation => relation.from !== relation.to);
    const uniqueRelationMap = new Map();
    visibleRelations.forEach(relation => {
        uniqueRelationMap.set(`${relation.from}::${relation.to}`, relation);
    });
    let uniqueRelations = Array.from(uniqueRelationMap.values());

    if (transitiveReductionEnabled) {
        uniqueRelations = transitiveReduction(uniqueRelations);
    }

    return {uniqueRelations, visibleSet, filteredCauseRelationEvidence};
}

function applyRelatedFilterToDiagramRelations(uniqueRelations, visibleSet, aggregatedRoot, aggregationDepth, relatedFilterMode) {
    const nextVisibleSet = new Set(visibleSet);
    let nextRelations = uniqueRelations;
    if (aggregatedRoot) {
        const relatedSet = collectRelatedSet(aggregatedRoot, uniqueRelations, aggregationDepth, relatedFilterMode);
        if (relatedFilterMode === 'direct') {
            nextRelations = uniqueRelations.filter(relation =>
                relation.from === aggregatedRoot || relation.to === aggregatedRoot
            );
        } else {
            nextRelations = uniqueRelations.filter(relation =>
                relatedSet.has(relation.from) && relatedSet.has(relation.to)
            );
        }
        nextVisibleSet.clear();
        relatedSet.forEach(value => nextVisibleSet.add(value));
    }
    nextRelations.forEach(relation => {
        nextVisibleSet.add(relation.from);
        nextVisibleSet.add(relation.to);
    });
    return {uniqueRelations: nextRelations, visibleSet: nextVisibleSet};
}

function renderPackageDiagram(context, packageFilterFqn, relatedFilterFqn) {
    const diagram = dom.getDiagram();
    if (!diagram) return;
    context.diagramElement = diagram;

    const {packages, relations, causeRelationEvidence} = getPackageSummaryData(context);

    const {
        uniqueRelations,
        visibleSet,
        filteredCauseRelationEvidence
    } = getVisibleDiagramElements(packages, relations, causeRelationEvidence, packageFilterFqn, relatedFilterFqn, context.aggregationDepth, context.relatedFilterMode, context.transitiveReductionEnabled);

    const nameByFqn = new Map(packages.map(item => [item.fqn, item.name || item.fqn]));
    const {source, nodeIdToFqn, mutualPairs} = buildMermaidDiagramSource(
        visibleSet,
        uniqueRelations,
        nameByFqn,
        context.diagramDirection
    );
    context.diagramNodeIdToFqn = nodeIdToFqn;
    renderMutualDependencyList(mutualPairs, filteredCauseRelationEvidence, context);

    context.lastDiagramSource = source;
    context.lastDiagramEdgeCount = uniqueRelations.length;
    if (context.lastDiagramEdgeCount > context.DEFAULT_MAX_EDGES) {
        context.pendingDiagramRender = {text: context.lastDiagramSource, maxEdges: context.lastDiagramEdgeCount};
        const message = [
            '関連数が多すぎるため描画を省略しました。',
            `エッジ数: ${context.lastDiagramEdgeCount}（上限: ${context.DEFAULT_MAX_EDGES}）`,
            '描画する場合はボタンを押してください。',
        ].join('\n');
        showDiagramErrorMessage(message, true, null, null, context);
        return;
    }
    diagram.removeAttribute('data-processed');
    diagram.textContent = context.lastDiagramSource;
    if (window.mermaid) {
        if (!mermaid.parseError) {
            mermaid.parseError = function (err, hash) {
                const message = err && err.message ? err.message : String(err);
                const location = hash ? `\nLine: ${hash.line} Column: ${hash.loc}` : '';
                const isEdgeLimit = message.includes('Edge limit exceeded');
                if (isEdgeLimit) {
                    context.pendingDiagramRender = {text: context.lastDiagramSource, maxEdges: context.lastDiagramEdgeCount};
                }
                showDiagramErrorMessage(`Mermaid parse error: ${message}${location}`, isEdgeLimit, err, hash, context);
            };
        }
        renderDiagramSvg(context.lastDiagramSource, context.DEFAULT_MAX_EDGES, context);
    }
}

function applyRelatedFilter(fqn, context) {
    context.relatedFilterFqn = fqn;
    renderDiagramAndTable(context);
    renderRelatedFilterTarget(context);
}

if (typeof window !== 'undefined') {
    window.filterPackageDiagram = function (nodeId) {
        const fqn = packageContext.diagramNodeIdToFqn.get(nodeId);
        if (!fqn) return;
        applyRelatedFilter(fqn, packageContext);
    };
}

function setupPackageFilterControls(context) {
    const input = dom.getPackageFilterInput();
    const applyButton = dom.getApplyPackageFilterButton();
    const clearPackageButton = dom.getClearPackageFilterButton();
    if (!input || !applyButton || !clearPackageButton) return;

    const applyFilter = () => {
        context.packageFilterFqn = normalizePackageFilterValue(input.value);
        renderDiagramAndTable(context);
        renderRelatedFilterTarget(context);
    };
    const clearPackageFilter = () => {
        input.value = '';
        context.packageFilterFqn = null;
        renderDiagramAndTable(context);
        renderRelatedFilterTarget(context);
    };

    applyButton.addEventListener('click', applyFilter);
    clearPackageButton.addEventListener('click', clearPackageFilter);
    input.addEventListener('keydown', event => {
        if (event.key === 'Enter') {
            event.preventDefault();
            applyFilter();
        }
    });
}

function setupAggregationDepthControl(context) {
    const select = dom.getDepthSelect();
    if (!select) return;
    const {packages} = getPackageSummaryData(context);
    const maxDepth = packages.reduce((max, item) => Math.max(max, getPackageDepth(item.fqn)), 0);
    updateAggregationDepthOptions(maxDepth, context);
    select.value = String(context.aggregationDepth);
    select.addEventListener('change', () => {
        context.aggregationDepth = normalizeAggregationDepthValue(select.value);
        renderDiagramAndTable(context);
        renderRelatedFilterTarget(context);
        updateAggregationDepthOptions(maxDepth, context);
    });
}

function updateAggregationDepthOptions(maxDepth, context) {
    const select = dom.getDepthSelect();
    if (!select) return;
    const {packages, relations} = getPackageSummaryData(context);
    const aggregationStats = buildAggregationStatsForFilters(
        packages,
        relations,
        context.packageFilterFqn,
        context.relatedFilterFqn,
        maxDepth,
        context.aggregationDepth,
        context.relatedFilterMode
    );
    const options = buildAggregationDepthOptions(aggregationStats, maxDepth);
    renderAggregationDepthOptions(select, options, context.aggregationDepth, maxDepth);
}

function buildAggregationDepthOptions(aggregationStats, maxDepth) {
    const options = [];
    const noAggregationStats = aggregationStats.get(0);
    options.push({
        value: '0',
        text: `集約なし（P${noAggregationStats.packageCount} / R${noAggregationStats.relationCount}）`,
    });
    for (let depth = 1; depth <= maxDepth; depth += 1) {
        const stats = aggregationStats.get(depth);
        if (!stats || stats.relationCount === 0) {
            continue;
        }
        options.push({
            value: String(depth),
            text: `深さ${depth}（P${stats.packageCount} / R${stats.relationCount}）`,
        });
    }
    return options;
}

function renderAggregationDepthOptions(select, options, aggregationDepth, maxDepth) {
    select.innerHTML = '';
    options.forEach(option => {
        const node = document.createElement('option');
        node.value = option.value;
        node.textContent = option.text;
        select.appendChild(node);
    });
    const value = Math.min(aggregationDepth, maxDepth);
    select.value = String(value);
}

function applyDefaultPackageFilterIfPresent(context) {
    const input = dom.getPackageFilterInput();
    if (!input || input.value.trim()) return false;
    const {packages} = getPackageSummaryData(context);
    const candidate = findDefaultPackageFilterCandidate(packages);
    if (!candidate) return false;
    input.value = candidate;
    context.packageFilterFqn = candidate;
    renderDiagramAndTable(context);
    return true;
}

function findDefaultPackageFilterCandidate(packages) {
    const domainRoots = packages
        .map(item => item.fqn)
        .map(fqn => {
            const parts = fqn.split('.');
            const domainIndex = parts.indexOf('domain');
            if (domainIndex === -1) return null;
            return parts.slice(0, domainIndex + 1).join('.');
        })
        .filter(Boolean);
    if (domainRoots.length === 0) return null;
    return domainRoots.reduce((best, current) => {
        const bestDepth = best.split('.').length;
        const currentDepth = current.split('.').length;
        return currentDepth < bestDepth ? current : best;
    });
}

function setupRelatedFilterControls(context) {
    const select = dom.getRelatedModeSelect();
    const clearButton = dom.getClearRelatedFilterButton();
    if (!select) return;
    select.value = context.relatedFilterMode;
    select.addEventListener('change', () => {
        context.relatedFilterMode = select.value;
        if (context.relatedFilterFqn) {
            renderDiagramAndTable(context);
        }
    });
    if (clearButton) {
        clearButton.addEventListener('click', () => {
            context.relatedFilterFqn = null;
            context.packageFilterFqn = normalizePackageFilterValue(dom.getPackageFilterInput()?.value);
            renderDiagramAndTable(context);
            renderRelatedFilterTarget(context);
        });
    }
}

function normalizePackageFilterValue(value) {
    const trimmed = (value ?? '').trim();
    return trimmed ? trimmed : null;
}

function normalizeAggregationDepthValue(value) {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : 0;
}

function setupDiagramDirectionControls(context) {
    const radios = dom.getDiagramDirectionRadios();
    radios.forEach(radio => {
        if (radio.value === context.diagramDirection) {
            radio.checked = true;
        }
        radio.addEventListener('change', () => {
            if (!radio.checked) return;
            context.diagramDirection = radio.value;
            renderDiagramAndTable(context);
        });
    });
}

function setupTransitiveReductionControl(context) {
    const container = dom.getDiagramDirectionRadio()?.parentNode?.parentNode;
    if (!container) return;

    const controlContainer = document.createElement('div');
    const checkbox = document.createElement('input');
    checkbox.type = 'checkbox';
    checkbox.id = 'transitive-reduction-toggle';
    checkbox.checked = context.transitiveReductionEnabled;
    checkbox.addEventListener('change', () => {
        context.transitiveReductionEnabled = checkbox.checked;
        renderDiagramAndTable(context);
    });

    const label = document.createElement('label');
    label.htmlFor = checkbox.id;
    label.textContent = '推移簡約';
    label.style.marginLeft = '4px';

    controlContainer.appendChild(checkbox);
    controlContainer.appendChild(label);
    container.appendChild(controlContainer);
}

if (typeof document !== 'undefined') {
    document.addEventListener("DOMContentLoaded", function () {
        const body = dom.getDocumentBody();
        if (!body || !body.classList.contains("package-list")) return;
        setupSortableTables();
        renderPackageTable(packageContext);
        setupPackageFilterControls(packageContext);
        setupAggregationDepthControl(packageContext);
        setupRelatedFilterControls(packageContext);
        setupDiagramDirectionControls(packageContext);
        setupTransitiveReductionControl(packageContext);
        const applied = applyDefaultPackageFilterIfPresent(packageContext);
        if (!applied) {
            renderDiagramAndTable(packageContext);
        }
        renderRelatedFilterTarget(packageContext);
    });
}

// Test-only exports for Node; no-op in browsers.
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        // public
        packageContext,
        dom,

        // private
        getVisibleDiagramElements,
        buildFilteredDiagramRelations,
        applyRelatedFilterToDiagramRelations,
        getAggregatedFqn,
        collectRelatedSet,
        getPackageSummaryData,
        getPackageDepth,
        getMaxPackageDepth,
        getCommonPrefixDepth,
        buildAggregationStats,
        buildAggregationStatsForFilters,
        buildAggregationStatsForPackageFilter,
        buildAggregationStatsForRelated,
        buildPackageRowVisibility,
        buildRelatedRowVisibility,
        getOrCreateDiagramErrorBox,
        showDiagramErrorMessage,
        hideDiagramErrorMessage,
        renderDiagramSvg,
        parsePackageSummaryData,
        renderPackageTable,
        buildPackageTableRows,
        buildPackageTableRowSpecs,
        createPackageTableRow,
        applyPackageFilterToTable,
        applyRelatedFilterToTable,
        renderRelatedFilterTarget,
        renderDiagramAndTable,
        renderMutualDependencyList,
        renderPackageDiagram,
        buildMermaidDiagramSource,
        buildDiagramNodeMaps,
        buildDiagramEdgeLines,
        buildDiagramNodeLines,
        buildDiagramGroupTree,
        buildSubgraphLines,
        applyRelatedFilter,
        setupPackageFilterControls,
        setupAggregationDepthControl,
        updateAggregationDepthOptions,
        buildAggregationDepthOptions,
        renderAggregationDepthOptions,
        applyDefaultPackageFilterIfPresent,
        findDefaultPackageFilterCandidate,
        normalizePackageFilterValue,
        normalizeAggregationDepthValue,
        setupRelatedFilterControls,
        setupDiagramDirectionControls,
        setupTransitiveReductionControl,
        detectStronglyConnectedComponents,
        transitiveReduction,
    };
}
