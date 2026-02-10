// 状態/DOMヘルパー
// contextは「UI状態・設定値など長期的に保持する値」に限定する。
// 一時的な中間データはcontextに保存せず、関数内のローカル変数で扱う。
const packageContext = {
    packageSummaryCache: null,
    diagramNodeIdToFqn: new Map(),
    aggregationDepth: 0,
    packageFilterFqn: [],
    relatedCallerFilterMode: '0', // '0':なし, '1':直接, '-1':すべて
    relatedCalleeFilterMode: '0', // '0':なし, '1':直接, '-1':すべて
    relatedFilterFqn: null,
    diagramDirection: 'TD',
    transitiveReductionEnabled: true,
    lastHighlightedNodeId: null, // 追加: 最後にハイライトされたノードのIDを保持
};

const DIAGRAM_CLICK_HANDLER_NAME = 'filterPackageDiagram';
const DEFAULT_MAX_EDGES = 500;

const dom = {
    getRelatedFilterTarget: () => document.getElementById('related-filter-target'),
    setRelatedFilterTargetText: (element, text) => { if (element) element.textContent = text; },

    getPackageTableBody: () => document.querySelector('#package-table tbody'),
    getPackageTableRows: () => document.querySelectorAll('#package-table tbody tr'),
    getPackageFilterInput: () => document.getElementById('package-filter-input'),
    getApplyPackageFilterButton: () => document.getElementById('apply-package-filter'),
    getClearPackageFilterButton: () => document.getElementById('clear-package-filter'),
    getResetPackageFilterButton: () => document.getElementById('reset-package-filter'),
    getDepthSelect: () => document.getElementById('package-depth-select'),
    getRelatedCallerModeSelect: () => document.getElementById('related-caller-mode-select'),
    getRelatedCalleeModeSelect: () => document.getElementById('related-callee-mode-select'),
    getClearRelatedFilterButton: () => document.getElementById('clear-related-filter'),
    getDiagramDirectionRadios: () => document.querySelectorAll('input[name="diagram-direction"]'),
    getDiagramDirectionRadio: () => document.querySelector('input[name="diagram-direction"]'),
    getTransitiveReductionToggle: () => document.getElementById('transitive-reduction-toggle'),
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

// データ取得/整形
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

// 集計
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

function buildAggregationStatsForFilters(packages, relations, packageFilterFqn, relatedFilterFqn, maxDepth, aggregationDepth, relatedCallerFilterMode, relatedCalleeFilterMode) {
    const withinPackageFilter = fqn => {
        if (packageFilterFqn.length === 0) return true;
        return packageFilterFqn.some(filter => {
            const prefix = `${filter}.`;
            return fqn === filter || fqn.startsWith(prefix);
        });
    };
    let filteredPackages = packageFilterFqn.length > 0 ? packages.filter(item => withinPackageFilter(item.fqn)) : packages;
    let filteredRelations = packageFilterFqn.length > 0
        ? relations.filter(relation => withinPackageFilter(relation.from) && withinPackageFilter(relation.to))
        : relations;

            if (relatedFilterFqn) {
                const aggregatedRoot = getAggregatedFqn(relatedFilterFqn, aggregationDepth);
                const relatedSet = collectRelatedSet(aggregatedRoot, filteredRelations, aggregationDepth, relatedCallerFilterMode, relatedCalleeFilterMode);
                filteredPackages = filteredPackages.filter(item =>
                    relatedSet.has(getAggregatedFqn(item.fqn, aggregationDepth))
                );
                // 依存元/依存先のモードに応じてフィルタリングロジックを調整する
                const filterDirectRelation = (relation) => {
                    const from = getAggregatedFqn(relation.from, aggregationDepth);
                    const to = getAggregatedFqn(relation.to, aggregationDepth);
                    const isCaller = relatedCallerFilterMode === '1' && to === aggregatedRoot;
                    const isCallee = relatedCalleeFilterMode === '1' && from === aggregatedRoot;
                    return isCaller || isCallee;
                };
    
                const filterTransitiveRelation = (relation) => {
                    const from = getAggregatedFqn(relation.from, aggregationDepth);
                    const to = getAggregatedFqn(relation.to, aggregationDepth);
                    return relatedSet.has(from) && relatedSet.has(to);
                };
    
                if (relatedCallerFilterMode === '1' || relatedCalleeFilterMode === '1') {
                    filteredRelations = filteredRelations.filter(filterDirectRelation);
                } else if (relatedCallerFilterMode === '-1' || relatedCalleeFilterMode === '-1') {
                    filteredRelations = filteredRelations.filter(filterTransitiveRelation);
                }
            }    return buildAggregationStats(filteredPackages, filteredRelations, maxDepth);
}

// フィルタ/正規化
function normalizePackageFilterValue(value) {
    const trimmed = (value ?? '').trim();
    if (!trimmed) return [];
    return trimmed.split('\n').map(s => s.trim()).filter(s => s);
}

function normalizeAggregationDepthValue(value) {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : 0;
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

function buildPackageRowVisibility(rowFqns, packageFilterFqn) {
    return rowFqns.map(fqn => {
        if (packageFilterFqn.length === 0) return true;
        return packageFilterFqn.some(filter => {
            const prefix = `${filter}.`;
            return fqn === filter || fqn.startsWith(prefix);
        });
    });
}

function buildRelatedRowVisibility(rowFqns, relations, packageFilterFqn, aggregationDepth, relatedCallerFilterMode, relatedCalleeFilterMode, relatedFilterFqn) {
    const withinPackageFilter = rowFqn => {
        if (packageFilterFqn.length === 0) return true;
        return packageFilterFqn.some(filter => {
            const prefix = `${filter}.`;
            return rowFqn === filter || rowFqn.startsWith(prefix);
        });
    };

    if (!relatedFilterFqn) {
        return rowFqns.map(rowFqn => withinPackageFilter(rowFqn));
    }

    const filteredRelations = packageFilterFqn.length > 0
        ? relations.filter(relation =>
            withinPackageFilter(relation.from) && withinPackageFilter(relation.to)
        )
        : relations;
    const aggregatedRoot = getAggregatedFqn(relatedFilterFqn, aggregationDepth);
    const relatedSet = collectRelatedSet(aggregatedRoot, filteredRelations, aggregationDepth, relatedCallerFilterMode, relatedCalleeFilterMode);
    return rowFqns.map(rowFqn => {
        const aggregatedRow = getAggregatedFqn(rowFqn, aggregationDepth);
        return withinPackageFilter(rowFqn) && relatedSet.has(aggregatedRow);
    });
}

function collectRelatedSet(root, relations, aggregationDepth, relatedCallerFilterMode, relatedCalleeFilterMode) {
    if (!root) return new Set();

    const relatedSet = new Set([root]); // 常にルート自身を含める

    // 呼び出し元 (依存元) の関係を収集
    if (relatedCallerFilterMode !== '0') { // 'なし' でない場合
        if (relatedCallerFilterMode === '1') { // '直接' (direct callers)
            relations.forEach(relation => {
                const from = getAggregatedFqn(relation.from, aggregationDepth);
                const to = getAggregatedFqn(relation.to, aggregationDepth);
                if (to === root) relatedSet.add(from);
            });
        } else { // '-1' ('すべて' - all transitive callers)
            const reverseAdjacency = new Map();
            relations.forEach(relation => {
                const from = getAggregatedFqn(relation.from, aggregationDepth);
                const to = getAggregatedFqn(relation.to, aggregationDepth);
                if (!reverseAdjacency.has(to)) reverseAdjacency.set(to, new Set());
                reverseAdjacency.get(to).add(from);
            });

            const queue = [root];
            const visited = new Set([root]);
            while (queue.length > 0) {
                const current = queue.shift();
                const callers = reverseAdjacency.get(current);
                if (callers) {
                    callers.forEach(caller => {
                        if (!visited.has(caller)) {
                            visited.add(caller);
                            relatedSet.add(caller);
                            queue.push(caller);
                        }
                    });
                }
            }
        }
    }

    // 呼び出し先 (依存先) の関係を収集
    if (relatedCalleeFilterMode !== '0') { // 'なし' でない場合
        if (relatedCalleeFilterMode === '1') { // '直接' (direct callees)
            relations.forEach(relation => {
                const from = getAggregatedFqn(relation.from, aggregationDepth);
                const to = getAggregatedFqn(relation.to, aggregationDepth);
                if (from === root) relatedSet.add(to);
            });
        } else { // '-1' ('すべて' - all transitive callees)
            const forwardAdjacency = new Map();
            relations.forEach(relation => {
                const from = getAggregatedFqn(relation.from, aggregationDepth);
                const to = getAggregatedFqn(relation.to, aggregationDepth);
                if (!forwardAdjacency.has(from)) forwardAdjacency.set(from, new Set());
                forwardAdjacency.get(from).add(to);
            });

            const queue = [root];
            const visited = new Set([root]);
            while (queue.length > 0) {
                const current = queue.shift();
                const callees = forwardAdjacency.get(current);
                if (callees) {
                    callees.forEach(callee => {
                        if (!visited.has(callee)) {
                            visited.add(callee);
                            relatedSet.add(callee);
                            queue.push(callee);
                        }
                    });
                }
            }
        }
    }

    return relatedSet;
}

function buildVisibleDiagramRelations(packages, relations, causeRelationEvidence, packageFilterFqn, aggregationDepth, transitiveReductionEnabled) {
    const withinPackageFilter = fqn => {
        if (packageFilterFqn.length === 0) return true;
        return packageFilterFqn.some(filter => {
            const prefix = `${filter}.`;
            return fqn === filter || fqn.startsWith(prefix);
        });
    };
    const visiblePackages = packageFilterFqn.length > 0
        ? packages.filter(item => withinPackageFilter(item.fqn))
        : packages;
    const visibleSet = new Set(visiblePackages.map(item => getAggregatedFqn(item.fqn, aggregationDepth)));
    const filteredRelations = packageFilterFqn.length > 0
        ? relations.filter(relation => withinPackageFilter(relation.from) && withinPackageFilter(relation.to))
        : relations;
    const filteredCauseRelationEvidence = packageFilterFqn.length > 0
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

function filterRelatedDiagramRelations(uniqueRelations, visibleSet, aggregatedRoot, aggregationDepth, relatedCallerFilterMode, relatedCalleeFilterMode) {
    const nextVisibleSet = new Set(visibleSet);
    let nextRelations = uniqueRelations;
    if (aggregatedRoot) {
        const relatedSet = collectRelatedSet(aggregatedRoot, uniqueRelations, aggregationDepth, relatedCallerFilterMode, relatedCalleeFilterMode);
        
        // 依存元/依存先のモードに応じてフィルタリングロジックを調整する
        const filterDirectRelation = (relation) => {
            const from = getAggregatedFqn(relation.from, aggregationDepth);
            const to = getAggregatedFqn(relation.to, aggregationDepth);
            const isCaller = relatedCallerFilterMode === '1' && to === aggregatedRoot;
            const isCallee = relatedCalleeFilterMode === '1' && from === aggregatedRoot;
            return isCaller || isCallee;
        };

        const filterTransitiveRelation = (relation) => {
            const from = getAggregatedFqn(relation.from, aggregationDepth);
            const to = getAggregatedFqn(relation.to, aggregationDepth);
            return relatedSet.has(from) && relatedSet.has(to);
        };

        if (relatedCallerFilterMode === '1' || relatedCalleeFilterMode === '1') {
            nextRelations = uniqueRelations.filter(filterDirectRelation);
        } else if (relatedCallerFilterMode === '-1' || relatedCalleeFilterMode === '-1') {
            nextRelations = uniqueRelations.filter(filterTransitiveRelation);
        } else {
            // モードが '0' (なし) の場合、関連フィルタが適用されないため、
            // 関係はそのまま (relatedSet に含まれるノード間の関係のみ)
            nextRelations = uniqueRelations.filter(relation =>
                relatedSet.has(getAggregatedFqn(relation.from, aggregationDepth)) &&
                relatedSet.has(getAggregatedFqn(relation.to, aggregationDepth))
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

function buildVisibleDiagramElements(packages, relations, causeRelationEvidence, packageFilterFqn, relatedFilterFqn, aggregationDepth, relatedCallerFilterMode, relatedCalleeFilterMode, transitiveReductionEnabled) {
    const base = buildVisibleDiagramRelations(
        packages,
        relations,
        causeRelationEvidence,
        packageFilterFqn,
        aggregationDepth,
        transitiveReductionEnabled
    );
    const aggregatedRoot = relatedFilterFqn ? getAggregatedFqn(relatedFilterFqn, aggregationDepth) : null;
    const {uniqueRelations, visibleSet} = filterRelatedDiagramRelations(
        base.uniqueRelations,
        base.visibleSet,
        aggregatedRoot,
        aggregationDepth,
        relatedCallerFilterMode,
        relatedCalleeFilterMode
    );
    return {
        uniqueRelations,
        visibleSet,
        filteredCauseRelationEvidence: base.filteredCauseRelationEvidence,
    };
}

// テーブル描画
function buildPackageTableRowData(packages, relations) {
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

function buildPackageTableActionSpecs() {
    return {
        filter: {
            ariaLabel: 'このパッケージで絞り込み',
            screenReaderText: '絞り込み',
        },
        related: {
            ariaLabel: '関連のみ表示',
            screenReaderText: '関連のみ表示',
        },
    };
}

function buildPackageTableRowElement(spec, applyFilter, applyRelatedFilterForRow) {
    const tr = document.createElement('tr');
    const actionSpecs = buildPackageTableActionSpecs();

    const actionTd = document.createElement('td');
    const actionButton = document.createElement('button');
    actionButton.type = 'button';
    actionButton.className = 'package-filter-icon';
    actionButton.setAttribute('aria-label', actionSpecs.filter.ariaLabel);
    const actionText = document.createElement('span');
    actionText.className = 'screen-reader-only';
    actionText.textContent = actionSpecs.filter.screenReaderText;
    actionButton.appendChild(actionText);
    actionButton.addEventListener('click', () => applyFilter(spec.fqn));
    actionTd.appendChild(actionButton);
    tr.appendChild(actionTd);

    const relatedTd = document.createElement('td');
    const relatedButton = document.createElement('button');
    relatedButton.type = 'button';
    relatedButton.className = 'related-icon';
    relatedButton.setAttribute('aria-label', actionSpecs.related.ariaLabel);
    const relatedText = document.createElement('span');
    relatedText.className = 'screen-reader-only';
    relatedText.textContent = actionSpecs.related.screenReaderText;
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

    const glossaryTd = document.createElement('td');
    glossaryTd.className = 'glossary-cell';
    const glossaryLink = document.createElement('a');
    glossaryLink.className = 'glossary-link-icon';
    glossaryLink.href = `glossary.html#${encodeURIComponent(spec.fqn)}`;
    glossaryLink.setAttribute('aria-label', '用語集');
    const glossaryText = document.createElement('span');
    glossaryText.className = 'screen-reader-only';
    glossaryText.textContent = '用語集';
    glossaryLink.appendChild(glossaryText);
    glossaryTd.appendChild(glossaryLink);
    tr.appendChild(glossaryTd);

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

function renderPackageTable(context) {
    const {packages, relations} = getPackageSummaryData(context);
    const rows = buildPackageTableRowData(packages, relations);
    const rowSpecs = buildPackageTableRowSpecs(rows);

    const tbody = dom.getPackageTableBody();

    const input = dom.getPackageFilterInput();
    const applyFilter = fqn => {
        if (input) {
            input.value = fqn;
        }
        context.packageFilterFqn = normalizePackageFilterValue(input.value);
        renderDiagramAndTable(context);
        renderRelatedFilterLabel(context);
    };
    const applyRelatedFilterForRow = fqn => {
        setRelatedFilterAndRender(fqn, context);
    };

    rowSpecs.forEach(spec => {
        const tr = buildPackageTableRowElement(spec, applyFilter, applyRelatedFilterForRow);
        tbody.appendChild(tr);
    });
}

function filterRelatedTableRows(fqn, context) {
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
        context.relatedCallerFilterMode,
        context.relatedCalleeFilterMode,
        fqn
    );
    rows.forEach((row, index) => {
        row.classList.toggle('hidden', !visibility[index]);
    });
}

function renderRelatedFilterLabel(context) {
    const target = dom.getRelatedFilterTarget();
    dom.setRelatedFilterTargetText(target, context.relatedFilterFqn ? context.relatedFilterFqn : '未選択');
}

function setRelatedFilterAndRender(fqn, context) {
    context.relatedFilterFqn = fqn;
    renderDiagramAndTable(context);
    renderRelatedFilterLabel(context);
}

function registerDiagramClickHandler(context, applyRelatedFilter = setRelatedFilterAndRender) {
    if (typeof window === 'undefined') return;
    window[DIAGRAM_CLICK_HANDLER_NAME] = function (nodeId) {
        const fqn = context.diagramNodeIdToFqn.get(nodeId);
        if (!fqn) return;
        applyRelatedFilter(fqn, context);
    };
}

function applyDefaultPackageFilterIfPresent(context) {
    const input = dom.getPackageFilterInput();
    if (!input || normalizePackageFilterValue(input.value).length > 0) return false;
    const {packages} = getPackageSummaryData(context);
    const candidate = findDefaultPackageFilterCandidate(packages);
    if (!candidate) return false;
    input.value = candidate;
    context.packageFilterFqn = normalizePackageFilterValue(input.value);
    renderDiagramAndTable(context);
    return true;
}

// 相互依存/依存関係の簡略表示
function buildMutualDependencyItems(mutualPairs, causeRelationEvidence, aggregationDepth) {
    if (!mutualPairs || mutualPairs.size === 0) return [];
    const relationMap = new Map();
    causeRelationEvidence.forEach(relation => {
        const fromPackage = getAggregatedFqn(getPackageFqnFromTypeFqn(relation.from), aggregationDepth);
        const toPackage = getAggregatedFqn(getPackageFqnFromTypeFqn(relation.to), aggregationDepth);
        if (fromPackage === toPackage) return;
        const key = fromPackage < toPackage ? `${fromPackage}::${toPackage}` : `${toPackage}::${fromPackage}`;
        if (!relationMap.has(key)) {
            relationMap.set(key, new Set());
        }
        relationMap.get(key).add(`${relation.from} -> ${relation.to}`);
    });
    return Array.from(mutualPairs).sort().map(key => {
        const parts = key.split('::');
        const pairLabel = `${parts[0]} <-> ${parts[1]}`;
        const causes = relationMap.get(key);
        return {
            pairLabel,
            causes: causes ? Array.from(causes).sort() : [],
        };
    });
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

// ダイアグラム生成
function buildMutualDependencyPairs(relations) {
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

    return {source: lines.join('\n'), nodeIdToFqn, nodeIdByFqn, mutualPairs};
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
    const mutualPairs = buildMutualDependencyPairs(uniqueRelations);
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
        const displayLabel = buildDiagramNodeLabel(nodeLabelById.get(nodeId), fqn, parentSubgraphFqn);
        lines.push(`${nodeId}["${escapeMermaidText(displayLabel)}"]`);
        const tooltip = escapeMermaidText(buildDiagramNodeTooltip(fqn));
        lines.push(`click ${nodeId} ${DIAGRAM_CLICK_HANDLER_NAME} "${tooltip}"`);
        if (fqn && parentFqns.has(fqn)) {
            lines.push(`class ${nodeId} parentPackage`);
        }
    };
    const nodeLines = buildSubgraphLines(rootGroup, addNodeLines, escapeMermaidText);

    return {nodeLines, hasParentStyle: parentFqns.size > 0};
}

function buildDiagramNodeLabel(displayLabel, fqn, parentSubgraphFqn) {
    if (!fqn) return displayLabel ?? '';
    if (displayLabel === fqn && parentSubgraphFqn && fqn.startsWith(`${parentSubgraphFqn}.`)) {
        return fqn.substring(parentSubgraphFqn.length + 1);
    }
    return displayLabel ?? '';
}

function buildDiagramSubgraphLabel(subgraphFqn, parentSubgraphFqn) {
    if (!subgraphFqn) return '';
    if (parentSubgraphFqn && subgraphFqn.startsWith(`${parentSubgraphFqn}.`)) {
        return subgraphFqn.substring(parentSubgraphFqn.length + 1);
    }
    return subgraphFqn;
}

function buildDiagramNodeTooltip(fqn) {
    return fqn ?? '';
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
            const label = buildDiagramSubgraphLabel(child.key, parentSubgraphFqnForNodes);
            lines.push(`subgraph ${groupId}["${escapeMermaidText(label)}"]`);
            renderGroup(child, false, child.key);
            lines.push('end');
        });
    };
    renderGroup(rootGroup, true, rootGroup.key);
    return lines;
}

// ダイアグラム表示/エラー
function getOrCreateDiagramErrorBox(diagram) {
    let errorBox = dom.getDiagramErrorBox();
    if (errorBox) return errorBox;
    return dom.createDiagramErrorBox(diagram);
}

function showDiagramErrorMessage(diagram, message, pendingRender, err, hash) {
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
        const hasAction = Boolean(pendingRender);
        dom.setNodeDisplay(actionNode, hasAction ? '' : 'none');
        if (hasAction) {
            dom.setNodeOnClick(actionNode, function () {
                renderDiagramWithMermaid(diagram, pendingRender.text, pendingRender.maxEdges);
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

function renderDiagramWithMermaid(diagram, text, maxEdges) {
    if (!diagram || !window.mermaid) return;
    hideDiagramErrorMessage(diagram);
    dom.removeDiagramAttribute(diagram, 'data-processed');
    dom.setDiagramContent(diagram, text);
    mermaid.initialize({startOnLoad: false, securityLevel: 'loose', maxEdges: maxEdges});
    mermaid.run({nodes: [diagram]});
}

// 描画/更新
function renderMutualDependencyList(mutualPairs, causeRelationEvidence, aggregationDepth, context) {
    const container = dom.getMutualDependencyList();
    if (!container) return;
    const items = buildMutualDependencyItems(mutualPairs, causeRelationEvidence, aggregationDepth);
    if (items.length === 0) {
        container.style.display = 'none';
        container.innerHTML = '';
        return;
    }

    container.style.display = '';
    const details = document.createElement('details');
    const summary = document.createElement('summary');
    summary.textContent = '相互依存と原因';
    const list = document.createElement('ul');

    const applyFilterAndRender = (fqnsString) => {
        const input = dom.getPackageFilterInput();
        if (input) {
            input.value = fqnsString;
        }
        context.packageFilterFqn = normalizePackageFilterValue(fqnsString);
        renderDiagramAndTable(context);
        renderRelatedFilterLabel(context);
    };

    items.forEach(item => {
        const itemNode = document.createElement('li');
        const pairDiv = document.createElement('div');
        pairDiv.className = 'pair';

        const pairLabelSpan = document.createElement('span');
        pairLabelSpan.textContent = item.pairLabel;
        pairDiv.appendChild(pairLabelSpan);

        const button = document.createElement('button');
        button.type = 'button';
        button.textContent = 'フィルタにセット';
        button.className = 'filter-button';
        const [package1, package2] = item.pairLabel.split(' <-> ');
        button.addEventListener('click', () => applyFilterAndRender(`${package1}\n${package2}`));
        pairDiv.appendChild(button);

        const diagramButton = document.createElement('button');
        diagramButton.type = 'button';
        diagramButton.textContent = '関連図を描画';
        diagramButton.className = 'diagram-button';
        diagramButton.addEventListener('click', () => renderMutualDependencyDiagram(item, container, context));
        pairDiv.appendChild(diagramButton);

        itemNode.appendChild(pairDiv);
        if (item.causes.length > 0) {
            const detailBody = document.createElement('pre');
            detailBody.textContent = item.causes.join('\n');
            itemNode.appendChild(detailBody);
        }
        list.appendChild(itemNode);
    });
    container.innerHTML = '';
    details.appendChild(summary);
    details.appendChild(list);
    const diagramContainer = document.createElement('pre');
    diagramContainer.id = 'mutual-dependency-diagram';
    diagramContainer.className = 'mermaid';
    details.appendChild(diagramContainer);
    container.appendChild(details);
}

function renderMutualDependencyDiagram(item, container, context) {
    const diagram = container.querySelector('#mutual-dependency-diagram');
    if (!diagram) return;

    const {source} = buildMutualDependencyDiagramSource(item.causes, context.diagramDirection);
    if (!source) {
        diagram.innerHTML = ''; // Clear previous diagram
        diagram.style.display = 'none';
        return;
    }

    diagram.style.display = 'block';
    renderDiagramWithMermaid(diagram, source);
}

function buildMutualDependencyDiagramSource(causes, direction) {
    if (causes.length === 0) return {source: null};

    const edges = causes.map(cause => {
        const [from, to] = cause.split(' -> ');
        return {from, to};
    });

    const nodes = new Set();
    edges.forEach(edge => {
        nodes.add(edge.from);
        nodes.add(edge.to);
    });

    const packages = new Map(); // packageFqn -> { nodes: Set<string>, name: string }
    nodes.forEach(node => {
        const packageFqn = node.substring(0, node.lastIndexOf('.'));
        const packageName = packageFqn.substring(packageFqn.lastIndexOf('.') + 1);
        if (!packages.has(packageFqn)) {
            packages.set(packageFqn, {nodes: new Set(), name: packageName});
        }
        packages.get(packageFqn).nodes.add(node);
    });

    const escapeId = id => id.replace(/\./g, '_');
    const escapeLabel = label => `"${label.replace(/"/g, '#quot;')}"`;

    let lines = [`graph ${direction || 'TD'};`];

    let packageIndex = 0;
    for (const [packageFqn, {nodes: packageNodes, name}] of packages.entries()) {
        lines.push(`subgraph P${packageIndex++}[${escapeLabel(name)}]`);
        for (const node of packageNodes) {
            const nodeId = escapeId(node);
            const className = node.substring(node.lastIndexOf('.') + 1);
            lines.push(`${nodeId}[${escapeLabel(className)}]`);
        }
        lines.push('end');
    }

    edges.forEach(({from, to}) => {
        lines.push(`${escapeId(from)} --> ${escapeId(to)}`);
    });

    return {source: lines.join('\n')};
}

function renderPackageDiagram(context, packageFilterFqn, relatedFilterFqn) {
    const diagram = dom.getDiagram();
    if (!diagram) return;

    // 以前のハイライトを解除
    if (context.lastHighlightedNodeId) {
        const oldNodeElement = diagram.querySelector(`[id$="${context.lastHighlightedNodeId}"]`);
        if (oldNodeElement) {
            oldNodeElement.classList.remove('related-filter-highlight');
        }
        context.lastHighlightedNodeId = null;
    }

    const renderPlan = buildDiagramRenderPlan(context, packageFilterFqn, relatedFilterFqn);
    applyDiagramRenderPlan(context, renderPlan);
    if (shouldSkipDiagramRenderByEdgeLimit(diagram, renderPlan, context)) return;
    setDiagramSource(diagram, renderPlan.source);

    // Mermaidでの描画後にハイライトを適用
    renderDiagramWithMermaidIfAvailable(diagram, renderPlan, context);

    if (relatedFilterFqn) {
        // Mermaidのレンダリングは非同期なので、少し遅延させてからハイライトを適用
        setTimeout(() => {
            const nodeId = context.diagramNodeIdByFqn.get(relatedFilterFqn);
            if (nodeId) {
                // MermaidはノードIDをSVG要素のDOM IDの一部として使用する
                // 例: id="flowchart-P0-1" のような形式
                // .nodeセレクタでノードグループを特定し、その中の<title>要素のテキストコンテンツがnodeIdと一致するものを探す
                const mermaidNodes = diagram.querySelectorAll('.node');
                for (const nodeElement of mermaidNodes) {
                    const titleElement = nodeElement.querySelector('title');
                    if (titleElement && titleElement.textContent === nodeId) {
                        nodeElement.classList.add('related-filter-highlight');
                        context.lastHighlightedNodeId = nodeId; // 最後にハイライトしたノードを記憶
                        break;
                    }
                }
            }
        }, 500); // Mermaidのレンダリングを待つための遅延
    }
}

function buildDiagramRenderPlan(context, packageFilterFqn, relatedFilterFqn) {
    const {packages, relations, causeRelationEvidence} = getPackageSummaryData(context);
    const {
        uniqueRelations,
        visibleSet,
        filteredCauseRelationEvidence
    } = buildVisibleDiagramElements(
        packages,
        relations,
        causeRelationEvidence,
        packageFilterFqn,
        relatedFilterFqn,
        context.aggregationDepth,
        context.relatedCallerFilterMode,
        context.relatedCalleeFilterMode,
        context.transitiveReductionEnabled
    );
    const nameByFqn = new Map(packages.map(item => [item.fqn, item.name || item.fqn]));
    const {source, nodeIdToFqn, mutualPairs} = buildMermaidDiagramSource(
        visibleSet,
        uniqueRelations,
        nameByFqn,
        context.diagramDirection
    );
    return {
        source,
        nodeIdToFqn,
        nodeIdByFqn,
        mutualPairs,
        uniqueRelations,
        filteredCauseRelationEvidence,
    };
}

function applyDiagramRenderPlan(context, renderPlan) {
    context.diagramNodeIdToFqn = renderPlan.nodeIdToFqn;
    context.diagramNodeIdByFqn = renderPlan.nodeIdByFqn;
    renderMutualDependencyList(renderPlan.mutualPairs, renderPlan.filteredCauseRelationEvidence, context.aggregationDepth, context);
}

function shouldSkipDiagramRenderByEdgeLimit(diagram, renderPlan, context) {
    const edgeCount = renderPlan.uniqueRelations.length;
    if (edgeCount <= DEFAULT_MAX_EDGES) return false;
    const pendingRender = {text: renderPlan.source, maxEdges: edgeCount};
    const message = [
        '関連数が多すぎるため描画を省略しました。',
        `エッジ数: ${edgeCount}（上限: ${DEFAULT_MAX_EDGES}）`,
        '描画する場合はボタンを押してください。',
    ].join('\n');
    showDiagramErrorMessage(diagram, message, pendingRender, null, null);
    return true;
}

function setDiagramSource(diagram, source) {
    diagram.removeAttribute('data-processed');
    diagram.textContent = source;
}

function renderDiagramWithMermaidIfAvailable(diagram, renderPlan, context) {
    if (!window.mermaid) return;
    ensureMermaidParseErrorHandler(diagram, renderPlan, context);
    renderDiagramWithMermaid(diagram, renderPlan.source, DEFAULT_MAX_EDGES);
}

function ensureMermaidParseErrorHandler(diagram, renderPlan, context) {
    mermaid.parseError = function (err, hash) {
        const message = err && err.message ? err.message : String(err);
        const location = hash ? `\nLine: ${hash.line} Column: ${hash.loc}` : '';
        const isEdgeLimit = message.includes('Edge limit exceeded');
        const pendingRender = isEdgeLimit
            ? {text: renderPlan.source, maxEdges: renderPlan.uniqueRelations.length}
            : null;
        showDiagramErrorMessage(diagram, `Mermaid parse error: ${message}${location}`, pendingRender, err, hash);
    };
}

function renderDiagramAndTable(context) {
    renderPackageDiagram(context, context.packageFilterFqn, context.relatedFilterFqn);
    filterRelatedTableRows(context.relatedFilterFqn, context);
    renderAggregationDepthSelectOptions(getMaxPackageDepth(context), context);
}

// UI配線
function setupPackageFilterControl(context) {
    const input = dom.getPackageFilterInput();
    const applyButton = dom.getApplyPackageFilterButton();
    const clearPackageButton = dom.getClearPackageFilterButton();
    const resetButton = dom.getResetPackageFilterButton(); // 新しいボタンの参照を取得
    if (!input || !applyButton || !clearPackageButton) return; // resetButtonはオプションなのでチェックしない

    // ページの初期ロード時に適用されるデフォルトフィルタを保持
    let initialDefaultFilterValue = '';
    const {packages} = getPackageSummaryData(context);
    const candidate = findDefaultPackageFilterCandidate(packages);
    if (candidate) {
        initialDefaultFilterValue = candidate;
    }

    const applyFilter = () => {
        context.packageFilterFqn = normalizePackageFilterValue(input.value);
        renderDiagramAndTable(context);
        renderRelatedFilterLabel(context);
    };
    const clearPackageFilter = () => {
        input.value = '';
        context.packageFilterFqn = [];
        renderDiagramAndTable(context);
        renderRelatedFilterLabel(context);
    };
    const resetPackageFilter = () => {
        input.value = initialDefaultFilterValue; // 初期デフォルト値でinputを更新
        context.packageFilterFqn = normalizePackageFilterValue(initialDefaultFilterValue); // contextも更新
        renderDiagramAndTable(context);
        renderRelatedFilterLabel(context);
    };

    applyButton.addEventListener('click', applyFilter);
    clearPackageButton.addEventListener('click', clearPackageFilter);
    if (resetButton) { // ボタンが存在する場合のみイベントリスナーを設定
        resetButton.addEventListener('click', resetPackageFilter);
    }
    input.addEventListener('keydown', event => {
        // Enterキーで改行を行うため、デフォルトの動作を妨げない
        // フィルタ適用はボタンクリックのみで行う
    });
}

function setupAggregationDepthControl(context) {
    const select = dom.getDepthSelect();
    if (!select) return;
    const {packages} = getPackageSummaryData(context);
    const maxDepth = packages.reduce((max, item) => Math.max(max, getPackageDepth(item.fqn)), 0);
    renderAggregationDepthSelectOptions(maxDepth, context);
    select.value = String(context.aggregationDepth);
    select.addEventListener('change', () => {
        context.aggregationDepth = normalizeAggregationDepthValue(select.value);
        renderDiagramAndTable(context);
        renderRelatedFilterLabel(context);
        renderAggregationDepthSelectOptions(maxDepth, context);
    });
}

function renderAggregationDepthSelectOptions(maxDepth, context) {
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
        context.relatedCallerFilterMode,
        context.relatedCalleeFilterMode
    );
    const options = buildAggregationDepthOptions(aggregationStats, maxDepth);
    renderAggregationDepthOptionsIntoSelect(select, options, context.aggregationDepth, maxDepth);
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

function renderAggregationDepthOptionsIntoSelect(select, options, aggregationDepth, maxDepth) {
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

function setupRelatedFilterControl(context) {
    const callerSelect = dom.getRelatedCallerModeSelect();
    const calleeSelect = dom.getRelatedCalleeModeSelect();
    const clearButton = dom.getClearRelatedFilterButton();
    if (!callerSelect || !calleeSelect) return;

    callerSelect.value = context.relatedCallerFilterMode;
    calleeSelect.value = context.relatedCalleeFilterMode;

    const applyFilter = () => {
        if (context.relatedFilterFqn) { // フィルタ対象が選択されている場合のみ再描画
            renderDiagramAndTable(context);
        }
    };

    callerSelect.addEventListener('change', () => {
        context.relatedCallerFilterMode = callerSelect.value;
        applyFilter();
    });
    calleeSelect.addEventListener('change', () => {
        context.relatedCalleeFilterMode = calleeSelect.value;
        applyFilter();
    });

    if (clearButton) {
        clearButton.addEventListener('click', () => {
            context.relatedFilterFqn = null;
            context.relatedCallerFilterMode = '0';
            context.relatedCalleeFilterMode = '0';
            callerSelect.value = '0';
            calleeSelect.value = '0';
            context.packageFilterFqn = normalizePackageFilterValue(dom.getPackageFilterInput()?.value);
            renderDiagramAndTable(context);
            renderRelatedFilterLabel(context);
        });
    }
}

function setupDiagramDirectionControl(context) {
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
    const checkbox = dom.getTransitiveReductionToggle();
    if (!checkbox) return;
    checkbox.checked = context.transitiveReductionEnabled;
    checkbox.addEventListener('change', () => {
        context.transitiveReductionEnabled = checkbox.checked;
        renderDiagramAndTable(context);
    });
}

// 初期化
if (typeof document !== 'undefined') {
    document.addEventListener("DOMContentLoaded", function () {
        const body = dom.getDocumentBody();
        if (!body || !body.classList.contains("package-list")) return;
        setupSortableTables();
        renderPackageTable(packageContext);
        setupPackageFilterControl(packageContext);
        setupAggregationDepthControl(packageContext);
        setupRelatedFilterControl(packageContext);
        setupDiagramDirectionControl(packageContext);
        setupTransitiveReductionControl(packageContext);
        registerDiagramClickHandler(packageContext);
        const applied = applyDefaultPackageFilterIfPresent(packageContext);
        if (!applied) {
            renderDiagramAndTable(packageContext);
        }
        renderRelatedFilterLabel(packageContext);
    });
}

// Test-only exports for Node; no-op in browsers.
// テスト用エクスポート
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        // public
        packageContext,
        DIAGRAM_CLICK_HANDLER_NAME,
        dom,

        // private
        getPackageSummaryData,
        parsePackageSummaryData,
        getPackageDepth,
        getMaxPackageDepth,
        getAggregatedFqn,
        getCommonPrefixDepth,
        getPackageFqnFromTypeFqn,
        buildAggregationStats,
        buildAggregationStatsForFilters,
        normalizePackageFilterValue,
        normalizeAggregationDepthValue,
        findDefaultPackageFilterCandidate,
        buildPackageRowVisibility,
        buildRelatedRowVisibility,
        collectRelatedSet,
        buildVisibleDiagramRelations,
        filterRelatedDiagramRelations,
        buildVisibleDiagramElements,
        buildPackageTableRowData,
        buildPackageTableRowSpecs,
        buildPackageTableActionSpecs,
        buildPackageTableRowElement,
        renderPackageTable,
        filterRelatedTableRows,
        renderRelatedFilterLabel,
        setRelatedFilterAndRender,
        applyDefaultPackageFilterIfPresent,
        buildMutualDependencyItems,
        detectStronglyConnectedComponents,
        transitiveReduction,
        buildMutualDependencyPairs,
        buildParentFqns,
        buildMermaidDiagramSource,
        buildDiagramNodeMaps,
        buildDiagramEdgeLines,
        buildDiagramNodeLines,
        buildDiagramNodeLabel,
        buildDiagramSubgraphLabel,
        buildDiagramNodeTooltip,
        buildDiagramGroupTree,
        buildSubgraphLines,
        getOrCreateDiagramErrorBox,
        showDiagramErrorMessage,
        hideDiagramErrorMessage,
        renderDiagramWithMermaid,
        renderMutualDependencyList,
        renderMutualDependencyDiagram,
        buildMutualDependencyDiagramSource,
        renderPackageDiagram,
        renderDiagramAndTable,
        registerDiagramClickHandler,
        setupPackageFilterControl,
        setupAggregationDepthControl,
        renderAggregationDepthSelectOptions,
        buildAggregationDepthOptions,
        renderAggregationDepthOptionsIntoSelect,
        setupRelatedFilterControl,
        setupDiagramDirectionControl,
        setupTransitiveReductionControl,
    };
}
