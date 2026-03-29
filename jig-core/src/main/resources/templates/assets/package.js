// 状態/DOMヘルパー
// contextは「UI状態・設定値など長期的に保持する値」に限定する。
// 一時的な中間データはcontextに保存せず、関数内のローカル変数で扱う。
const packageContext = {
    packageSummaryCache: null,
    diagramNodeIdToFqn: new Map(),
    aggregationDepth: 0,
    packageFilterFqn: [],
    focusCallerMode: '1', // '0':なし, '1':直接, '-1':すべて
    focusCalleeMode: '1', // '0':なし, '1':直接, '-1':すべて
    focusedPackageFqn: null,
    diagramDirection: 'TD',
    mutualDependencyDiagramDirection: 'LR',
    transitiveReductionEnabled: true,
};

const DIAGRAM_CLICK_HANDLER_NAME = 'filterPackageDiagram';

// package-diagram.js で定義された共通関数への参照
// （globalThis.Jig.packageDiagram 経由でアクセス）
const getAggregatedFqn = (fqn, depth) => globalThis.Jig.packageDiagram.getAggregatedFqn(fqn, depth);
const getPackageFqnFromTypeFqn = (typeFqn) => globalThis.Jig.packageDiagram.getPackageFqnFromTypeFqn(typeFqn);
const isWithinPackageFilters = (fqn, packageFilterFqn) => globalThis.Jig.packageDiagram.isWithinPackageFilters(fqn, packageFilterFqn);
const buildVisibleDiagramRelations = (packages, relations, causeRelationEvidence, options) => 
    globalThis.Jig.packageDiagram.buildVisibleDiagramRelations(packages, relations, causeRelationEvidence, options);

// package.js でのパッケージ図生成: clickHandlerName を固定して呼び出す
const buildMermaidDiagramSource = (packageFqns, uniqueRelations, nameByFqn, diagramDirection, focusedPackageFqn) =>
    globalThis.Jig.packageDiagram.buildMermaidDiagramSource(
        packageFqns, uniqueRelations, nameByFqn,
        {diagramDirection, focusedPackageFqn, clickHandlerName: DIAGRAM_CLICK_HANDLER_NAME}
    );

const dom = {
    getFocusTarget: () => document.getElementById('focus-target'),
    setFocusTargetText: (element, text) => { if (element) element.textContent = text; },

    getPackageTableBody: () => document.querySelector('#package-table tbody'),
    getPackageTableRows: () => document.querySelectorAll('#package-table tbody tr'),
    getPackageFilterInput: () => document.getElementById('package-filter-input'),
    getApplyPackageFilterButton: () => document.getElementById('apply-package-filter'),
    getClearPackageFilterButton: () => document.getElementById('clear-package-filter'),
    getResetPackageFilterButton: () => document.getElementById('reset-package-filter'),
    getDepthSelect: () => document.getElementById('package-depth-select'),
    getDepthUpButton: () => document.getElementById('depth-up-button'),
    getDepthDownButton: () => document.getElementById('depth-down-button'),
    getFocusCallerModeSelect: () => document.getElementById('focus-caller-mode-select'), // IDは変更しない
    getFocusCalleeModeSelect: () => document.getElementById('focus-callee-mode-select'), // IDは変更しない
    getClearFocusButton: () => document.getElementById('clear-focus'),
    getDiagramDirectionRadios: () => document.querySelectorAll('input[name="diagram-direction"]'),
    getDiagramDirectionRadio: () => document.querySelector('input[name="diagram-direction"]'),
    getTransitiveReductionToggle: () => document.getElementById('transitive-reduction-toggle'),
    getMutualDependencyList: () => document.getElementById('mutual-dependency-list'),
    getDiagram: () => document.getElementById('package-relation-diagram'),
    getDocumentBody: () => document.body,
    getPackageDataScript: () => document.getElementById('package-data'),
};

// データ取得/整形
function getPackageSummaryData(context) {
    if (context.packageSummaryCache) return context.packageSummaryCache;
    // Defensive: packageData が存在しない場合でも安全に処理
    const data = globalThis.packageData ?? {};
    context.packageSummaryCache = parsePackageSummaryData(data);
    return context.packageSummaryCache;
}

function parsePackageSummaryData(packageData) {
    // packageData はオブジェクト（JSON文字列ではない）
    // 配列形式と オブジェクト形式の両方に対応
    const isArrayFormat = Array.isArray(packageData);
    return {
        packages: isArrayFormat ? packageData : (packageData?.packages ?? []),
        relations: isArrayFormat ? [] : (packageData?.relations ?? []),
        causeRelationEvidence: globalThis.typeRelationsData?.relations ?? [],
        domainPackageRoots: isArrayFormat ? [] : (packageData?.domainPackageRoots ?? []),
    };
}

function getGlossaryTitle(fqn) {
    return globalThis.Jig.glossary.getTypeTerm(fqn).title
}

