globalThis.Jig ??= {};
globalThis.Jig.dom ??= {};
globalThis.Jig.glossary ??= {};
globalThis.Jig.packageDiagram ??= {};

const PackageApp = (() => {
    const Jig = globalThis.Jig;

    // 状態/DOMヘルパー
    // stateは「UI状態・設定値など長期的に保持する値」に限定する。
    // 一時的な中間データはstateに保存せず、関数内のローカル変数で扱う。
    const state = {
        packageRelationCache: null,
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
        getFocusCallerModeSelect: () => document.getElementById('focus-caller-mode-select'),
        getFocusCalleeModeSelect: () => document.getElementById('focus-callee-mode-select'),
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
    function getPackageRelationData(context) {
        if (context.packageRelationCache) return context.packageRelationCache;
        const data = globalThis.packageData ?? {};
        context.packageRelationCache = parsePackageRelationData(data);
        return context.packageRelationCache;
    }

    function parsePackageRelationData(packageData) {
        const isArrayFormat = Array.isArray(packageData);
        return {
            packages: isArrayFormat ? packageData : (packageData?.packages ?? []),
            relations: isArrayFormat ? [] : (packageData?.relations ?? []),
            causeRelationEvidence: globalThis.typeRelationsData?.relations ?? [],
            domainPackageRoots: isArrayFormat ? [] : (packageData?.domainPackageRoots ?? []),
        };
    }

    function getGlossaryTitle(fqn) {
        return Jig.glossary.getTypeTerm(fqn).title;
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
        const {packages} = getPackageRelationData(context);
        return packages.reduce((max, item) => Math.max(max, getPackageDepth(item.fqn)), 0);
    }

    // 集計
    function buildAggregationStats(packages, relations, maxDepth) {
        const stats = new Map();
        for (let depth = 0; depth <= maxDepth; depth += 1) {
            const aggregatedPackages = new Set(packages.map(item => Jig.packageDiagram.getAggregatedFqn(item.fqn, depth)));
            const relationKeys = new Set();
            relations.forEach(relation => {
                const from = Jig.packageDiagram.getAggregatedFqn(relation.from, depth);
                const to = Jig.packageDiagram.getAggregatedFqn(relation.to, depth);
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
            ? packages.filter(item => Jig.packageDiagram.isWithinPackageFilters(item.fqn, packageFilterFqn))
            : packages;
        let filteredRelations = packageFilterFqn.length > 0
            ? relations.filter(relation => Jig.packageDiagram.isWithinPackageFilters(relation.from, packageFilterFqn) && Jig.packageDiagram.isWithinPackageFilters(relation.to, packageFilterFqn))
            : relations;

        if (focusedPackageFqn) {
            const aggregatedRoot = Jig.packageDiagram.getAggregatedFqn(focusedPackageFqn, aggregationDepth);
            const focusSet = collectFocusSet(aggregatedRoot, filteredRelations, aggregationDepth, focusCallerMode, focusCalleeMode);
            filteredPackages = filteredPackages.filter(item =>
                focusSet.has(Jig.packageDiagram.getAggregatedFqn(item.fqn, aggregationDepth))
            );
            
            const filterDirectRelation = (relation) => {
                const from = Jig.packageDiagram.getAggregatedFqn(relation.from, aggregationDepth);
                const to = Jig.packageDiagram.getAggregatedFqn(relation.to, aggregationDepth);
                const isCaller = focusCallerMode === '1' && to === aggregatedRoot;
                const isCallee = focusCalleeMode === '1' && from === aggregatedRoot;
                return isCaller || isCallee;
            };

            const filterTransitiveRelation = (relation) => {
                const from = Jig.packageDiagram.getAggregatedFqn(relation.from, aggregationDepth);
                const to = Jig.packageDiagram.getAggregatedFqn(relation.to, aggregationDepth);
                return focusSet.has(from) && focusSet.has(to);
            };

            if (focusCallerMode === '-1' || focusCalleeMode === '-1') {
                filteredRelations = filteredRelations.filter(filterTransitiveRelation);
            } else if (focusCallerMode === '1' || focusCalleeMode === '1') {
                filteredRelations = filteredRelations.filter(filterDirectRelation);
            }
        }
        return buildAggregationStats(filteredPackages, filteredRelations, maxDepth);
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
        return rowFqns.map(fqn => Jig.packageDiagram.isWithinPackageFilters(fqn, packageFilterFqn));
    }

    function buildFocusRowVisibility(rowFqns, relations, packageFilterFqn, aggregationDepth, focusCallerMode, focusCalleeMode, focusedPackageFqn) {
        if (!focusedPackageFqn) {
            return rowFqns.map(rowFqn => Jig.packageDiagram.isWithinPackageFilters(rowFqn, packageFilterFqn));
        }

        const filteredRelations = packageFilterFqn.length > 0
            ? relations.filter(relation =>
                Jig.packageDiagram.isWithinPackageFilters(relation.from, packageFilterFqn) && Jig.packageDiagram.isWithinPackageFilters(relation.to, packageFilterFqn)
            )
            : relations;
        const aggregatedRoot = Jig.packageDiagram.getAggregatedFqn(focusedPackageFqn, aggregationDepth);
        const focusSet = collectFocusSet(aggregatedRoot, filteredRelations, aggregationDepth, focusCallerMode, focusCalleeMode);
        return rowFqns.map(rowFqn => {
            const aggregatedRow = Jig.packageDiagram.getAggregatedFqn(rowFqn, aggregationDepth);
            return Jig.packageDiagram.isWithinPackageFilters(rowFqn, packageFilterFqn) && focusSet.has(aggregatedRow);
        });
    }

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

    function buildReverseAdjacency(relations, aggregationDepth) {
        const reverseAdjacency = new Map();
        relations.forEach(relation => {
            const from = Jig.packageDiagram.getAggregatedFqn(relation.from, aggregationDepth);
            const to = Jig.packageDiagram.getAggregatedFqn(relation.to, aggregationDepth);
            if (!reverseAdjacency.has(to)) reverseAdjacency.set(to, new Set());
            reverseAdjacency.get(to).add(from);
        });
        return reverseAdjacency;
    }

    function buildForwardAdjacency(relations, aggregationDepth) {
        const forwardAdjacency = new Map();
        relations.forEach(relation => {
            const from = Jig.packageDiagram.getAggregatedFqn(relation.from, aggregationDepth);
            const to = Jig.packageDiagram.getAggregatedFqn(relation.to, aggregationDepth);
            if (!forwardAdjacency.has(from)) forwardAdjacency.set(from, new Set());
            forwardAdjacency.get(from).add(to);
        });
        return forwardAdjacency;
    }

    function collectFocusSet(root, relations, aggregationDepth, focusCallerMode, focusCalleeMode) {
        if (!root) return new Set();

        const focusSet = new Set([root]);

        if (focusCallerMode !== '0') {
            if (focusCallerMode === '1') {
                relations.forEach(relation => {
                    const from = Jig.packageDiagram.getAggregatedFqn(relation.from, aggregationDepth);
                    const to = Jig.packageDiagram.getAggregatedFqn(relation.to, aggregationDepth);
                    if (to === root) focusSet.add(from);
                });
            } else {
                const reverseAdjacency = buildReverseAdjacency(relations, aggregationDepth);
                const callers = traverseGraph(root, reverseAdjacency);
                callers.forEach(caller => focusSet.add(caller));
            }
        }

        if (focusCalleeMode !== '0') {
            if (focusCalleeMode === '1') {
                relations.forEach(relation => {
                    const from = Jig.packageDiagram.getAggregatedFqn(relation.from, aggregationDepth);
                    const to = Jig.packageDiagram.getAggregatedFqn(relation.to, aggregationDepth);
                    if (from === root) focusSet.add(to);
                });
            } else {
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
            
            const filterDirectRelation = (relation) => {
                const from = Jig.packageDiagram.getAggregatedFqn(relation.from, aggregationDepth);
                const to = Jig.packageDiagram.getAggregatedFqn(relation.to, aggregationDepth);
                const isCaller = focusCallerMode === '1' && to === aggregatedRoot;
                const isCallee = focusCalleeMode === '1' && from === aggregatedRoot;
                return isCaller || isCallee;
            };

            const filterTransitiveRelation = (relation) => {
                const from = Jig.packageDiagram.getAggregatedFqn(relation.from, aggregationDepth);
                const to = Jig.packageDiagram.getAggregatedFqn(relation.to, aggregationDepth);
                return focusSet.has(from) && focusSet.has(to);
            };

            if (focusCallerMode === '-1' || focusCalleeMode === '-1') {
                nextRelations = uniqueRelations.filter(filterTransitiveRelation);
            } else if (focusCallerMode === '1' || focusCalleeMode === '1') {
                nextRelations = uniqueRelations.filter(filterDirectRelation);
            } else {
                nextRelations = uniqueRelations.filter(relation =>
                    focusSet.has(Jig.packageDiagram.getAggregatedFqn(relation.from, aggregationDepth)) &&
                    focusSet.has(Jig.packageDiagram.getAggregatedFqn(relation.to, aggregationDepth))
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
        const base = Jig.packageDiagram.buildVisibleDiagramRelations(
            packages,
            relations,
            causeRelationEvidence,
            {packageFilterFqn, aggregationDepth, transitiveReductionEnabled}
        );
        const aggregatedRoot = focusedPackageFqn ? Jig.packageDiagram.getAggregatedFqn(focusedPackageFqn, aggregationDepth) : null;
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
        const {packages, relations} = getPackageRelationData(context);
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
        const {relations} = getPackageRelationData(context);
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
        const {domainPackageRoots} = getPackageRelationData(context);
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
            const fromPackage = Jig.packageDiagram.getAggregatedFqn(Jig.packageDiagram.getPackageFqnFromTypeFqn(relation.from), aggregationDepth);
            const toPackage = Jig.packageDiagram.getAggregatedFqn(Jig.packageDiagram.getPackageFqnFromTypeFqn(relation.to), aggregationDepth);
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

        const generator = (dir) => buildMutualDependencyDiagramSource(item.causes, dir, item.pairLabel).source;
        if (!generator(context.mutualDependencyDiagramDirection)) {
            diagram.innerHTML = '';
            diagram.style.display = 'none';
            return;
        }

        const button = itemNode.querySelector('.diagram-button');
        if (button) {
            button.style.display = 'none';
        }

        diagram.style.display = 'block';
        if (!globalThis.Jig || !globalThis.Jig.mermaid || typeof globalThis.Jig.mermaid.renderWithControls !== 'function') return;
        Jig.mermaid.renderWithControls(diagram, generator, {direction: context.mutualDependencyDiagramDirection});
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

        const packages = new Map();
        nodes.forEach(node => {
            const packageFqn = Jig.packageDiagram.getPackageFqnFromTypeFqn(node);
            const packageName = packageFqn === '(default)'
                ? '(default)'
                : Jig.glossary.typeSimpleName(packageFqn);
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
            from: Jig.packageDiagram.getPackageFqnFromTypeFqn(from),
            to: Jig.packageDiagram.getPackageFqnFromTypeFqn(to),
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
                const depth = Jig.packageDiagram.getCommonPrefixDepth([packageFqn, root]);
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
                const className = Jig.glossary.typeSimpleName(classFqn);
                targetLines.push(Jig.mermaid.getNodeDefinition(nodeId, className, 'class'));
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
                const rootLabel = Jig.glossary.typeSimpleName(root);
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
        const generator = (dir) => buildDiagramRenderPlan(context, packageFilterFqn, focusedPackageFqn, dir).source;
        Jig.mermaid.renderWithControls(diagram, generator, {
            edgeCount: renderPlan.uniqueRelations.length,
            direction: context.diagramDirection
        });
    }

    function buildDiagramRenderPlan(context, packageFilterFqn, focusedPackageFqn, direction = context.diagramDirection) {
        const {packages, relations, causeRelationEvidence} = getPackageRelationData(context);
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
        const {source, nodeIdToFqn, mutualPairs} = Jig.packageDiagram.buildMermaidDiagramSource(
            packageFqns, uniqueRelations,
            {diagramDirection: direction, focusedPackageFqn, clickHandlerName: DIAGRAM_CLICK_HANDLER_NAME}
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
        const resetButton = dom.getResetPackageFilterButton();
        if (!input || !applyButton || !clearPackageButton) return;

        let initialDefaultFilterValue = '';
        const {domainPackageRoots} = getPackageRelationData(context);
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
            input.value = initialDefaultFilterValue;
            context.packageFilterFqn = normalizePackageFilterValue(initialDefaultFilterValue);
            renderDiagramAndTable(context);
            renderFocusLabel(context);
        };

        applyButton.addEventListener('click', applyFilter);
        clearPackageButton.addEventListener('click', clearPackageFilter);
        if (resetButton) {
            resetButton.addEventListener('click', resetPackageFilter);
        }
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
        const maxDepth = getMaxPackageDepth(context);
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

        updateDepthButtonStates(select, upButton, downButton);
    }

    function renderAggregationDepthSelectOptions(maxDepth, context) {
        const select = dom.getDepthSelect();
        if (!select) return;
        const {packages, relations} = getPackageRelationData(context);
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
            if (context.focusedPackageFqn) {
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

    function init() {
        if (typeof document === 'undefined' || !document.body.classList.contains("package-relation")) return;
        Jig.dom.setupSortableTables();
        renderPackageTable(state);
        setupPackageFilterControl(state);
        const {domainPackageRoots} = getPackageRelationData(state);
        state.aggregationDepth = getInitialAggregationDepth(domainPackageRoots);
        setupAggregationDepthControl(state);
        setupFocusControl(state);
        setupDiagramDirectionControl(state);
        setupTransitiveReductionControl(state);
        registerDiagramClickHandler(state);
        const applied = applyDefaultPackageFilterIfPresent(state);
        if (!applied) {
            renderDiagramAndTable(state);
        }
        renderFocusLabel(state);
    }

    return {
        init,
        state,
        DIAGRAM_CLICK_HANDLER_NAME,
        dom,

        // For testing
        getPackageRelationData,
        parsePackageRelationData,
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

        // Expose internal functions from globalThis.Jig.packageDiagram for testing via PackageApp
        buildDiagramNodeMaps: (...args) => globalThis.Jig.packageDiagram.buildDiagramNodeMaps(...args),
        buildDiagramNodeLabel: (...args) => globalThis.Jig.packageDiagram.buildDiagramNodeLabel(...args),
        buildDiagramSubgraphLabel: (...args) => globalThis.Jig.packageDiagram.buildDiagramSubgraphLabel(...args),
        buildDiagramNodeTooltip: (...args) => globalThis.Jig.packageDiagram.buildDiagramNodeTooltip(...args),
        buildDiagramGroupTree: (...args) => globalThis.Jig.packageDiagram.buildDiagramGroupTree(...args),
        buildSubgraphLines: (...args) => globalThis.Jig.packageDiagram.buildSubgraphLines(...args),
        buildDiagramEdgeLines: (...args) => globalThis.Jig.packageDiagram.buildDiagramEdgeLines(...args),
        buildDiagramNodeLines: (...args) => globalThis.Jig.packageDiagram.buildDiagramNodeLines(...args),
    };
})();

if (typeof document !== 'undefined') {
    document.addEventListener("DOMContentLoaded", () => {
        PackageApp.init();
    });
}

if (typeof module !== 'undefined' && module.exports) {
    module.exports = PackageApp;
}
