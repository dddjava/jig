const PackageApp = (() => {
    const Jig = globalThis.Jig;

    // 状態/DOMヘルパー
    // stateは「UI状態・設定値など長期的に保持する値」に限定する。
    // 一時的な中間データはstateに保存せず、関数内のローカル変数で扱う。
    const hierarchyState = {
        packageRelationCache: null,
        diagramNodeIdToFqn: new Map(),
        aggregationDepth: 0,
        packageFilterFqn: [],
        diagramDirection: 'TB',
        mutualDependencyDiagramDirection: 'LR',
        transitiveReductionEnabled: true,
    };

    const exploreState = {
        exploreTargetPackages: [],   // 指定したパッケージFQN[]（複数可）
        exploreCallerMode: '1',      // '0':なし, '1':直接, '-1':すべて
        exploreCalleeMode: '1',      // '0':なし, '1':直接, '-1':すべて
        diagramNodeIdToFqn: new Map(),
        diagramDirection: 'TB',
    };

    const HIERARCHY_DIAGRAM_CLICK_HANDLER_NAME = 'filterPackageDiagram';
    const EXPLORE_DIAGRAM_CLICK_HANDLER_NAME = 'explorePackageDiagram';

    const dom = {
        getPackageTableBody: () => document.querySelector('#package-table tbody'),
        getPackageTableRows: () => document.querySelectorAll('#package-table tbody tr'),
        getPackageFilterSelect: () => document.getElementById('package-filter-select'),
        getClearPackageFilterButton: () => document.getElementById('clear-package-filter'),
        getResetPackageFilterButton: () => document.getElementById('reset-package-filter'),
        getDepthSelect: () => document.getElementById('package-depth-select'),
        getDepthUpButton: () => document.getElementById('depth-up-button'),
        getDepthDownButton: () => document.getElementById('depth-down-button'),
        getTransitiveReductionToggle: () => document.getElementById('transitive-reduction-toggle'),
        getMutualDependencyList: () => document.getElementById('mutual-dependency-list'),
        getDiagram: () => document.getElementById('package-relation-diagram'),
        getExploreDiagram: () => document.getElementById('package-explore-diagram'),
        getExplorePackageList: () => document.getElementById('explore-package-list'),
        getExploreListFilter: () => document.getElementById('explore-list-filter'),
        getExploreClearSelectionButton: () => document.getElementById('explore-clear-selection'),
        getExploreCallerModeSelect: () => document.getElementById('explore-caller-mode-select'),
        getExploreCalleeModeSelect: () => document.getElementById('explore-callee-mode-select'),
        getDocumentBody: () => document.body,
    };

    // データ取得/整形
    function getPackageRelationData(context) {
        if (context.packageRelationCache) return context.packageRelationCache;
        const data = Jig.data.package.get() ?? {};
        context.packageRelationCache = parsePackageRelationData(data);
        return context.packageRelationCache;
    }

    function parsePackageRelationData(packageData) {
        const isArrayFormat = Array.isArray(packageData);
        return {
            packages: isArrayFormat ? packageData : (packageData?.packages ?? []),
            relations: isArrayFormat ? [] : (packageData?.relations ?? []),
            causeRelationEvidence: Jig.data.typeRelations.getRelations(),
            domainPackageRoots: isArrayFormat ? [] : (packageData?.domainPackageRoots ?? []),
        };
    }

    function getGlossaryTitle(fqn) {
        return Jig.glossary.getTypeTerm(fqn).title;
    }

    function getMaxPackageDepth() {
        const data = parsePackageRelationData(Jig.data.package.get() ?? {});
        return data.packages.reduce((max, item) => Math.max(max, Jig.util.getPackageDepth(item.fqn)), 0);
    }

    function aggregatePackageData(packages, relations, depth) {
        if (!depth || depth <= 0) return {packages, relations};

        const packageMap = new Map();
        packages.forEach(pkg => {
            const aggFqn = Jig.util.getAggregatedFqn(pkg.fqn, depth);
            if (!packageMap.has(aggFqn)) {
                packageMap.set(aggFqn, {fqn: aggFqn, classCount: 0});
            }
            packageMap.get(aggFqn).classCount += (pkg.classCount ?? 0);
        });

        const relationKeys = new Set();
        const aggregatedRelations = [];
        relations.forEach(rel => {
            const from = Jig.util.getAggregatedFqn(rel.from, depth);
            const to = Jig.util.getAggregatedFqn(rel.to, depth);
            if (from === to) return;
            const key = `${from}::${to}`;
            if (!relationKeys.has(key)) {
                relationKeys.add(key);
                aggregatedRelations.push({from, to});
            }
        });

        return {packages: Array.from(packageMap.values()), relations: aggregatedRelations};
    }

    function filterByPackageFilter(packages, relations, packageFilterFqn) {
        if (packageFilterFqn.length === 0) return {packages, relations};
        return {
            packages: packages.filter(pkg => Jig.util.isWithinPackageFilters(pkg.fqn, packageFilterFqn)),
            relations: relations.filter(r =>
                Jig.util.isWithinPackageFilters(r.from, packageFilterFqn) &&
                Jig.util.isWithinPackageFilters(r.to, packageFilterFqn)),
        };
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
            return domainPackageRoots;
        }
        return null;
    }

    function getInitialAggregationDepth(domainPackageRoots) {
        if (!domainPackageRoots?.length) return 0;
        const minDepth = Math.min(...domainPackageRoots.map(fqn => Jig.util.getPackageDepth(fqn)));
        return minDepth + 1;
    }

    function buildPackageRowVisibility(rowFqns, packageFilterFqn) {
        return rowFqns.map(fqn => Jig.util.isWithinPackageFilters(fqn, packageFilterFqn));
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
            const from = Jig.util.getAggregatedFqn(relation.from, aggregationDepth);
            const to = Jig.util.getAggregatedFqn(relation.to, aggregationDepth);
            if (!reverseAdjacency.has(to)) reverseAdjacency.set(to, new Set());
            reverseAdjacency.get(to).add(from);
        });
        return reverseAdjacency;
    }

    function buildForwardAdjacency(relations, aggregationDepth) {
        const forwardAdjacency = new Map();
        relations.forEach(relation => {
            const from = Jig.util.getAggregatedFqn(relation.from, aggregationDepth);
            const to = Jig.util.getAggregatedFqn(relation.to, aggregationDepth);
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
                    const from = Jig.util.getAggregatedFqn(relation.from, aggregationDepth);
                    const to = Jig.util.getAggregatedFqn(relation.to, aggregationDepth);
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
                    const from = Jig.util.getAggregatedFqn(relation.from, aggregationDepth);
                    const to = Jig.util.getAggregatedFqn(relation.to, aggregationDepth);
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

    // 関連探索: 複数の起点からcaller/calleeセットを収集する
    function collectExploreNodeSets(targetPackages, relations, callerMode, calleeMode) {
        const targetSet = new Set(targetPackages);
        const callerSet = new Set();
        const calleeSet = new Set();

        if (targetSet.size === 0) return {targetSet, callerSet, calleeSet};

        if (callerMode !== '0') {
            if (callerMode === '1') {
                relations.forEach(relation => {
                    if (targetSet.has(relation.to)) callerSet.add(relation.from);
                });
            } else {
                const reverseAdjacency = buildReverseAdjacency(relations, 0);
                targetSet.forEach(target => {
                    const callers = traverseGraph(target, reverseAdjacency);
                    callers.forEach(caller => {
                        if (!targetSet.has(caller)) callerSet.add(caller);
                    });
                });
            }
        }

        if (calleeMode !== '0') {
            if (calleeMode === '1') {
                relations.forEach(relation => {
                    if (targetSet.has(relation.from)) calleeSet.add(relation.to);
                });
            } else {
                const forwardAdjacency = buildForwardAdjacency(relations, 0);
                targetSet.forEach(target => {
                    const callees = traverseGraph(target, forwardAdjacency);
                    callees.forEach(callee => {
                        if (!targetSet.has(callee)) calleeSet.add(callee);
                    });
                });
            }
        }

        // callerSet/calleeSet からターゲットを除外
        targetSet.forEach(t => { callerSet.delete(t); calleeSet.delete(t); });

        return {targetSet, callerSet, calleeSet};
    }

    function buildVisibleDiagramElements(packages, relations, causeRelationEvidence, packageFilterFqn, aggregationDepth, transitiveReductionEnabled) {
        return Jig.mermaid.builder.buildVisibleDiagramRelations(
            packages,
            relations,
            causeRelationEvidence,
            {packageFilterFqn, aggregationDepth, transitiveReductionEnabled}
        );
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

    function buildPackageTableRowElement(spec, applyFilter) {
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
        const tbody = dom.getPackageTableBody();
        if (!tbody) return;

        const {packages, relations} = getPackageRelationData(context);
        const aggregated = aggregatePackageData(packages, relations, context.aggregationDepth);

        const {packages: filteredPackages, relations: filteredRelations} =
            filterByPackageFilter(aggregated.packages, aggregated.relations, context.packageFilterFqn);

        const rows = buildPackageTableRowData(filteredPackages, filteredRelations);
        const rowSpecs = buildPackageTableRowSpecs(rows);

        tbody.innerHTML = '';

        const applyFilter = fqn => {
            setPackageFilterSelectValues(context, [fqn]);
            renderHierarchyDiagramAndTable(context);
        };

        rowSpecs.forEach(spec => {
            const tr = buildPackageTableRowElement(spec, applyFilter);
            tbody.appendChild(tr);
        });
    }

    // 相互依存/依存関係の簡略表示
    function buildMutualDependencyItems(mutualPairs, causeRelationEvidence, aggregationDepth) {
        if (!mutualPairs || mutualPairs.size === 0) return [];
        const relationMap = new Map();
        causeRelationEvidence.forEach(relation => {
            const fromPackage = Jig.util.getAggregatedFqn(Jig.util.getPackageFqnFromTypeFqn(relation.from), aggregationDepth);
            const toPackage = Jig.util.getAggregatedFqn(Jig.util.getPackageFqnFromTypeFqn(relation.to), aggregationDepth);
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

        const applyFilterAndRender = (fqns) => {
            setPackageFilterSelectValues(context, fqns);
            renderHierarchyDiagramAndTable(context);
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
            button.addEventListener('click', () => applyFilterAndRender([package1, package2]));
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
        Jig.mermaid.render.renderWithControls(diagram, generator, {direction: context.mutualDependencyDiagramDirection});
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
            const packageFqn = Jig.util.getPackageFqnFromTypeFqn(node);
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
            from: Jig.util.getPackageFqnFromTypeFqn(from),
            to: Jig.util.getPackageFqnFromTypeFqn(to),
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
                const depth = Jig.util.getCommonPrefixDepth([packageFqn, root]);
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
                targetLines.push(Jig.mermaid.builder.getNodeDefinition(nodeId, className, 'class'));
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

        let lines = [`graph ${direction || 'TB'};`];

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

    function renderHierarchyDiagram(context) {
        const diagram = dom.getDiagram();
        if (!diagram) return;

        const renderPlan = buildHierarchyDiagramRenderPlan(context);
        applyHierarchyDiagramRenderPlan(context, renderPlan);
        setDiagramSource(diagram, renderPlan.source);

        const generator = (dir) => buildHierarchyDiagramRenderPlan(context, dir).source;
        Jig.mermaid.render.renderWithControls(diagram, generator, {direction: context.diagramDirection});
    }

    function buildHierarchyDiagramRenderPlan(context, direction = context.diagramDirection) {
        const {packages, relations, causeRelationEvidence} = getPackageRelationData(context);
        const {
            uniqueRelations,
            packageFqns,
            filteredCauseRelationEvidence
        } = buildVisibleDiagramElements(
            packages,
            relations,
            causeRelationEvidence,
            context.packageFilterFqn,
            context.aggregationDepth,
            context.transitiveReductionEnabled
        );
        const {source, nodeIdToFqn, mutualPairs} = Jig.mermaid.builder.buildMermaidDiagramSource(
            packageFqns, uniqueRelations,
            {diagramDirection: direction, clickHandlerName: HIERARCHY_DIAGRAM_CLICK_HANDLER_NAME}
        );
        return {
            source,
            nodeIdToFqn,
            mutualPairs,
            uniqueRelations,
            filteredCauseRelationEvidence,
        };
    }

    function applyHierarchyDiagramRenderPlan(context, renderPlan) {
        context.diagramNodeIdToFqn = renderPlan.nodeIdToFqn;
        renderMutualDependencyList(renderPlan.mutualPairs, renderPlan.filteredCauseRelationEvidence, context.aggregationDepth, context);
    }

    function setDiagramSource(diagram, source) {
        diagram.removeAttribute('data-processed');
        diagram.textContent = source;
    }

    function renderHierarchyDiagramAndTable(context) {
        renderHierarchyDiagram(context);
        renderPackageTable(context);
        renderAggregationDepthSelectOptions(getMaxPackageDepth(), context);
    }

    // 関連探索ダイアグラム
    function renderExploreDiagram(context) {
        const diagram = dom.getExploreDiagram();
        if (!diagram) return;

        const {packages, relations} = getPackageRelationData(context);
        const {targetSet, callerSet, calleeSet} = collectExploreNodeSets(
            context.exploreTargetPackages,
            relations,
            context.exploreCallerMode,
            context.exploreCalleeMode
        );

        if (targetSet.size === 0) {
            setDiagramSource(diagram, '');
            diagram.innerHTML = '<span class="placeholder-text">対象パッケージを追加してください</span>';
            return;
        }

        const visibleFqns = new Set([...targetSet, ...callerSet, ...calleeSet]);
        const visibleRelations = relations.filter(r => visibleFqns.has(r.from) && visibleFqns.has(r.to));

        const exploreOptions = (dir) => ({
            targetFqns: targetSet,
            callerFqns: callerSet,
            calleeFqns: calleeSet,
            diagramDirection: dir,
            clickHandlerName: EXPLORE_DIAGRAM_CLICK_HANDLER_NAME,
        });

        const renderPlan = Jig.mermaid.builder.buildExploreDiagramSource(visibleFqns, visibleRelations, exploreOptions(context.diagramDirection));
        setDiagramSource(diagram, renderPlan.source);
        context.diagramNodeIdToFqn = renderPlan.nodeIdToFqn;

        const generator = (dir) => Jig.mermaid.builder.buildExploreDiagramSource(visibleFqns, visibleRelations, exploreOptions(dir)).source;
        Jig.mermaid.render.renderWithControls(diagram, generator, {direction: context.diagramDirection});
    }

    function renderExplorePackageList(context) {
        const container = dom.getExplorePackageList();
        if (!container) return;

        const targetSet = new Set(context.exploreTargetPackages);

        // テーブルが既に存在する場合はクラスのみ更新する
        const existingTable = container.querySelector('table.explore-package-table');
        if (existingTable) {
            existingTable.querySelectorAll('tbody tr[data-fqn]').forEach(tr => {
                tr.classList.toggle('explore-target-selected', targetSet.has(tr.dataset.fqn));
            });
            return;
        }

        // 初回のみテーブルを構築する
        const {packages} = getPackageRelationData(hierarchyState);
        const sortedPackages = [...packages].sort((a, b) => a.fqn.localeCompare(b.fqn));

        const table = document.createElement('table');
        table.className = 'explore-package-table sortable';

        const thead = document.createElement('thead');
        const headerRow = document.createElement('tr');
        ['完全修飾名', '名称'].forEach(text => {
            const th = document.createElement('th');
            th.textContent = text;
            headerRow.appendChild(th);
        });
        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement('tbody');
        sortedPackages.forEach(pkg => {
            const tr = document.createElement('tr');
            tr.dataset.fqn = pkg.fqn;
            if (targetSet.has(pkg.fqn)) tr.classList.add('explore-target-selected');

            tr.addEventListener('click', () => {
                if (context.exploreTargetPackages.includes(pkg.fqn)) {
                    context.exploreTargetPackages = context.exploreTargetPackages.filter(p => p !== pkg.fqn);
                } else {
                    context.exploreTargetPackages = [...context.exploreTargetPackages, pkg.fqn];
                }
                renderExplorePackageList(context);
                renderExploreDiagram(context);
            });

            const fqnTd = document.createElement('td');
            fqnTd.textContent = pkg.fqn;
            fqnTd.className = 'fqn';
            tr.appendChild(fqnTd);

            const nameTd = document.createElement('td');
            nameTd.textContent = getGlossaryTitle(pkg.fqn);
            tr.appendChild(nameTd);

            tbody.appendChild(tr);
        });
        table.appendChild(tbody);
        container.appendChild(table);
        Jig.dom.setupSortableTables();

        const filterInput = dom.getExploreListFilter();
        if (filterInput) {
            filterInput.addEventListener('input', () => {
                const filterText = filterInput.value.toLowerCase();
                tbody.querySelectorAll('tr[data-fqn]').forEach(tr => {
                    const matches = !filterText
                        || tr.dataset.fqn.toLowerCase().includes(filterText)
                        || tr.cells[1]?.textContent.toLowerCase().includes(filterText);
                    tr.classList.toggle('hidden', !matches);
                });
            });
        }
    }

    // UI配線
    function setPackageFilterSelectValues(context, fqns) {
        const select = dom.getPackageFilterSelect();
        if (!select) return;
        context.packageFilterFqn = fqns;
        Array.from(select.children).forEach(option => {
            option.selected = fqns.includes(option.value);
        });
    }

    function setupPackageFilterControl(context) {
        const select = dom.getPackageFilterSelect();
        const clearPackageButton = dom.getClearPackageFilterButton();
        const resetButton = dom.getResetPackageFilterButton();
        if (!select) return;

        const {domainPackageRoots} = getPackageRelationData(context);
        const defaultFqns = findDefaultPackageFilterCandidate(domainPackageRoots) ?? [];

        const clearPackageFilter = () => {
            context.packageFilterFqn = [];
            Array.from(select.children).forEach(option => { option.selected = false; });
            renderHierarchyDiagramAndTable(context);
        };
        const resetPackageFilter = () => {
            setPackageFilterSelectValues(context, defaultFqns);
            renderHierarchyDiagramAndTable(context);
        };

        select.addEventListener('change', () => {
            context.packageFilterFqn = Array.from(select.selectedOptions).map(opt => opt.value);
            renderHierarchyDiagramAndTable(context);
        });

        if (clearPackageButton) clearPackageButton.addEventListener('click', clearPackageFilter);
        if (resetButton) resetButton.addEventListener('click', resetPackageFilter);
    }

    function buildPackageFilterSelectOptions(aggregatedPackages) {
        return aggregatedPackages.map(pkg => pkg.fqn).sort();
    }

    function renderPackageFilterSelectOptions(context) {
        const select = dom.getPackageFilterSelect();
        if (!select) return;

        const {packages, relations} = getPackageRelationData(context);
        const {packages: aggregatedPackages} = aggregatePackageData(packages, relations, context.aggregationDepth);
        const fqns = buildPackageFilterSelectOptions(aggregatedPackages);
        const currentSelected = new Set(context.packageFilterFqn);

        select.innerHTML = '';
        fqns.forEach(fqn => {
            const option = document.createElement('option');
            option.value = fqn;
            option.textContent = fqn;
            option.selected = currentSelected.has(fqn);
            select.appendChild(option);
        });
    }

    function applyDefaultPackageFilterIfPresent(context) {
        const {domainPackageRoots} = getPackageRelationData(context);
        const candidate = findDefaultPackageFilterCandidate(domainPackageRoots);
        if (!candidate || !candidate.length) return false;
        context.packageFilterFqn = candidate;
        renderPackageFilterSelectOptions(context);
        renderHierarchyDiagramAndTable(context);
        return true;
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
        const maxDepth = getMaxPackageDepth();
        renderAggregationDepthSelectOptions(maxDepth, context);
        select.value = String(context.aggregationDepth);

        const upButton = dom.getDepthUpButton();
        const downButton = dom.getDepthDownButton();

        select.addEventListener('change', () => {
            context.aggregationDepth = normalizeAggregationDepthValue(select.value);
            renderPackageFilterSelectOptions(context);
            renderHierarchyDiagramAndTable(context);
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
        const options = buildAggregationDepthOptions(maxDepth);
        renderAggregationDepthOptionsIntoSelect(select, options, context.aggregationDepth, maxDepth);

        const upButton = dom.getDepthUpButton();
        const downButton = dom.getDepthDownButton();
        updateDepthButtonStates(select, upButton, downButton);
    }

    function buildAggregationDepthOptions(maxDepth) {
        const options = [{value: '0', text: '集約なし'}];
        for (let depth = 1; depth <= maxDepth; depth += 1) {
            options.push({value: String(depth), text: `深さ${depth}`});
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

    function setupTransitiveReductionControl(context) {
        const checkbox = dom.getTransitiveReductionToggle();
        if (!checkbox) return;
        checkbox.checked = context.transitiveReductionEnabled;
        checkbox.addEventListener('change', () => {
            context.transitiveReductionEnabled = checkbox.checked;
            renderHierarchyDiagramAndTable(context);
        });
    }

    function setupExploreControl(context) {
        const clearButton = dom.getExploreClearSelectionButton();
        const callerSelect = dom.getExploreCallerModeSelect();
        const calleeSelect = dom.getExploreCalleeModeSelect();

        if (clearButton) {
            clearButton.addEventListener('click', () => {
                context.exploreTargetPackages = [];
                renderExplorePackageList(context);
                renderExploreDiagram(context);
            });
        }

        if (callerSelect) {
            callerSelect.value = context.exploreCallerMode;
            callerSelect.addEventListener('change', () => {
                context.exploreCallerMode = callerSelect.value;
                if (context.exploreTargetPackages.length > 0) renderExploreDiagram(context);
            });
        }

        if (calleeSelect) {
            calleeSelect.value = context.exploreCalleeMode;
            calleeSelect.addEventListener('change', () => {
                context.exploreCalleeMode = calleeSelect.value;
                if (context.exploreTargetPackages.length > 0) renderExploreDiagram(context);
            });
        }
    }

    function registerHierarchyDiagramClickHandler(context) {
        if (typeof window === 'undefined') return;
        window[HIERARCHY_DIAGRAM_CLICK_HANDLER_NAME] = function (nodeId) {
            const fqn = context.diagramNodeIdToFqn.get(nodeId);
            if (!fqn) return;
            // クリックしたパッケージでフィルタを絞り込む
            setPackageFilterSelectValues(context, [fqn]);
            renderHierarchyDiagramAndTable(context);
        };
    }

    function registerExploreDiagramClickHandler(context) {
        if (typeof window === 'undefined') return;
        window[EXPLORE_DIAGRAM_CLICK_HANDLER_NAME] = function (nodeId) {
            const fqn = context.diagramNodeIdToFqn.get(nodeId);
            if (!fqn) return;
            if (!context.exploreTargetPackages.includes(fqn)) {
                context.exploreTargetPackages = [...context.exploreTargetPackages, fqn];
                renderExplorePackageList(context);
                renderExploreDiagram(context);
            }
        };
    }

    function setupTabControl() {
        const tabs = document.querySelectorAll('.package-mode-tabs .tab-button');
        const panels = document.querySelectorAll('.package-tab-panel');
        tabs.forEach(tab => {
            tab.addEventListener('click', () => {
                tabs.forEach(t => t.classList.remove('is-active'));
                panels.forEach(p => p.classList.remove('is-active'));
                tab.classList.add('is-active');
                const panelId = `panel-${tab.dataset.tab}`;
                const panel = document.getElementById(panelId);
                if (panel) panel.classList.add('is-active');
            });
        });
    }

    function init() {
        if (typeof document === 'undefined' || !document.body.classList.contains("package-relation")) return;
        Jig.dom.setupSortableTables();
        setupTabControl();

        // 階層探索の初期化
        setupPackageFilterControl(hierarchyState);
        const {domainPackageRoots} = getPackageRelationData(hierarchyState);
        hierarchyState.aggregationDepth = getInitialAggregationDepth(domainPackageRoots);
        setupAggregationDepthControl(hierarchyState);
        renderPackageFilterSelectOptions(hierarchyState);
        setupTransitiveReductionControl(hierarchyState);
        registerHierarchyDiagramClickHandler(hierarchyState);

        const applied = applyDefaultPackageFilterIfPresent(hierarchyState);
        if (!applied) {
            renderHierarchyDiagramAndTable(hierarchyState);
        }

        // 関連探索の初期化
        setupExploreControl(exploreState);
        registerExploreDiagramClickHandler(exploreState);
        renderExploreDiagram(exploreState);
        renderExplorePackageList(exploreState);
    }

    return {
        init,
        hierarchyState,
        exploreState,
        HIERARCHY_DIAGRAM_CLICK_HANDLER_NAME,
        EXPLORE_DIAGRAM_CLICK_HANDLER_NAME,
        dom,

        // For testing
        getPackageRelationData,
        parsePackageRelationData,
        getGlossaryTitle,
        getMaxPackageDepth,
        aggregatePackageData,
        filterByPackageFilter,
        normalizePackageFilterValue,
        normalizeAggregationDepthValue,
        findDefaultPackageFilterCandidate,
        getInitialAggregationDepth,
        buildPackageRowVisibility,
        collectFocusSet,
        collectExploreNodeSets,
        buildVisibleDiagramElements,
        buildPackageTableRowData,
        buildPackageTableRowSpecs,
        buildPackageTableRowElement,
        renderPackageTable,
        applyDefaultPackageFilterIfPresent,
        buildMutualDependencyItems,
        renderMutualDependencyList,
        renderMutualDependencyDiagram,
        buildMutualDependencyDiagramSource,
        renderHierarchyDiagram,
        renderHierarchyDiagramAndTable,
        buildHierarchyDiagramRenderPlan,
        renderExploreDiagram,
        renderExplorePackageList,
        registerHierarchyDiagramClickHandler,
        registerExploreDiagramClickHandler,
        setupPackageFilterControl,
        setupAggregationDepthControl,
        renderAggregationDepthSelectOptions,
        buildAggregationDepthOptions,
        renderAggregationDepthOptionsIntoSelect,
        renderPackageFilterSelectOptions,
        buildPackageFilterSelectOptions,
        setupTransitiveReductionControl,
        setupExploreControl,
        setupTabControl,
        setPackageFilterSelectValues,
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