/**
 * @param {string} fqn
 * @return {number} 深さ
 */
function getPackageDepth(fqn) {
    if (!fqn || fqn === '(default)') return 0;
    return fqn.split('.').length;
}

function getMaxPackageDepth(context) {
    const {packages} = getPackageSummaryData(context);
    return packages.reduce((max, item) => Math.max(max, getPackageDepth(item.fqn)), 0);
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

function buildAggregationStatsForFilters(packages, relations, packageFilterFqn, focusedPackageFqn, maxDepth, aggregationDepth, focusCallerMode, focusCalleeMode) {
    let filteredPackages = packageFilterFqn.length > 0
        ? packages.filter(item => isWithinPackageFilters(item.fqn, packageFilterFqn))
        : packages;
    let filteredRelations = packageFilterFqn.length > 0
        ? relations.filter(relation => isWithinPackageFilters(relation.from, packageFilterFqn) && isWithinPackageFilters(relation.to, packageFilterFqn))
        : relations;

            if (focusedPackageFqn) {
                const aggregatedRoot = getAggregatedFqn(focusedPackageFqn, aggregationDepth);
                const focusSet = collectFocusSet(aggregatedRoot, filteredRelations, aggregationDepth, focusCallerMode, focusCalleeMode);
                filteredPackages = filteredPackages.filter(item =>
                    focusSet.has(getAggregatedFqn(item.fqn, aggregationDepth))
                );
                // 依存元/依存先のモードに応じてフィルタリングロジックを調整する
                const filterDirectRelation = (relation) => {
                    const from = getAggregatedFqn(relation.from, aggregationDepth);
                    const to = getAggregatedFqn(relation.to, aggregationDepth);
                    const isCaller = focusCallerMode === '1' && to === aggregatedRoot;
                    const isCallee = focusCalleeMode === '1' && from === aggregatedRoot;
                    return isCaller || isCallee;
                };
    
                const filterTransitiveRelation = (relation) => {
                    const from = getAggregatedFqn(relation.from, aggregationDepth);
                    const to = getAggregatedFqn(relation.to, aggregationDepth);
                    return focusSet.has(from) && focusSet.has(to);
                };
    
                if (focusCallerMode === '-1' || focusCalleeMode === '-1') {
                    filteredRelations = filteredRelations.filter(filterTransitiveRelation);
                } else if (focusCallerMode === '1' || focusCalleeMode === '1') {
                    filteredRelations = filteredRelations.filter(filterDirectRelation);
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

function findDefaultPackageFilterCandidate(domainPackageRoots) {
    if (domainPackageRoots?.length) {
        return domainPackageRoots.join('\n');
    }
    return null;
}

function getInitialAggregationDepth(domainPackageRoots) {
    if (!domainPackageRoots?.length) return 0;
    const minDepth = Math.min(...domainPackageRoots.map(fqn => getPackageDepth(fqn)));
    return minDepth + 1;
}

function buildPackageRowVisibility(rowFqns, packageFilterFqn) {
    return rowFqns.map(fqn => isWithinPackageFilters(fqn, packageFilterFqn));
}

function buildFocusRowVisibility(rowFqns, relations, packageFilterFqn, aggregationDepth, focusCallerMode, focusCalleeMode, focusedPackageFqn) {
    if (!focusedPackageFqn) {
        return rowFqns.map(rowFqn => isWithinPackageFilters(rowFqn, packageFilterFqn));
    }

    const filteredRelations = packageFilterFqn.length > 0
        ? relations.filter(relation =>
            isWithinPackageFilters(relation.from, packageFilterFqn) && isWithinPackageFilters(relation.to, packageFilterFqn)
        )
        : relations;
    const aggregatedRoot = getAggregatedFqn(focusedPackageFqn, aggregationDepth);
    const focusSet = collectFocusSet(aggregatedRoot, filteredRelations, aggregationDepth, focusCallerMode, focusCalleeMode);
    return rowFqns.map(rowFqn => {
        const aggregatedRow = getAggregatedFqn(rowFqn, aggregationDepth);
        return isWithinPackageFilters(rowFqn, packageFilterFqn) && focusSet.has(aggregatedRow);
    });
}

// BFS グラフ走査を実行し、隣接ノードをすべて収集
function traverseGraph(root, adjacencyMap) {
    const visited = new Set([root]);
    const queue = [root];
    while (queue.length > 0) {
        const current = queue.shift();
        const neighbors = adjacencyMap.get(current);
        if (neighbors) {
            neighbors.forEach(neighbor => {
                if (!visited.has(neighbor)) {
                    visited.add(neighbor);
                    queue.push(neighbor);
                }
            });
        }
    }
    return visited;
}

// 呼び出し元グラフ（逆方向グラフ）を構築
function buildReverseAdjacency(relations, aggregationDepth) {
    const reverseAdjacency = new Map();
    relations.forEach(relation => {
        const from = getAggregatedFqn(relation.from, aggregationDepth);
        const to = getAggregatedFqn(relation.to, aggregationDepth);
        if (!reverseAdjacency.has(to)) reverseAdjacency.set(to, new Set());
        reverseAdjacency.get(to).add(from);
    });
    return reverseAdjacency;
}

// 呼び出し先グラフを構築
function buildForwardAdjacency(relations, aggregationDepth) {
    const forwardAdjacency = new Map();
    relations.forEach(relation => {
        const from = getAggregatedFqn(relation.from, aggregationDepth);
        const to = getAggregatedFqn(relation.to, aggregationDepth);
        if (!forwardAdjacency.has(from)) forwardAdjacency.set(from, new Set());
        forwardAdjacency.get(from).add(to);
    });
    return forwardAdjacency;
}

function collectFocusSet(root, relations, aggregationDepth, focusCallerMode, focusCalleeMode) {
    if (!root) return new Set();

    const focusSet = new Set([root]); // 常にルート自身を含める

    // 呼び出し元 (依存元) の関係を収集
    if (focusCallerMode !== '0') { // 'なし' でない場合
        if (focusCallerMode === '1') { // '直接' (direct callers)
            relations.forEach(relation => {
                const from = getAggregatedFqn(relation.from, aggregationDepth);
                const to = getAggregatedFqn(relation.to, aggregationDepth);
                if (to === root) focusSet.add(from);
            });
        } else { // '-1' ('すべて' - all transitive callers)
            const reverseAdjacency = buildReverseAdjacency(relations, aggregationDepth);
            const callers = traverseGraph(root, reverseAdjacency);
            callers.forEach(caller => focusSet.add(caller));
        }
    }

    // 呼び出し先 (依存先) の関係を収集
    if (focusCalleeMode !== '0') { // 'なし' でない場合
        if (focusCalleeMode === '1') { // '直接' (direct callees)
            relations.forEach(relation => {
                const from = getAggregatedFqn(relation.from, aggregationDepth);
                const to = getAggregatedFqn(relation.to, aggregationDepth);
                if (from === root) focusSet.add(to);
            });
        } else { // '-1' ('すべて' - all transitive callees)
            const forwardAdjacency = buildForwardAdjacency(relations, aggregationDepth);
            const callees = traverseGraph(root, forwardAdjacency);
            callees.forEach(callee => focusSet.add(callee));
        }
    }

    return focusSet;
}

function filterFocusDiagramRelations(uniqueRelations, packageFqns, aggregatedRoot, aggregationDepth, focusCallerMode, focusCalleeMode) {
    const nextVisibleSet = new Set(packageFqns);
    let nextRelations = uniqueRelations;
    if (aggregatedRoot) {
        const focusSet = collectFocusSet(aggregatedRoot, uniqueRelations, aggregationDepth, focusCallerMode, focusCalleeMode);
        
        // 依存元/依存先のモードに応じてフィルタリングロジックを調整する
        const filterDirectRelation = (relation) => {
            const from = getAggregatedFqn(relation.from, aggregationDepth);
            const to = getAggregatedFqn(relation.to, aggregationDepth);
            const isCaller = focusCallerMode === '1' && to === aggregatedRoot;
            const isCallee = focusCalleeMode === '1' && from === aggregatedRoot;
            return isCaller || isCallee;
        };

        const filterTransitiveRelation = (relation) => {
            const from = getAggregatedFqn(relation.from, aggregationDepth);
            const to = getAggregatedFqn(relation.to, aggregationDepth);
            return focusSet.has(from) && focusSet.has(to);
        };

        if (focusCallerMode === '-1' || focusCalleeMode === '-1') {
            nextRelations = uniqueRelations.filter(filterTransitiveRelation);
        } else if (focusCallerMode === '1' || focusCalleeMode === '1') {
            nextRelations = uniqueRelations.filter(filterDirectRelation);
        } else {
            // モードが '0' (なし) の場合、関連フィルタが適用されないため、
            // 関係はそのまま (focusSet に含まれるノード間の関係のみ)
            nextRelations = uniqueRelations.filter(relation =>
                focusSet.has(getAggregatedFqn(relation.from, aggregationDepth)) &&
                focusSet.has(getAggregatedFqn(relation.to, aggregationDepth))
            );
        }

        nextVisibleSet.clear();
        focusSet.forEach(value => nextVisibleSet.add(value));
    }
    nextRelations.forEach(relation => {
        nextVisibleSet.add(relation.from);
        nextVisibleSet.add(relation.to);
    });
    return {uniqueRelations: nextRelations, packageFqns: nextVisibleSet};
}

function buildVisibleDiagramElements(packages, relations, causeRelationEvidence, packageFilterFqn, focusedPackageFqn, aggregationDepth, focusCallerMode, focusCalleeMode, transitiveReductionEnabled) {
    const base = buildVisibleDiagramRelations(
        packages,
        relations,
        causeRelationEvidence,
        {packageFilterFqn, aggregationDepth, transitiveReductionEnabled}
    );
    const aggregatedRoot = focusedPackageFqn ? getAggregatedFqn(focusedPackageFqn, aggregationDepth) : null;
    const {uniqueRelations, packageFqns} = filterFocusDiagramRelations(
        base.uniqueRelations,
        base.packageFqns,
        aggregatedRoot,
        aggregationDepth,
        focusCallerMode,
        focusCalleeMode
    );
    return {
        uniqueRelations,
        packageFqns,
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
        name: getGlossaryTitle(item.fqn),
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
        focus: {
            ariaLabel: 'フォーカス',
            screenReaderText: 'フォーカス',
        },
    };
}

function buildPackageTableRowElement(spec, applyFilter, applyFocusForRow) {
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

    const focusTd = document.createElement('td');
    const focusButton = document.createElement('button');
    focusButton.type = 'button';
    focusButton.className = 'focus-icon';
    focusButton.setAttribute('aria-label', actionSpecs.focus.ariaLabel);
    const focusText = document.createElement('span');
    focusText.className = 'screen-reader-only';
    focusText.textContent = actionSpecs.focus.screenReaderText;
    focusButton.appendChild(focusText);
    focusButton.addEventListener('click', () => applyFocusForRow(spec.fqn));
    focusTd.appendChild(focusButton);
    tr.appendChild(focusTd);

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
        renderFocusLabel(context);
    };
    const applyFocusForRow = fqn => {
        setFocusAndRender(fqn, context);
    };

    rowSpecs.forEach(spec => {
        const tr = buildPackageTableRowElement(spec, applyFilter, applyFocusForRow);
        tbody.appendChild(tr);
    });
}

function filterFocusTableRows(fqn, context) {
    const rows = dom.getPackageTableRows();
    const {relations} = getPackageSummaryData(context);
    const rowFqns = Array.from(rows, row => {
        const fqnCell = row.querySelector('td.fqn');
        return fqnCell ? fqnCell.textContent : '';
    });
    const visibility = buildFocusRowVisibility(
        rowFqns,
        relations,
        context.packageFilterFqn,
        context.aggregationDepth,
        context.focusCallerMode,
        context.focusCalleeMode,
        fqn
    );
    rows.forEach((row, index) => {
        row.classList.toggle('hidden', !visibility[index]);
    });
}

function renderFocusLabel(context) {
    const target = dom.getFocusTarget();
    dom.setFocusTargetText(target, context.focusedPackageFqn ? context.focusedPackageFqn : '未選択');
}

function setFocusAndRender(fqn, context) {
    context.focusedPackageFqn = fqn;
    renderDiagramAndTable(context);
    renderFocusLabel(context);
}

function registerDiagramClickHandler(context, applyFocus = setFocusAndRender) {
    if (typeof window === 'undefined') return;
    window[DIAGRAM_CLICK_HANDLER_NAME] = function (nodeId) {
        const fqn = context.diagramNodeIdToFqn.get(nodeId);
        if (!fqn) return;
        applyFocus(fqn, context);
    };
}

function applyDefaultPackageFilterIfPresent(context) {
    const input = dom.getPackageFilterInput();
    if (!input || normalizePackageFilterValue(input.value).length) return false;
    const {domainPackageRoots} = getPackageSummaryData(context);
    const candidate = findDefaultPackageFilterCandidate(domainPackageRoots);
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

// ダイアグラム生成
// 描画/更新
function renderMutualDependencyList(mutualPairs, causeRelationEvidence, aggregationDepth, context) {
    const container = dom.getMutualDependencyList();
    if (!container) return;
    const items = buildMutualDependencyItems(mutualPairs, causeRelationEvidence, aggregationDepth);
    if (!items?.length) {
        container.style.display = 'none';
        container.innerHTML = '';
        return;
    }

    container.style.display = '';
    const details = document.createElement('details');
    const summary = document.createElement('summary');
    summary.textContent = '相互依存と原因';
    const list = document.createElement('ul');

    const settingsRow = document.createElement('div');
    settingsRow.className = 'control-row';
    const settingsLabel = document.createElement('span');
    settingsLabel.className = 'control-label';
    settingsLabel.textContent = '図の向き:';
    settingsRow.appendChild(settingsLabel);

    ['TD', 'LR'].forEach(direction => {
        const label = document.createElement('label');
        label.className = 'radio-label';
        const radio = document.createElement('input');
        radio.type = 'radio';
        radio.name = 'mutual-dependency-diagram-direction';
        radio.value = direction;
        radio.checked = context.mutualDependencyDiagramDirection === direction;
        radio.addEventListener('change', () => {
            if (radio.checked) {
                context.mutualDependencyDiagramDirection = direction;
                // 表示されている図をすべて更新
                const itemsWithDiagram = Array.from(list.querySelectorAll('li')).filter(li => {
                    const diag = li.querySelector('.mutual-dependency-diagram');
                    return diag && diag.style.display !== 'none';
                });
                itemsWithDiagram.forEach(li => {
                    const itemLabel = li.querySelector('.pair span').textContent;
                    const item = items.find(i => i.pairLabel === itemLabel);
                    if (item) {
                        renderMutualDependencyDiagram(item, li, context);
                    }
                });
            }
        });
        label.appendChild(radio);
        label.appendChild(document.createTextNode(direction === 'TD' ? ' 縦' : ' 横'));
        settingsRow.appendChild(label);
    });

    const applyFilterAndRender = (fqnsString) => {
        const input = dom.getPackageFilterInput();
        if (input) {
            input.value = fqnsString;
        }
        context.packageFilterFqn = normalizePackageFilterValue(fqnsString);
        renderDiagramAndTable(context);
        renderFocusLabel(context);
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

        itemNode.appendChild(pairDiv);

        if (item.causes?.length) {
            const detailBody = document.createElement('pre');
            detailBody.className = 'causes';
            detailBody.textContent = item.causes.join('\n');
            itemNode.appendChild(detailBody);
        }

        const diagramButton = document.createElement('button');
        diagramButton.type = 'button';
        diagramButton.textContent = '関連図を描画';
        diagramButton.className = 'diagram-button';
        diagramButton.addEventListener('click', () => renderMutualDependencyDiagram(item, itemNode, context));
        itemNode.appendChild(diagramButton);

        const diagramContainer = document.createElement('pre');
        diagramContainer.className = 'mermaid mutual-dependency-diagram';
        diagramContainer.style.display = 'none';
        itemNode.appendChild(diagramContainer);

        list.appendChild(itemNode);
    });
    container.innerHTML = '';
    details.appendChild(summary);
    details.appendChild(settingsRow);
    details.appendChild(list);
    container.appendChild(details);
}

function renderMutualDependencyDiagram(item, itemNode, context) {
    const diagram = itemNode.querySelector('.mutual-dependency-diagram');
    if (!diagram) return;

    const {source} = buildMutualDependencyDiagramSource(item.causes, context.mutualDependencyDiagramDirection, item.pairLabel);
    if (!source) {
        diagram.innerHTML = ''; // Clear previous diagram
        diagram.style.display = 'none';
        return;
    }

    const button = itemNode.querySelector('.diagram-button');
    if (button) {
        button.style.display = 'none';
    }

    diagram.style.display = 'block';
    if (!globalThis.Jig || !globalThis.Jig.mermaid || typeof globalThis.Jig.mermaid.renderWithControls !== 'function') return;
    globalThis.Jig.mermaid.renderWithControls(diagram, source);
}

function buildMutualDependencyDiagramSource(causes, direction, mutualPairLabel) {
    if (!causes?.length) return {source: null};

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
        const packageFqn = getPackageFqnFromTypeFqn(node);
        const packageName = packageFqn === '(default)'
            ? '(default)'
            : globalThis.Jig.glossary.typeSimpleName(packageFqn);
        if (!packages.has(packageFqn)) {
            packages.set(packageFqn, {nodes: new Set(), name: packageName});
        }
        packages.get(packageFqn).nodes.add(node);
    });

    const escapeId = id => id.replace(/\./g, '_');
    const escapeLabel = label => `"${label.replace(/"/g, '#quot;')}"`;
    const pairPackages = typeof mutualPairLabel === 'string'
        ? mutualPairLabel.split(' <-> ').map(value => value.trim()).filter(value => value)
        : [];
    const uniquePairPackages = [];
    pairPackages.forEach(packageFqn => {
        if (!uniquePairPackages.includes(packageFqn)) uniquePairPackages.push(packageFqn);
    });
    const collapsedPairPackages = new Set(
        uniquePairPackages.filter(packageFqn =>
            uniquePairPackages.some(other => other !== packageFqn && packageFqn.startsWith(`${other}.`))
        )
    );
    const outerRoots = uniquePairPackages
        .filter(packageFqn => packageFqn && packageFqn !== '(default)' && !collapsedPairPackages.has(packageFqn))
        .slice(0, 2);

    const packageRelations = edges.map(({from, to}) => ({
        from: getPackageFqnFromTypeFqn(from),
        to: getPackageFqnFromTypeFqn(to),
    }));
    const packageAdjacency = new Map();
    const ensureAdjacent = packageFqn => {
        if (!packageAdjacency.has(packageFqn)) packageAdjacency.set(packageFqn, new Set());
        return packageAdjacency.get(packageFqn);
    };
    packageRelations.forEach(({from, to}) => {
        if (!from || !to || from === to) return;
        ensureAdjacent(from).add(to);
        ensureAdjacent(to).add(from);
    });
    const shortestDistance = (start, goal) => {
        if (!start || !goal) return Number.POSITIVE_INFINITY;
        if (start === goal) return 0;
        const queue = [{node: start, distance: 0}];
        const visited = new Set([start]);
        while (queue.length > 0) {
            const current = queue.shift();
            const adjacent = packageAdjacency.get(current.node);
            if (!adjacent) continue;
            for (const next of adjacent) {
                if (visited.has(next)) continue;
                if (next === goal) return current.distance + 1;
                visited.add(next);
                queue.push({node: next, distance: current.distance + 1});
            }
        }
        return Number.POSITIVE_INFINITY;
    };
    const chooseOuterRoot = packageFqn => {
        if (!outerRoots?.length) return null;
        const directMatches = outerRoots.filter(root => packageFqn === root || packageFqn.startsWith(`${root}.`));
        if (directMatches.length === 1) return directMatches[0];
        if (directMatches.length > 1) {
            return directMatches.reduce((best, current) => current.length > best.length ? current : best, directMatches[0]);
        }

        let bestRoot = outerRoots[0];
        let bestDepth = -1;
        let tiedRoots = [];
        outerRoots.forEach(root => {
            const depth = getCommonPrefixDepth([packageFqn, root]);
            if (depth > bestDepth) {
                bestDepth = depth;
                bestRoot = root;
                tiedRoots = [root];
            } else if (depth === bestDepth) {
                tiedRoots.push(root);
            }
        });
        if (tiedRoots.length <= 1) return bestRoot;

        let bestDistance = Number.POSITIVE_INFINITY;
        let nearestRoot = tiedRoots[0];
        tiedRoots.forEach(root => {
            const distance = shortestDistance(packageFqn, root);
            if (distance < bestDistance) {
                bestDistance = distance;
                nearestRoot = root;
            }
        });
        return nearestRoot;
    };
    const appendClassNodes = (targetLines, classNodes) => {
        classNodes.forEach(classFqn => {
            const nodeId = escapeId(classFqn);
            const className = globalThis.Jig.glossary.typeSimpleName(classFqn);
            targetLines.push(globalThis.Jig.mermaid.getNodeDefinition(nodeId, className, 'class'));
        });
    };
    const createTreeNode = () => ({classes: new Set(), children: new Map()});
    const appendTreePackage = (treeRoot, relativePath, classNodes) => {
        let current = treeRoot;
        relativePath.forEach(segment => {
            if (!current.children.has(segment)) current.children.set(segment, createTreeNode());
            current = current.children.get(segment);
        });
        classNodes.forEach(classFqn => current.classes.add(classFqn));
    };
    const renderTreeNode = (targetLines, label, node, counter) => {
        targetLines.push(`subgraph P${counter.value++}[${escapeLabel(label)}]`);
        appendClassNodes(targetLines, node.classes);
        Array.from(node.children.entries()).sort((a, b) => a[0].localeCompare(b[0])).forEach(([segment, child]) => {
            renderTreeNode(targetLines, segment, child, counter);
        });
        targetLines.push('end');
    };

    let lines = [`graph ${direction || 'TD'};`];

    if (outerRoots.length >= 1) {
        const groups = new Map(outerRoots.map(root => [root, []]));
        for (const packageEntry of packages.entries()) {
            const packageFqn = packageEntry[0];
            const selectedRoot = chooseOuterRoot(packageFqn) || outerRoots[0];
            groups.get(selectedRoot).push(packageEntry);
        }
        const subgraphCounter = {value: 0};
        outerRoots.forEach((root, outerIndex) => {
            const rootLabel = globalThis.Jig.glossary.typeSimpleName(root);
            lines.push(`subgraph O${outerIndex}[${escapeLabel(rootLabel || root)}]`);
            const groupedPackages = groups.get(root) || [];
            const treeRoot = createTreeNode();
            const outerDirectClasses = new Set();
            groupedPackages.forEach(([packageFqn, {nodes: classNodes, name}]) => {
                if (packageFqn === root) {
                    classNodes.forEach(classFqn => outerDirectClasses.add(classFqn));
                    return;
                }
                if (!packageFqn.startsWith(`${root}.`)) {
                    lines.push(`subgraph X${subgraphCounter.value++}[${escapeLabel(name)}]`);
                    appendClassNodes(lines, classNodes);
                    lines.push('end');
                    return;
                }
                const relativePath = packageFqn.substring(root.length + 1).split('.').filter(Boolean);
                appendTreePackage(treeRoot, relativePath, classNodes);
            });
            appendClassNodes(lines, outerDirectClasses);
            Array.from(treeRoot.children.entries()).sort((a, b) => a[0].localeCompare(b[0])).forEach(([segment, child]) => {
                renderTreeNode(lines, segment, child, subgraphCounter);
            });
            lines.push('end');
        });
    } else {
        let packageIndex = 0;
        for (const [, {nodes: packageNodes, name}] of packages.entries()) {
            lines.push(`subgraph P${packageIndex++}[${escapeLabel(name)}]`);
            appendClassNodes(lines, packageNodes);
            lines.push('end');
        }
    }

    edges.forEach(({from, to}) => {
        lines.push(`${escapeId(from)} --> ${escapeId(to)}`);
    });

    return {source: lines.join('\n')};
}

function renderPackageDiagram(context, packageFilterFqn, focusedPackageFqn) {
    const diagram = dom.getDiagram();
    if (!diagram) return;

    const renderPlan = buildDiagramRenderPlan(context, packageFilterFqn, focusedPackageFqn);
    applyDiagramRenderPlan(context, renderPlan);
    setDiagramSource(diagram, renderPlan.source);
    if (!globalThis.Jig || !globalThis.Jig.mermaid || typeof globalThis.Jig.mermaid.renderWithControls !== 'function') return;
    globalThis.Jig.mermaid.renderWithControls(diagram, renderPlan.source, {edgeCount: renderPlan.uniqueRelations.length});
}

function buildDiagramRenderPlan(context, packageFilterFqn, focusedPackageFqn) {
    const {packages, relations, causeRelationEvidence} = getPackageSummaryData(context);
    const {
        uniqueRelations,
        packageFqns,
        filteredCauseRelationEvidence
    } = buildVisibleDiagramElements(
        packages,
        relations,
        causeRelationEvidence,
        packageFilterFqn,
        focusedPackageFqn,
        context.aggregationDepth,
        context.focusCallerMode,
        context.focusCalleeMode,
        context.transitiveReductionEnabled
    );
    const nameByFqn = new Map(packages.map(item => [item.fqn, getGlossaryTitle(item.fqn)]));
    const {source, nodeIdToFqn, mutualPairs} = buildMermaidDiagramSource(
        packageFqns,
        uniqueRelations,
        nameByFqn,
        context.diagramDirection,
        focusedPackageFqn // Pass focusedPackageFqn here
    );
    return {
        source,
        nodeIdToFqn,
        mutualPairs,
        uniqueRelations,
        filteredCauseRelationEvidence,
    };
}

function applyDiagramRenderPlan(context, renderPlan) {
    context.diagramNodeIdToFqn = renderPlan.nodeIdToFqn;
    renderMutualDependencyList(renderPlan.mutualPairs, renderPlan.filteredCauseRelationEvidence, context.aggregationDepth, context);
}

function setDiagramSource(diagram, source) {
    diagram.removeAttribute('data-processed');
    diagram.textContent = source;
}

function renderDiagramAndTable(context) {
    renderPackageDiagram(context, context.packageFilterFqn, context.focusedPackageFqn);
    filterFocusTableRows(context.focusedPackageFqn, context);
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
    const {domainPackageRoots} = getPackageSummaryData(context);
    const candidate = findDefaultPackageFilterCandidate(domainPackageRoots);
    if (candidate) {
        initialDefaultFilterValue = candidate;
    }

    const applyFilter = () => {
        context.packageFilterFqn = normalizePackageFilterValue(input.value);
        renderDiagramAndTable(context);
        renderFocusLabel(context);
    };
    const clearPackageFilter = () => {
        input.value = '';
        context.packageFilterFqn = [];
        renderDiagramAndTable(context);
        renderFocusLabel(context);
    };
    const resetPackageFilter = () => {
        input.value = initialDefaultFilterValue; // 初期デフォルト値でinputを更新
        context.packageFilterFqn = normalizePackageFilterValue(initialDefaultFilterValue); // contextも更新
        renderDiagramAndTable(context);
        renderFocusLabel(context);
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

function updateDepthButtonStates(select, upButton, downButton) {
    if (!select || !upButton || !downButton) return;
    const currentValue = normalizeAggregationDepthValue(select.value);
    const options = Array.from(select.options).map(opt => Number(opt.value));
    const currentIndex = options.indexOf(currentValue);

    upButton.disabled = currentIndex <= 0;
    downButton.disabled = currentIndex < 0 || currentIndex >= options.length - 1;
}

function setupAggregationDepthControl(context) {
    const select = dom.getDepthSelect();
    if (!select) return;
    const {packages} = getPackageSummaryData(context);
    const maxDepth = packages.reduce((max, item) => Math.max(max, getPackageDepth(item.fqn)), 0);
    renderAggregationDepthSelectOptions(maxDepth, context);
    select.value = String(context.aggregationDepth);

    const upButton = dom.getDepthUpButton();
    const downButton = dom.getDepthDownButton();

    select.addEventListener('change', () => {
        context.aggregationDepth = normalizeAggregationDepthValue(select.value);
        renderDiagramAndTable(context);
        renderFocusLabel(context);
        renderAggregationDepthSelectOptions(maxDepth, context);
        updateDepthButtonStates(select, upButton, downButton);
    });

    if (upButton) {
        upButton.addEventListener('click', () => {
            const currentValue = normalizeAggregationDepthValue(select.value);
            const options = Array.from(select.options).map(opt => Number(opt.value));
            const currentIndex = options.indexOf(currentValue);
            if (currentIndex > 0) {
                select.value = String(options[currentIndex - 1]);
                select.dispatchEvent(new Event('change'));
            }
        });
    }
    if (downButton) {
        downButton.addEventListener('click', () => {
            const currentValue = normalizeAggregationDepthValue(select.value);
            const options = Array.from(select.options).map(opt => Number(opt.value));
            const currentIndex = options.indexOf(currentValue);
            if (currentIndex >= 0 && currentIndex < options.length - 1) {
                select.value = String(options[currentIndex + 1]);
                select.dispatchEvent(new Event('change'));
            }
        });
    }

    // 初期状態でボタンの状態を更新
    updateDepthButtonStates(select, upButton, downButton);
}

function renderAggregationDepthSelectOptions(maxDepth, context) {
    const select = dom.getDepthSelect();
    if (!select) return;
    const {packages, relations} = getPackageSummaryData(context);
    const aggregationStats = buildAggregationStatsForFilters(
        packages,
        relations,
        context.packageFilterFqn,
        context.focusedPackageFqn,
        maxDepth,
        context.aggregationDepth,
        context.focusCallerMode,
        context.focusCalleeMode
    );
    const options = buildAggregationDepthOptions(aggregationStats, maxDepth);
    renderAggregationDepthOptionsIntoSelect(select, options, context.aggregationDepth, maxDepth);

    // オプション変更後にボタンの状態を更新
    const upButton = dom.getDepthUpButton();
    const downButton = dom.getDepthDownButton();
    updateDepthButtonStates(select, upButton, downButton);
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

function setupFocusControl(context) {
    const callerSelect = dom.getFocusCallerModeSelect();
    const calleeSelect = dom.getFocusCalleeModeSelect();
    const clearButton = dom.getClearFocusButton();
    if (!callerSelect || !calleeSelect) return;

    callerSelect.value = context.focusCallerMode;
    calleeSelect.value = context.focusCalleeMode;

    const applyFilter = () => {
        if (context.focusedPackageFqn) { // フィルタ対象が選択されている場合のみ再描画
            renderDiagramAndTable(context);
        }
    };

    callerSelect.addEventListener('change', () => {
        context.focusCallerMode = callerSelect.value;
        applyFilter();
    });
    calleeSelect.addEventListener('change', () => {
        context.focusCalleeMode = calleeSelect.value;
        applyFilter();
    });

    if (clearButton) {
        clearButton.addEventListener('click', () => {
            context.focusedPackageFqn = null;
            context.packageFilterFqn = normalizePackageFilterValue(dom.getPackageFilterInput()?.value);
            renderDiagramAndTable(context);
            renderFocusLabel(context);
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
        if (!body || !body.classList.contains("package-summary")) return;
        setupSortableTables();
        renderPackageTable(packageContext);
        setupPackageFilterControl(packageContext);
        // domainPackageRootsに基づく初期aggregationDepth設定
        const {domainPackageRoots} = getPackageSummaryData(packageContext);
        packageContext.aggregationDepth = getInitialAggregationDepth(domainPackageRoots);
        setupAggregationDepthControl(packageContext);
        setupFocusControl(packageContext);
        setupDiagramDirectionControl(packageContext);
        setupTransitiveReductionControl(packageContext);
        registerDiagramClickHandler(packageContext);
        const applied = applyDefaultPackageFilterIfPresent(packageContext);
        if (!applied) {
            renderDiagramAndTable(packageContext);
        }
        renderFocusLabel(packageContext);
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
        getGlossaryTitle,
        getPackageDepth,
        getMaxPackageDepth,
        buildAggregationStats,
        buildAggregationStatsForFilters,
        normalizePackageFilterValue,
        normalizeAggregationDepthValue,
        findDefaultPackageFilterCandidate,
        getInitialAggregationDepth,
        buildPackageRowVisibility,
        buildFocusRowVisibility,
        collectFocusSet,
        buildVisibleDiagramRelations,
        filterFocusDiagramRelations,
        buildVisibleDiagramElements,
        buildPackageTableRowData,
        buildPackageTableRowSpecs,
        buildPackageTableActionSpecs,
        buildPackageTableRowElement,
        renderPackageTable,
        filterFocusTableRows,
        renderFocusLabel,
        setFocusAndRender,
        applyDefaultPackageFilterIfPresent,
        buildMutualDependencyItems,
        buildMermaidDiagramSource,
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
        setupFocusControl,
        setupDiagramDirectionControl,
        setupTransitiveReductionControl,
    };
}
