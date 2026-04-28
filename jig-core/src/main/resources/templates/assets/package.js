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
        hierarchyCollapsedPackages: [],
        diagramDirection: 'TB',
        mutualDependencyDiagramDirection: 'LR',
        transitiveReductionEnabled: true,
    };

    const exploreState = {
        exploreTargetPackages: [],   // 指定したパッケージFQN[]（複数可）
        exploreCollapsedPackages: [], // 折りたたみ中のパッケージFQN[]
        exploreCallerMode: '1',      // '0':なし, '1':直接, '-1':すべて
        exploreCalleeMode: '1',      // '0':なし, '1':直接, '-1':すべて
        diagramNodeIdToFqn: new Map(),
        diagramDirection: 'TB',
    };

    const HIERARCHY_DIAGRAM_CLICK_HANDLER_NAME = 'filterPackageDiagram';
    const EXPLORE_DIAGRAM_CLICK_HANDLER_NAME = 'explorePackageDiagram';
    const TAB = {HIERARCHY: 'hierarchy', EXPLORE: 'explore'};

    const dom = {
        getClearPackageFilterButton: () => document.getElementById('clear-package-filter'),
        getResetPackageFilterButton: () => document.getElementById('reset-package-filter'),
        getDepthSelect: () => document.getElementById('package-depth-select'),
        getDepthUpButton: () => document.getElementById('depth-up-button'),
        getDepthDownButton: () => document.getElementById('depth-down-button'),
        getTransitiveReductionToggle: () => document.getElementById('transitive-reduction-toggle'),
        getMutualDependencyList: () => document.getElementById('mutual-dependency-list'),
        getDiagram: () => document.getElementById('package-relation-diagram'),
        getExploreDiagram: () => document.getElementById('package-explore-diagram'),
        getHierarchyPackageList: () => document.getElementById('hierarchy-package-table'),
        getHierarchyListFilter: () => document.getElementById('hierarchy-list-filter'),
        getExplorePackageList: () => document.getElementById('explore-package-table'),
        getExploreListFilter: () => document.getElementById('explore-list-filter'),
        getExploreClearSelectionButton: () => document.getElementById('explore-clear-selection'),
        getExploreCallerModeRadios: () => document.querySelectorAll('input[name="explore-caller-mode"]'),
        getExploreCalleeModeRadios: () => document.querySelectorAll('input[name="explore-callee-mode"]'),
        getDocumentBody: () => document.body,
    };

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

    function buildAdjacency(relations, aggregationDepth, reversed) {
        const adjacency = new Map();
        relations.forEach(relation => {
            const from = Jig.util.getAggregatedFqn(relation.from, aggregationDepth);
            const to = Jig.util.getAggregatedFqn(relation.to, aggregationDepth);
            const key = reversed ? to : from;
            const value = reversed ? from : to;
            Jig.util.addToSetMap(adjacency, key, value);
        });
        return adjacency;
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
                const reverseAdjacency = buildAdjacency(relations, aggregationDepth, true);
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
                const forwardAdjacency = buildAdjacency(relations, aggregationDepth, false);
                const callees = traverseGraph(root, forwardAdjacency);
                callees.forEach(callee => focusSet.add(callee));
            }
        }

        return focusSet;
    }

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
                const reverseAdjacency = buildAdjacency(relations, 0, true);
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
                const forwardAdjacency = buildAdjacency(relations, 0, false);
                targetSet.forEach(target => {
                    const callees = traverseGraph(target, forwardAdjacency);
                    callees.forEach(callee => {
                        if (!targetSet.has(callee)) calleeSet.add(callee);
                    });
                });
            }
        }

        targetSet.forEach(t => {
            callerSet.delete(t);
            calleeSet.delete(t);
        });

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

    function createNumberTd(value, countName) {
        const td = Jig.dom.createElement('td', {textContent: String(value ?? 0), className: 'number'});
        if (countName) td.dataset.count = countName;
        return td;
    }

    function buildMutualDependencyItems(mutualPairs, causeRelationEvidence, aggregationDepth) {
        if (!mutualPairs || mutualPairs.size === 0) return [];
        const relationMap = new Map();
        causeRelationEvidence.forEach(relation => {
            const fromPackage = Jig.util.getAggregatedFqn(Jig.util.getPackageFqnFromTypeFqn(relation.from), aggregationDepth);
            const toPackage = Jig.util.getAggregatedFqn(Jig.util.getPackageFqnFromTypeFqn(relation.to), aggregationDepth);
            if (fromPackage === toPackage) return;
            const key = fromPackage < toPackage ? `${fromPackage}::${toPackage}` : `${toPackage}::${fromPackage}`;
            Jig.util.addToSetMap(relationMap, key, `${relation.from} -> ${relation.to}`);
        });
        return Array.from(mutualPairs).sort().map(key => {
            const parts = key.split('::');
            const pairLabel = `${parts[0]} <-> ${parts[1]}`;
            const titles = [getGlossaryTitle(parts[0]), getGlossaryTitle(parts[1])];
            const titleLabel = `${titles[0]} <-> ${titles[1]}`;
            const allCauses = Array.from(relationMap.get(key) ?? []).sort();
            const causesForward = allCauses.filter(c => {
                const fromPkg = Jig.util.getAggregatedFqn(Jig.util.getPackageFqnFromTypeFqn(c.split(' -> ')[0]), aggregationDepth);
                return fromPkg === parts[0];
            });
            const causesBackward = allCauses.filter(c => {
                const fromPkg = Jig.util.getAggregatedFqn(Jig.util.getPackageFqnFromTypeFqn(c.split(' -> ')[0]), aggregationDepth);
                return fromPkg === parts[1];
            });
            return {
                pairLabel,
                titleLabel,
                titles,
                causes: allCauses,
                causesForward,
                causesBackward,
            };
        });
    }

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
        const heading = Jig.dom.createElement('h3', {textContent: '相互依存分析'});
        const sections = items.map(item => {
            let diagramRendered = false;

            const tabSection = Jig.dom.tab.buildSection(
                [
                    {id: 'overview', label: '概要'},
                    {id: 'diagram', label: 'クラス関連図'},
                    {id: 'text', label: 'テキスト'},
                ],
                {
                    className: 'jig-card-section tab-content-section tab-mutual-dependency',
                    initialActiveId: 'overview',
                    onTabChange: (id) => {
                        if (id === 'diagram' && !diagramRendered) {
                            diagramRendered = true;
                            const forwardCheckbox = Jig.dom.createElement('input', {attributes: {type: 'checkbox'}});
                            forwardCheckbox.checked = true;
                            const backwardCheckbox = Jig.dom.createElement('input', {attributes: {type: 'checkbox'}});
                            backwardCheckbox.checked = true;
                            tabSection.panels['diagram'].appendChild(Jig.dom.createElement('fieldset', {
                                className: 'diagram-panel-options',
                                children: [
                                    Jig.dom.createElement('legend', {textContent: '表示する関連'}),
                                    Jig.dom.createElement('label', {className: 'diagram-panel-option', children: [forwardCheckbox, `${item.titles[0]} → ${item.titles[1]} (${item.causesForward.length}件)`]}),
                                    Jig.dom.createElement('label', {className: 'diagram-panel-option', children: [backwardCheckbox, `${item.titles[0]} ← ${item.titles[1]} (${item.causesBackward.length}件)`]}),
                                ],
                            }));
                            const diagramContainer = Jig.dom.createElement('div', {className: 'mermaid-diagram'});
                            tabSection.panels['diagram'].appendChild(diagramContainer);
                            const render = (container) => {
                                container.innerHTML = '';
                                const filteredCauses = [
                                    ...(forwardCheckbox.checked ? item.causesForward : []),
                                    ...(backwardCheckbox.checked ? item.causesBackward : []),
                                ];
                                const generator = (dir) => buildMutualDependencyDiagramSource(filteredCauses, dir, item.pairLabel).source;
                                if (generator(context.mutualDependencyDiagramDirection)) {
                                    Jig.mermaid.render.renderWithControls(container, generator, {direction: context.mutualDependencyDiagramDirection});
                                }
                            };
                            render(diagramContainer);
                            forwardCheckbox.addEventListener('change', () => render(diagramContainer));
                            backwardCheckbox.addEventListener('change', () => render(diagramContainer));
                        }
                    }
                }
            );

            const tabsBar = tabSection.section.children[0];
            tabsBar.insertBefore(
                Jig.dom.createElement('span', {className: 'mutual-dependency-title', textContent: item.titleLabel}),
                tabsBar.children[0]
            );

            tabSection.panels['overview'].appendChild(
                Jig.dom.createElement('span', {className: 'pair-label', textContent: item.pairLabel})
            );

            tabSection.panels['text'].appendChild(
                Jig.dom.createElement('pre', {
                    className: 'causes',
                    textContent: item.causes?.length ? item.causes.join('\n') : ''
                })
            );

            return tabSection.section;
        });
        container.innerHTML = '';
        container.appendChild(heading);
        sections.forEach(section => container.appendChild(section));
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
        packageRelations.forEach(({from, to}) => {
            if (!from || !to || from === to) return;
            Jig.util.addToSetMap(packageAdjacency, from, to);
            Jig.util.addToSetMap(packageAdjacency, to, from);
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
        renderHierarchyPackageList(context);
        renderAggregationDepthSelectOptions(getMaxPackageDepth(), context);
        syncStateToURL();
    }

    function buildCollapsedSubpackageMap(selectedPackages) {
        const tbody = dom.getExplorePackageList()?.querySelector('tbody');
        if (!tbody) return new Map();
        const selectedSet = new Set(selectedPackages);
        // 最も近い祖先を優先するため長さ降順でソート
        const sortedSelected = [...selectedPackages].sort((a, b) => b.length - a.length);
        const map = new Map();
        tbody.querySelectorAll('tr[data-fqn].hidden-by-collapse').forEach(tr => {
            const fqn = tr.dataset.fqn;
            if (selectedSet.has(fqn)) return;
            const parent = sortedSelected.find(p => fqn.startsWith(p + '.'));
            if (parent) map.set(fqn, parent);
        });
        return map;
    }

    function renderExploreDiagram(context) {
        const diagram = dom.getExploreDiagram();
        if (!diagram) {
            syncStateToURL();
            return;
        }

        const {relations} = getPackageRelationData(context);

        const collapsedToParent = buildCollapsedSubpackageMap(context.exploreTargetPackages);
        const effectiveTargets = [...context.exploreTargetPackages, ...collapsedToParent.keys()];

        const {targetSet, callerSet, calleeSet} = collectExploreNodeSets(
            effectiveTargets,
            relations,
            context.exploreCallerMode,
            context.exploreCalleeMode
        );

        if (targetSet.size === 0) {
            setDiagramSource(diagram, '');
            diagram.innerHTML = '<span class="placeholder-text">対象パッケージを追加してください</span>';
            syncStateToURL();
            return;
        }

        const resolve = fqn => collapsedToParent.get(fqn) ?? fqn;
        const resolvedTargetSet = new Set([...targetSet].map(resolve));
        const resolvedCallerSet = new Set([...callerSet].map(resolve).filter(fqn => !resolvedTargetSet.has(fqn)));
        const resolvedCalleeSet = new Set([...calleeSet].map(resolve).filter(fqn => !resolvedTargetSet.has(fqn)));

        const visibleFqns = new Set([...resolvedTargetSet, ...resolvedCallerSet, ...resolvedCalleeSet]);

        const seenRelations = new Set();
        const visibleRelations = [];
        relations.forEach(r => {
            const from = resolve(r.from);
            const to = resolve(r.to);
            if (from === to) return;
            if (!visibleFqns.has(from) || !visibleFqns.has(to)) return;
            const key = `${from}::${to}`;
            if (seenRelations.has(key)) return;
            seenRelations.add(key);
            visibleRelations.push({from, to});
        });

        const exploreOptions = (dir) => ({
            targetFqns: resolvedTargetSet,
            callerFqns: resolvedCallerSet,
            calleeFqns: resolvedCalleeSet,
            diagramDirection: dir,
            clickHandlerName: EXPLORE_DIAGRAM_CLICK_HANDLER_NAME,
        });

        const renderPlan = Jig.mermaid.builder.buildExploreDiagramSource(visibleFqns, visibleRelations, exploreOptions(context.diagramDirection));
        setDiagramSource(diagram, renderPlan.source);
        context.diagramNodeIdToFqn = renderPlan.nodeIdToFqn;

        const generator = (dir) => Jig.mermaid.builder.buildExploreDiagramSource(visibleFqns, visibleRelations, exploreOptions(dir)).source;
        Jig.mermaid.render.renderWithControls(diagram, generator, {direction: context.diagramDirection});
        syncStateToURL();
    }

    function getRelativeFqn(fqn, fqnSet) {
        const parts = fqn.split('.');
        for (let i = parts.length - 1; i > 0; i--) {
            const ancestor = parts.slice(0, i).join('.');
            if (fqnSet.has(ancestor)) {
                const relative = fqn.substring(ancestor.length + 1);
                return {ancestor, relative};
            }
        }
        return {ancestor: undefined, relative: fqn};
    }

    function renderPackageList(config) {
        const container = config.getContainer();
        if (!container) return;

        const existingTbody = container.querySelector('tbody');
        if (existingTbody) {
            existingTbody.querySelectorAll('tr[data-fqn]').forEach(tr => {
                tr.classList.toggle('explore-target-selected', config.isSelected(tr.dataset.fqn));
            });
            return;
        }

        const {packages, relations, domainPackageRoots} = getPackageRelationData(hierarchyState);
        // domainPackageRoots に含まれるが packages にないものを追加する
        const packageFqnSet = new Set(packages.map(p => p.fqn));
        const allPackages = [
            ...packages,
            ...(domainPackageRoots ?? [])
                .filter(fqn => !packageFqnSet.has(fqn))
                .map(fqn => ({fqn, classCount: 0})),
        ];
        const rowDataMap = new Map(buildPackageTableRowData(allPackages, relations).map(r => [r.fqn, r]));
        const sortedPackages = [...allPackages].sort((a, b) => a.fqn.localeCompare(b.fqn));

        // ソート済みなので隣接する次のパッケージとのFQN前方一致で O(n) 判定
        const hasChildrenSet = new Set();
        for (let i = 0; i < sortedPackages.length - 1; i++) {
            if (sortedPackages[i + 1].fqn.startsWith(sortedPackages[i].fqn + '.')) {
                hasChildrenSet.add(sortedPackages[i].fqn);
            }
        }

        const fqnSet = new Set(sortedPackages.map(p => p.fqn));
        const collapsedSet = new Set(config.getCollapsedPackages());
        const tbody = document.createElement('tbody');

        function refreshCountDisplay(targetTr) {
            const toggleBtn = targetTr.querySelector('.explore-collapse-toggle');
            const isCollapsed = toggleBtn?.getAttribute('aria-expanded') === 'false';
            const fqn = targetTr.dataset.fqn;
            let classCount, incomingCount, outgoingCount;
            if (!isCollapsed) {
                ({classCount, incomingCount, outgoingCount} = rowDataMap.get(fqn));
            } else {
                classCount = 0; incomingCount = 0; outgoingCount = 0;
                const prefix = fqn + '.';
                rowDataMap.forEach((data, key) => {
                    if (key === fqn || key.startsWith(prefix)) {
                        classCount += data.classCount;
                        incomingCount += data.incomingCount;
                        outgoingCount += data.outgoingCount;
                    }
                });
            }
            targetTr.querySelector('td[data-count="class"]').textContent = String(classCount);
            targetTr.querySelector('td[data-count="incoming"]').textContent = String(incomingCount);
            targetTr.querySelector('td[data-count="outgoing"]').textContent = String(outgoingCount);
        }

        sortedPackages.forEach(pkg => {
            const tr = document.createElement('tr');
            tr.dataset.fqn = pkg.fqn;
            const rowData = rowDataMap.get(pkg.fqn);
            if (config.isSelected(pkg.fqn)) tr.classList.add('explore-target-selected');
            if ([...collapsedSet].some(c => pkg.fqn.startsWith(c + '.'))) {
                tr.classList.add('hidden-by-collapse');
            }

            tr.addEventListener('click', () => config.onRowClick(pkg.fqn));

            const toggleTd = document.createElement('td');
            if (hasChildrenSet.has(pkg.fqn)) {
                const toggleBtn = document.createElement('button');
                const initiallyCollapsed = collapsedSet.has(pkg.fqn);
                toggleBtn.type = 'button';
                toggleBtn.className = 'explore-collapse-toggle';
                toggleBtn.textContent = initiallyCollapsed ? '▶' : '▼';
                toggleBtn.setAttribute('aria-expanded', String(!initiallyCollapsed));
                toggleBtn.setAttribute('aria-label', initiallyCollapsed ? '配下を展開' : '配下を折りたたむ');
                toggleBtn.addEventListener('click', (e) => {
                    e.stopPropagation();
                    const collapsing = toggleBtn.getAttribute('aria-expanded') === 'true';
                    toggleBtn.setAttribute('aria-expanded', String(!collapsing));
                    toggleBtn.textContent = collapsing ? '▶' : '▼';
                    toggleBtn.setAttribute('aria-label', collapsing ? '配下を展開' : '配下を折りたたむ');
                    const childPrefix = pkg.fqn + '.';
                    tbody.querySelectorAll('tr[data-fqn]').forEach(childTr => {
                        if (childTr.dataset.fqn.startsWith(childPrefix)) {
                            childTr.classList.toggle('hidden-by-collapse', collapsing);
                        }
                    });
                    config.onCollapseChange(pkg.fqn, collapsing, childPrefix, tbody);
                    refreshCountDisplay(tr);
                    if (!collapsing) {
                        tbody.querySelectorAll('tr[data-fqn]').forEach(childTr => {
                            if (childTr.dataset.fqn.startsWith(childPrefix)) {
                                refreshCountDisplay(childTr);
                            }
                        });
                    }
                });
                toggleTd.appendChild(toggleBtn);
            }
            tr.appendChild(toggleTd);

            const {ancestor, relative} = getRelativeFqn(pkg.fqn, fqnSet);
            const depth = ancestor ? ancestor.split('.').length : 0;

            const fqnTd = document.createElement('td');
            fqnTd.textContent = relative;
            fqnTd.title = pkg.fqn;
            fqnTd.className = 'fqn';
            fqnTd.style.paddingLeft = `${depth * 16 + 4}px`;
            tr.appendChild(fqnTd);

            const nameTd = document.createElement('td');
            nameTd.textContent = getGlossaryTitle(pkg.fqn);
            tr.appendChild(nameTd);

            tr.appendChild(createNumberTd(rowData.classCount, 'class'));
            tr.appendChild(createNumberTd(rowData.incomingCount, 'incoming'));
            tr.appendChild(createNumberTd(rowData.outgoingCount, 'outgoing'));

            tbody.appendChild(tr);
        });

        Array.from(tbody.children).forEach(tr => {
            if (tr.querySelector('.explore-collapse-toggle')?.getAttribute('aria-expanded') === 'false') {
                refreshCountDisplay(tr);
            }
        });
        container.appendChild(tbody);

        const filterInput = config.getFilterInput();
        if (filterInput) {
            filterInput.addEventListener('input', () => {
                const filterText = filterInput.value.toLowerCase();
                tbody.querySelectorAll('tr[data-fqn]').forEach(tr => {
                    const matches = !filterText || tr.dataset.fqn.toLowerCase().includes(filterText);
                    tr.classList.toggle('hidden', !matches);
                });
            });
        }
    }

    function buildHierarchyListConfig(context) {
        const config = {
            getContainer: () => dom.getHierarchyPackageList(),
            getFilterInput: () => dom.getHierarchyListFilter(),
            getCollapsedPackages: () => context.hierarchyCollapsedPackages,
            isSelected: (fqn) => context.packageFilterFqn.includes(fqn),
            onRowClick: (fqn) => {
                if (context.packageFilterFqn.includes(fqn)) {
                    context.packageFilterFqn = context.packageFilterFqn.filter(p => p !== fqn);
                } else {
                    context.packageFilterFqn = [...context.packageFilterFqn, fqn];
                }
                renderHierarchyDiagramAndTable(context);
            },
            onCollapseChange: (fqn, collapsing) => {
                if (collapsing) {
                    context.hierarchyCollapsedPackages = [...context.hierarchyCollapsedPackages, fqn];
                } else {
                    context.hierarchyCollapsedPackages = context.hierarchyCollapsedPackages.filter(p => p !== fqn);
                }
                syncStateToURL();
            },
        };
        return config;
    }

    function buildExploreListConfig(context) {
        const config = {
            getContainer: () => dom.getExplorePackageList(),
            getFilterInput: () => dom.getExploreListFilter(),
            getCollapsedPackages: () => context.exploreCollapsedPackages,
            isSelected: (fqn) => context.exploreTargetPackages.includes(fqn),
            onRowClick: (fqn) => {
                if (context.exploreTargetPackages.includes(fqn)) {
                    context.exploreTargetPackages = context.exploreTargetPackages.filter(p => p !== fqn);
                } else {
                    context.exploreTargetPackages = [...context.exploreTargetPackages, fqn];
                }
                renderPackageList(config);
                renderExploreDiagram(context);
            },
            onCollapseChange: (fqn, collapsing, childPrefix, tbody) => {
                const selectedSet = new Set(context.exploreTargetPackages);
                if (collapsing) {
                    const childrenSelected = context.exploreTargetPackages.some(t => t.startsWith(childPrefix));
                    if (childrenSelected) {
                        context.exploreTargetPackages = [
                            ...context.exploreTargetPackages.filter(t => !t.startsWith(childPrefix)),
                            ...(selectedSet.has(fqn) ? [] : [fqn]),
                        ];
                        renderPackageList(config);
                    }
                    context.exploreCollapsedPackages = [...context.exploreCollapsedPackages, fqn];
                } else {
                    const childFqnsToAdd = [];
                    tbody.querySelectorAll('tr[data-fqn]').forEach(childTr => {
                        const childFqn = childTr.dataset.fqn;
                        if (childFqn.startsWith(childPrefix) && selectedSet.has(fqn) && !selectedSet.has(childFqn)) {
                            childFqnsToAdd.push(childFqn);
                        }
                    });
                    if (childFqnsToAdd.length > 0) {
                        context.exploreTargetPackages = [...context.exploreTargetPackages, ...childFqnsToAdd];
                        renderPackageList(config);
                    }
                    context.exploreCollapsedPackages = context.exploreCollapsedPackages.filter(p => p !== fqn);
                }
                const affectsSelection = context.exploreTargetPackages.some(t =>
                    t === fqn || t.startsWith(fqn + '.') || fqn.startsWith(t + '.')
                );
                if (affectsSelection) {
                    renderExploreDiagram(context);
                } else {
                    syncStateToURL();
                }
            },
        };
        return config;
    }

    function renderHierarchyPackageList(context) {
        renderPackageList(buildHierarchyListConfig(context));
    }

    function renderExplorePackageList(context) {
        renderPackageList(buildExploreListConfig(context));
    }

    function setupPackageFilterControl(context) {
        const clearPackageButton = dom.getClearPackageFilterButton();
        const resetButton = dom.getResetPackageFilterButton();

        const {domainPackageRoots} = getPackageRelationData(context);
        const defaultFqns = findDefaultPackageFilterCandidate(domainPackageRoots) ?? [];

        if (clearPackageButton) clearPackageButton.addEventListener('click', () => {
            context.packageFilterFqn = [];
            renderHierarchyDiagramAndTable(context);
        });
        if (resetButton) resetButton.addEventListener('click', () => {
            context.packageFilterFqn = defaultFqns;
            renderHierarchyDiagramAndTable(context);
        });
    }

    function applyDefaultPackageFilterIfPresent(context) {
        const {domainPackageRoots} = getPackageRelationData(context);
        const candidate = findDefaultPackageFilterCandidate(domainPackageRoots);
        if (!candidate || !candidate.length) return;

        context.packageFilterFqn = candidate;
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
            select.appendChild(Jig.dom.createElement('option', {
                textContent: option.text,
                attributes: {value: option.value},
            }));
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
        const callerRadios = dom.getExploreCallerModeRadios();
        const calleeRadios = dom.getExploreCalleeModeRadios();

        if (clearButton) {
            clearButton.addEventListener('click', () => {
                context.exploreTargetPackages = [];
                renderExplorePackageList(context);
                renderExploreDiagram(context);
            });
        }

        if (callerRadios.length > 0) {
            callerRadios.forEach(radio => {
                radio.checked = radio.value === context.exploreCallerMode;
                radio.addEventListener('change', () => {
                    if (!radio.checked) return;
                    context.exploreCallerMode = radio.value;
                    renderExploreDiagram(context);
                });
            });
        }

        if (calleeRadios.length > 0) {
            calleeRadios.forEach(radio => {
                radio.checked = radio.value === context.exploreCalleeMode;
                radio.addEventListener('change', () => {
                    if (!radio.checked) return;
                    context.exploreCalleeMode = radio.value;
                    renderExploreDiagram(context);
                });
            });
        }
    }

    function registerHierarchyDiagramClickHandler(context) {
        if (typeof window === 'undefined') return;
        window[HIERARCHY_DIAGRAM_CLICK_HANDLER_NAME] = function (nodeId) {
            const fqn = context.diagramNodeIdToFqn.get(nodeId);
            if (!fqn) return;
            context.packageFilterFqn = [fqn];
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

    function renderTab(tabName) {
        if (tabName === TAB.EXPLORE) {
            renderExploreDiagram(exploreState);
            renderExplorePackageList(exploreState);
        } else if (tabName === TAB.HIERARCHY) {
            renderHierarchyDiagramAndTable(hierarchyState);
        }
    }

    function setupTabControl(onTabActivated) {
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
                onTabActivated(tab.dataset.tab);
                syncStateToURL();
            });
        });
    }

    function syncStateToURL() {
        if (typeof window === 'undefined') return;
        const params = new URLSearchParams();
        const activeTab = document.querySelector('.package-mode-tabs .tab-button.is-active')?.dataset.tab;

        if (activeTab === TAB.EXPLORE) {
            params.set('tab', TAB.EXPLORE);
            exploreState.exploreTargetPackages.forEach(p => params.append('target', p));
            exploreState.exploreCollapsedPackages.forEach(p => params.append('collapsed', p));
            if (exploreState.exploreCallerMode !== '1') params.set('caller', exploreState.exploreCallerMode);
            if (exploreState.exploreCalleeMode !== '1') params.set('callee', exploreState.exploreCalleeMode);
        } else {
            if (hierarchyState.aggregationDepth !== 0) params.set('depth', hierarchyState.aggregationDepth);
            hierarchyState.packageFilterFqn.forEach(f => params.append('filter', f));
            hierarchyState.hierarchyCollapsedPackages.forEach(p => params.append('hcollapsed', p));
            if (!hierarchyState.transitiveReductionEnabled) params.set('reduction', 'false');
        }

        const queryString = params.toString();
        const newURL = queryString
            ? `${window.location.pathname}?${queryString}${window.location.hash}`
            : `${window.location.pathname}${window.location.hash}`;
        window.history.replaceState({}, '', newURL);
    }

    function loadStateFromURL() {
        if (typeof window === 'undefined') return;
        const params = new URLSearchParams(window.location.search);

        const depth = params.get('depth');
        if (depth !== null) hierarchyState.aggregationDepth = Number(depth);

        const filters = params.getAll('filter');
        if (filters.length > 0) hierarchyState.packageFilterFqn = filters;

        const hcollapsed = params.getAll('hcollapsed');
        if (hcollapsed.length > 0) hierarchyState.hierarchyCollapsedPackages = hcollapsed;

        const reduction = params.get('reduction');
        if (reduction === 'false') hierarchyState.transitiveReductionEnabled = false;

        const targets = params.getAll('target');
        if (targets.length > 0) exploreState.exploreTargetPackages = targets;

        const collapsed = params.getAll('collapsed');
        if (collapsed.length > 0) exploreState.exploreCollapsedPackages = collapsed;

        const caller = params.get('caller');
        if (caller) exploreState.exploreCallerMode = caller;

        const callee = params.get('callee');
        if (callee) exploreState.exploreCalleeMode = callee;

        const tab = params.get('tab');
        if (tab) {
            const tabButton = document.querySelector(`.package-mode-tabs .tab-button[data-tab="${tab}"]`);
            if (tabButton) tabButton.click();
        }
    }

    function init() {
        if (typeof document === 'undefined' || !document.body.classList.contains("package-relation")) return;
        Jig.dom.setupSortableTables();
        const renderedTabs = new Set();
        setupTabControl(tabName => {
            if (!renderedTabs.has(tabName)) {
                renderedTabs.add(tabName);
                renderTab(tabName);
            }
        });

        setupPackageFilterControl(hierarchyState);
        const {domainPackageRoots} = getPackageRelationData(hierarchyState);
        hierarchyState.aggregationDepth = getInitialAggregationDepth(domainPackageRoots);

        // 関連探索の初期化（loadStateFromURL より前に行い、タブクリック時の描画を可能にする）
        setupExploreControl(exploreState);
        registerExploreDiagramClickHandler(exploreState);

        loadStateFromURL();

        setupAggregationDepthControl(hierarchyState);
        setupTransitiveReductionControl(hierarchyState);
        registerHierarchyDiagramClickHandler(hierarchyState);

        if (hierarchyState.packageFilterFqn.length === 0) {
            applyDefaultPackageFilterIfPresent(hierarchyState);
        }

        // アクティブなタブのみ初期描画（loadStateFromURL のタブクリックで既に描画済みの場合はスキップ）
        const activeTabName = document.querySelector('.package-mode-tabs .tab-button.is-active')?.dataset.tab ?? TAB.HIERARCHY;
        if (!renderedTabs.has(activeTabName)) {
            renderedTabs.add(activeTabName);
            renderTab(activeTabName);
        }
    }

    return {
        init,
        hierarchyState,
        exploreState,
        HIERARCHY_DIAGRAM_CLICK_HANDLER_NAME,
        EXPLORE_DIAGRAM_CLICK_HANDLER_NAME,
        TAB,
        dom,

        syncStateToURL,
        loadStateFromURL,

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
        applyDefaultPackageFilterIfPresent,
        buildMutualDependencyItems,
        renderMutualDependencyList,
        buildMutualDependencyDiagramSource,
        renderHierarchyDiagram,
        renderHierarchyDiagramAndTable,
        buildHierarchyDiagramRenderPlan,
        buildCollapsedSubpackageMap,
        renderExploreDiagram,
        renderHierarchyPackageList,
        renderExplorePackageList,
        registerHierarchyDiagramClickHandler,
        registerExploreDiagramClickHandler,
        setupPackageFilterControl,
        setupAggregationDepthControl,
        renderAggregationDepthSelectOptions,
        buildAggregationDepthOptions,
        renderAggregationDepthOptionsIntoSelect,
        setupTransitiveReductionControl,
        setupExploreControl,
        setupTabControl,
        getRelativeFqn,
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
