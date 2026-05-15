const DomainApp = (() => {
    const Jig = globalThis.Jig;

    const domainSettings = {
        diagramDirection: 'TB',
        showDiagrams: true,
        showDescriptions: true,
        showDeprecatedNodes: true,
        showMembers: true,
        kindFilter: 'all',
        transitiveReductionEnabled: true,
        sidebarFilterText: '',
    };

    // ----- パッケージ・型データの構築 / 関連の集計 -----

    /**
     * domainPackageRoots と types からパッケージ一覧を構築する
     * @param {string[]} domainPackageRoots
     * @param {{fqn: string}[]} types
     * @returns {PackageType[]}
     */
    function buildPackages(domainPackageRoots, types) {
        const packageTypesMap = new Map();

        for (const type of types) {
            const lastDot = type.fqn.lastIndexOf('.');
            if (lastDot < 0) continue;
            const pkgFqn = type.fqn.substring(0, lastDot);

            Jig.util.pushToMap(packageTypesMap, pkgFqn, {fqn: type.fqn});

            // pkgFqn から domainPackageRoots に到達するまで、空の親パッケージも Map に登録しておく
            // （子パッケージのみを持つ中間パッケージのナビゲーションを成立させるため）
            let current = pkgFqn;
            while (!domainPackageRoots.includes(current)) {
                const parentDot = current.lastIndexOf('.');
                if (parentDot < 0) break;
                const parent = current.substring(0, parentDot);
                const isUnderRoot = domainPackageRoots.some(
                    root => parent === root || parent.startsWith(root + '.'));
                if (!isUnderRoot) break;
                if (!packageTypesMap.has(parent)) packageTypesMap.set(parent, []);
                current = parent;
            }
        }

        return Array.from(packageTypesMap.entries())
            .map(([fqn, pkgTypes]) => ({
                fqn,
                types: pkgTypes.sort((a, b) => a.fqn.localeCompare(b.fqn))
            }))
            .sort((a, b) => a.fqn.localeCompare(b.fqn));
    }

    /**
     * パッケージの直下の子パッケージを取得する
     * @param {PackageType} pkg
     * @param {Map<string, PackageType[]>} childPackagesMap
     * @returns {PackageType[]}
     */
    function getDirectChildPackages(pkg, childPackagesMap) {
        return childPackagesMap.get(pkg.fqn) ?? [];
    }

    /**
     * パッケージに含まれる kind の集合を返す（再帰的）
     * @param {PackageType} pkg
     * @param {Map<string, PackageType[]>} childPackagesMap
     * @param {Map<string, DomainType>} typesMap
     * @param {Map<string, Set<string>>} [memo] - 同一描画パスでの再計算を抑止するメモ
     * @returns {Set<string>}
     */
    function pkgKinds(pkg, childPackagesMap, typesMap, memo) {
        if (memo?.has(pkg.fqn)) return memo.get(pkg.fqn);
        const kinds = new Set();
        pkg.types.forEach(type => {
            const kind = typesMap?.get(type.fqn)?.kind;
            if (kind) kinds.add(kind);
        });
        getDirectChildPackages(pkg, childPackagesMap).forEach(childPkg => {
            pkgKinds(childPkg, childPackagesMap, typesMap, memo).forEach(k => kinds.add(k));
        });
        memo?.set(pkg.fqn, kinds);
        return kinds;
    }

    /**
     * 型間の関連からパッケージ間の関連を導出する（重複排除）
     * @returns {Array<{from: string, to: string}>}
     */
    function derivePackageRelations(typeRelations, typesMap) {
        const filteredRelations = typeRelations
            .filter(r => typesMap?.has(r.from) && typesMap?.has(r.to));
        const relMap = new Map();
        filteredRelations.forEach(({from, to}) => {
            const fromPkg = Jig.util.getPackageFqnFromTypeFqn(from);
            const toPkg = Jig.util.getPackageFqnFromTypeFqn(to);
            if (fromPkg !== toPkg) relMap.set(`${fromPkg}::${toPkg}`, {from: fromPkg, to: toPkg});
        });
        return Array.from(relMap.values());
    }

    // ----- Mermaid ダイアグラムソース生成 -----

    /**
     * パッケージカードに表示するパッケージ関連図（同階層の直接関連）
     * @param {{fqn: string}} pkg
     * @param {{from: string, to: string}[]} allPackageRelations
     * @param {string} direction
     * @returns {string|null}
     */
    function createPackageDirectRelationDiagram(pkg, allPackageRelations, direction = domainSettings.diagramDirection, showPhysicalName = false) {
        const directRelations = collectPackageDirectRelations(pkg, allPackageRelations);
        if (directRelations.length === 0) return null;

        const packageFqns = new Set([pkg.fqn]);
        directRelations.forEach(r => {
            packageFqns.add(r.from);
            packageFqns.add(r.to);
        });

        const {source} = Jig.mermaid.builder.buildMermaidDiagramSource(
            packageFqns, directRelations,
            {
                diagramDirection: direction,
                nodeClickUrlCallback: (fqn) => "#" + Jig.util.fqnToId("domain", fqn),
                focusedPackageFqn: pkg.fqn,
                showPhysicalName,
            }
        );
        return source;
    }

    function collectPackageDirectRelations(pkg, allPackageRelations) {
        return allPackageRelations.filter(r => r.from === pkg.fqn || r.to === pkg.fqn);
    }

    function hasPackageDirectRelationDiagram(pkg, allPackageRelations) {
        return collectPackageDirectRelations(pkg, allPackageRelations).length > 0;
    }

    /**
     * パッケージカードに表示するパッケージ内パッケージ関連図
     * @param pkg
     * @param allPackages
     * @param allPackageRelations
     * @param {string} direction
     * @return {string|null}
     */
    function createPackageRelationDiagram(pkg, allPackages, allPackageRelations, direction = domainSettings.diagramDirection, showPhysicalName = false) {
        const elements = collectPackageRelationDiagramElements(pkg, allPackages, allPackageRelations);
        if (!elements) return null;
        const {uniqueRelations, packageFqns} = elements;
        const {source} = Jig.mermaid.builder.buildMermaidDiagramSource(
            packageFqns,
            uniqueRelations,
            {
                diagramDirection: direction,
                nodeClickUrlCallback: (fqn) => "#" + Jig.util.fqnToId("domain", fqn),
                showPhysicalName,
            }
        );
        return source;
    }

    function collectPackageRelationDiagramElements(pkg, allPackages, allPackageRelations) {
        const {uniqueRelations, packageFqns} = Jig.mermaid.builder.buildVisibleDiagramRelations(
            allPackages,
            allPackageRelations,
            [],
            {
                packageFilterFqn: [pkg.fqn],
                aggregationDepth: pkg.fqn.split('.').length + 1,
                transitiveReductionEnabled: domainSettings.transitiveReductionEnabled
            }
        );
        if (packageFqns.size <= 1 || uniqueRelations.length === 0) return null;
        return {uniqueRelations, packageFqns};
    }

    function hasPackageRelationDiagram(pkg, allPackages, allPackageRelations) {
        return collectPackageRelationDiagramElements(pkg, allPackages, allPackageRelations) !== null;
    }

    /**
     * 型の直接関連エッジと関与 FQN セットを収集する（deprecated フィルタ適用済み）
     * @param {DomainType} type
     * @param {Array} typeRelations
     * @param {Map} typesMap
     * @param {{showOutgoing?: boolean, showIncoming?: boolean}} options
     * @returns {{edges: Array, involvedFqns: Set<string>} | null}
     */
    function collectTypeRelationEdges(type, typeRelations, typesMap, {showOutgoing = true, showIncoming = true} = {}) {
        const allRelations = typeRelations
            .filter(r => typesMap?.has(r.from) && typesMap?.has(r.to));

        const outgoing = allRelations.filter(r => r.from === type.fqn);
        const incoming = allRelations.filter(r => r.to === type.fqn);

        if (outgoing.length === 0 && incoming.length === 0) return null;

        const filteredOut = showOutgoing
            ? (domainSettings.showDeprecatedNodes ? outgoing : outgoing.filter(r => !typesMap?.get(r.to)?.isDeprecated))
            : [];
        const filteredIn = showIncoming
            ? (domainSettings.showDeprecatedNodes ? incoming : incoming.filter(r => !typesMap?.get(r.from)?.isDeprecated))
            : [];

        if (filteredOut.length === 0 && filteredIn.length === 0) {
            return (!showOutgoing || !showIncoming) ? {edges: [], involvedFqns: new Set([type.fqn])} : null;
        }

        const edgeMap = new Map();
        [...filteredOut, ...filteredIn].forEach(r => edgeMap.set(`${r.from}::${r.to}`, r));
        const edges = Array.from(edgeMap.values());

        const involvedFqns = new Set([type.fqn]);
        edges.forEach(r => { involvedFqns.add(r.from); involvedFqns.add(r.to); });

        return {edges, involvedFqns};
    }

    /**
     * クラスカードに表示するクラス関連図（このクラスと関連する全クラスを表示）
     * @param {DomainType} type
     * @param {Array} typeRelations
     * @param {Map} typesMap
     * @param {string} direction
     * @param {{showOutgoing?: boolean, showIncoming?: boolean}} options
     * @returns {string | null}
     */
    function createTypeRelationDiagram(type, typeRelations, typesMap, direction = domainSettings.diagramDirection, {showOutgoing = true, showIncoming = true, showPhysicalName = false} = {}) {
        const result = collectTypeRelationEdges(type, typeRelations, typesMap, {showOutgoing, showIncoming});
        if (!result) return null;
        const {edges, involvedFqns} = result;

        const fqnToMermaidId = (fqn) => Jig.util.fqnToId("n", fqn);
        const fqnToHtmlId = (fqn) => Jig.util.fqnToId("domain", fqn);
        const {type: typeLabel, pkg: pkgLabel} = Jig.glossary.makeLabels(showPhysicalName);

        function packageOf(fqn) {
            const idx = fqn.lastIndexOf('.');
            return idx < 0 ? null : fqn.substring(0, idx);
        }

        // パッケージごとにノードをグループ化
        const byPackage = new Map();
        involvedFqns.forEach(fqn => {
            const pkg = packageOf(fqn);
            Jig.util.pushToMap(byPackage, pkg, fqn);
        });
        const edgeLengthByKey = new Map();
        byPackage.forEach(fqns => {
            const {edgeLengthByKey: lengths} = Jig.mermaid.graph.computeOutboundEdgeLengths({
                nodesInSubgraph: fqns,
                edges: edges
            });
            lengths.forEach((length, key) => {
                const current = edgeLengthByKey.get(key) || 1;
                if (length > current) edgeLengthByKey.set(key, length);
            });
        });

        const selfId = fqnToMermaidId(type.fqn);
        const builder = new Jig.mermaid.Builder();
        byPackage.forEach((fqns, pkgFqn) => {
            if (pkgFqn) {
                const sg = builder.startSubgraph(Jig.util.fqnToId("sg", pkgFqn), pkgLabel(pkgFqn));
                fqns.forEach(fqn => builder.addNodeToSubgraph(sg, fqnToMermaidId(fqn), typeLabel(fqn)));
            } else {
                fqns.forEach(fqn => builder.addNode(fqnToMermaidId(fqn), typeLabel(fqn)));
            }
        });
        involvedFqns.forEach(fqn => builder.addClick(fqnToMermaidId(fqn), `#${fqnToHtmlId(fqn)}`));
        edges.forEach(r => {
            const edgeLength = edgeLengthByKey.get(`${r.from}::${r.to}`) || 1;
            builder.addEdge(fqnToMermaidId(r.from), fqnToMermaidId(r.to), "", false, edgeLength);
        });
        builder.addStyle(selfId, "font-weight:bold");

        return builder.build(direction);
    }

    /**
     * クラスカードに表示するクラス図（classDiagram形式、物理名ラベル）
     * @param {DomainType} type
     * @param {Array} typeRelations
     * @param {Map} typesMap
     * @param {string} direction
     * @param {{showOutgoing?: boolean, showIncoming?: boolean}} options
     * @returns {string | null}
     */
    const VISIBILITY_ORDER = ['PUBLIC', 'PROTECTED', 'PACKAGE', 'PRIVATE'];

    function createTypeClassDiagramSource(type, typeRelations, typesMap, direction = domainSettings.diagramDirection, {
        showOutgoing = true, showIncoming = true,
        showFields = true, showMethods = true,
        maxVisibility = 'PRIVATE',
        showPhysicalName = false
    } = {}) {
        const result = collectTypeRelationEdges(type, typeRelations, typesMap, {showOutgoing, showIncoming});
        if (!result) return null;
        const {edges, involvedFqns} = result;

        const fqnToNodeId = (fqn) => Jig.util.fqnToId("n", fqn);
        const fqnToHtmlId = (fqn) => Jig.util.fqnToId("domain", fqn);
        const {type: typeLabel} = Jig.glossary.makeLabels(showPhysicalName);

        function edgeTypeFromKinds(kinds) {
            if (!kinds) return 'dependency';
            if (kinds.includes('継承クラス')) return 'realization';
            if (kinds.includes('実装インタフェース')) return 'inheritance';
            if (kinds.includes('フィールド型') || kinds.includes('フィールド型引数')) return 'association';
            return 'dependency';
        }

        const maxVisibilityIndex = VISIBILITY_ORDER.indexOf(maxVisibility);
        const visibilityAllowed = (v) => VISIBILITY_ORDER.indexOf(v) <= maxVisibilityIndex;

        const builder = new Jig.mermaid.ClassDiagramBuilder();

        involvedFqns.forEach(fqn => {
            const nodeId = fqnToNodeId(fqn);
            builder.addClass(nodeId, typeLabel(fqn));

            const domainType = typesMap?.get(fqn);
            if (showFields) {
                (domainType?.fields || []).forEach(f => {
                    builder.addField(nodeId, typeNameWithGenerics(f.typeRef), f.name);
                });
            }
            if (showMethods) {
                (domainType?.methods || []).filter(m => visibilityAllowed(m.visibility)).forEach(m => {
                    const {name, params, returnType} = parseMethodInfo(m);
                    builder.addMethod(nodeId, m.visibility, name, params, returnType, false);
                });
                (domainType?.staticMethods || []).filter(m => visibilityAllowed(m.visibility)).forEach(m => {
                    const {name, params, returnType} = parseMethodInfo(m);
                    builder.addMethod(nodeId, m.visibility, name, params, returnType, true);
                });
            }

            builder.addClick(nodeId, `#${fqnToHtmlId(fqn)}`);
        });
        edges.forEach(r => builder.addEdge(fqnToNodeId(r.from), fqnToNodeId(r.to), edgeTypeFromKinds(r.kinds)));

        return builder.build(direction);
    }

    function hasTypeRelationDiagram(type, typeRelations, typesMap) {
        return collectTypeRelationEdges(type, typeRelations, typesMap) !== null;
    }

    function hasPackageTypeRelationDiagram(pkg, typesMap) {
        let pkgTypeFqns = new Set(pkg.types.map(t => t.fqn));
        if (pkgTypeFqns.size === 0) return false;
        if (!domainSettings.showDeprecatedNodes) {
            pkgTypeFqns = new Set([...pkgTypeFqns].filter(fqn => !typesMap?.get(fqn)?.isDeprecated));
        }
        return pkgTypeFqns.size > 0;
    }

    function typeNameWithGenerics(typeRef) {
        const baseName = Jig.glossary.typeSimpleName(typeRef.fqn);
        if (!typeRef.typeArgumentRefs?.length) return baseName;
        return `${baseName}~${typeRef.typeArgumentRefs.map(typeNameWithGenerics).join(', ')}~`;
    }

    function parseMethodInfo(method) {
        const hashIdx = method.fqn.lastIndexOf('#');
        const parenIdx = method.fqn.indexOf('(', hashIdx);
        const name = parenIdx > 0
            ? method.fqn.substring(hashIdx + 1, parenIdx)
            : method.fqn.substring(hashIdx + 1);
        const params = (method.parameters || []).map(p => typeNameWithGenerics(p.typeRef));
        const returnType = method.returnTypeRef ? typeNameWithGenerics(method.returnTypeRef) : '';
        return {name, params, returnType};
    }

    /**
     * パッケージカードに表示するパッケージ内クラス関連図
     * @param {PackageType} pkg
     * @param {Array} typeRelations
     * @param {Map} typesMap
     * @param {Object} options
     * @returns {string | null}
     */
    function createRelationDiagram(pkg, typeRelations, typesMap, {
        showExternalOutgoing = true,
        showExternalIncoming = true,
        direction = domainSettings.diagramDirection,
        showPhysicalName = false
    } = {}) {
        const fqnToMermaidId = (fqn) => Jig.util.fqnToId("n", fqn);
        const fqnToHtmlId = (fqn) => Jig.util.fqnToId("domain", fqn);

        const relations = typeRelations
            .filter(r => typesMap?.has(r.from) && typesMap?.has(r.to));

        let pkgTypeFqns = new Set(pkg.types.map(t => t.fqn));
        if (pkgTypeFqns.size === 0) return null;

        // Deprecated ノード非表示の場合、deprecated 型を除外
        if (!domainSettings.showDeprecatedNodes) {
            pkgTypeFqns = new Set([...pkgTypeFqns].filter(fqn => !typesMap?.get(fqn)?.isDeprecated));
            if (pkgTypeFqns.size === 0) return null;
        }

        // このパッケージの型から出る関連・入る関連
        const fromPkgRelations = relations.filter(r => pkgTypeFqns.has(r.from));
        const toPkgRelations = relations.filter(r => pkgTypeFqns.has(r.to) && !pkgTypeFqns.has(r.from));

        // 内部関連と外部関連に分類
        const internalRelations = fromPkgRelations.filter(r => pkgTypeFqns.has(r.to));

        const externalOutgoing = showExternalOutgoing
            ? fromPkgRelations.filter(r => !pkgTypeFqns.has(r.to))
            : [];
        const externalIncoming = showExternalIncoming
            ? toPkgRelations
            : [];

        function packageOf(fqn) {
            const idx = fqn.lastIndexOf('.');
            return idx < 0 ? fqn : fqn.substring(0, idx);
        }

        // 型が関連を持つ場合は関連から、ない場合はパッケージ内全型をノードにする
        const hasAnyRelation = fromPkgRelations.length > 0 || toPkgRelations.length > 0;
        const internalFqns = hasAnyRelation ? new Set() : pkgTypeFqns;

        if (hasAnyRelation) {
            fromPkgRelations.forEach(r => internalFqns.add(r.from));
            internalRelations.forEach(r => internalFqns.add(r.to));
            externalIncoming.forEach(r => internalFqns.add(r.to));
        }

        // 外部パッケージノード
        const externalPkgFqns = new Set();
        externalOutgoing.forEach(r => externalPkgFqns.add(packageOf(r.to)));
        externalIncoming.forEach(r => externalPkgFqns.add(packageOf(r.from)));

        // エッジ（重複排除）
        const allEdges = [
            ...internalRelations.map(r => ({from: r.from, to: r.to})),
            ...externalOutgoing.map(r => ({from: r.from, to: packageOf(r.to)})),
            ...externalIncoming.map(r => ({from: packageOf(r.from), to: r.to})),
        ];
        const uniqueEdgesMap = new Map();
        allEdges.forEach(e => {
            const key = `${e.from} --> ${e.to}`;
            uniqueEdgesMap.set(key, e);
        });

        let edges = Array.from(uniqueEdgesMap.values());
        if (domainSettings.transitiveReductionEnabled) {
            edges = Jig.mermaid.graph.transitiveReduction(edges);
        }

        const {edgeLengthByKey} = Jig.mermaid.graph.computeOutboundEdgeLengths({
            nodesInSubgraph: internalFqns,
            edges: edges
        });

        const {type: typeLabel, pkg: pkgLabel} = Jig.glossary.makeLabels(showPhysicalName);

        const builder = new Jig.mermaid.Builder();
        const sg = builder.startSubgraph(Jig.util.fqnToId("sg", pkg.fqn), pkgLabel(pkg.fqn), direction);
        internalFqns.forEach(fqn => builder.addNodeToSubgraph(sg, fqnToMermaidId(fqn), typeLabel(fqn)));
        externalPkgFqns.forEach(fqn => builder.addNode(fqnToMermaidId(fqn), pkgLabel(fqn), 'package'));
        [...internalFqns, ...externalPkgFqns].forEach(fqn =>
            builder.addClick(fqnToMermaidId(fqn), `#${fqnToHtmlId(fqn)}`)
        );
        edges.forEach(edge => {
            const edgeLength = edgeLengthByKey.get(`${edge.from}::${edge.to}`) || 1;
            builder.addEdge(fqnToMermaidId(edge.from), fqnToMermaidId(edge.to), "", false, edgeLength);
        });

        return builder.build(direction);
    }

    // ----- DOM レンダリング -----

    /**
     * @param {PackageType} pkg
     * @param {Map<string, PackageType[]>} childPackagesMap
     * @param {Map<string, DomainType>} typesMap
     * @param {boolean} isTopLevel
     * @returns {HTMLElement}
     */
    function renderPackageNavItem(pkg, childPackagesMap, typesMap, isTopLevel = false, kindsMemo = new Map()) {
        // 子が1つだけでタイプを持たないパッケージを統合して表示
        let currentPkg = pkg;
        const mergedNames = [Jig.glossary.getPackageTerm(pkg.fqn).title];

        while (true) {
            const childPackages = getDirectChildPackages(currentPkg, childPackagesMap);
            if (childPackages.length !== 1) break;
            if (currentPkg.types.length > 0) break;

            const childPkg = childPackages[0];
            mergedNames.push(Jig.glossary.getPackageTerm(childPkg.fqn).title);
            currentPkg = childPkg;
        }

        const childList = Jig.dom.createElement("ul", {className: "in-page-sidebar__links"});

        // 子パッケージを表示（統合後の currentPkg の直下のみ）
        const childPackages = getDirectChildPackages(currentPkg, childPackagesMap);
        childPackages.forEach(childPkg => {
            childList.appendChild(renderPackageNavItem(childPkg, childPackagesMap, typesMap, false, kindsMemo));
        });

        // 子タイプを表示
        currentPkg.types.forEach(child => {
            const domainType = typesMap?.get(child.fqn);
            const link = Jig.dom.createElement("a", {
                attributes: {href: "#" + Jig.util.fqnToId("domain", child.fqn)},
                className: "in-page-sidebar__link" + (domainType?.isDeprecated ? " deprecated" : ""),
                children: [
                    Jig.dom.kind.badgeElement("クラス"),
                    Jig.dom.createElement("span", {textContent: Jig.glossary.getTypeTerm(child.fqn).title})
                ]
            });
            childList.appendChild(Jig.dom.createElement("li", {
                className: "in-page-sidebar__item",
                children: [
                    Jig.dom.createElement("div", {
                        attributes: {"data-kind": domainType?.kind || ''},
                        children: [link]
                    })
                ]
            }));
        });

        const summaryLink = Jig.dom.createElement("a", {
            className: "in-page-sidebar__link",
            attributes: {href: "#" + Jig.util.fqnToId("domain", currentPkg.fqn)},
            children: [
                Jig.dom.kind.badgeElement("パッケージ"),
                Jig.dom.createElement("span", {textContent: mergedNames.join("/")})
            ]
        });
        const headerChildren = [summaryLink, Jig.dom.sidebar.createToggle(childList)];
        const wrapperAttrs = {"data-kind-children": [...pkgKinds(currentPkg, childPackagesMap, typesMap, kindsMemo)].join(' ')};

        if (isTopLevel) {
            return Jig.dom.createElement("section", {
                className: "in-page-sidebar__section",
                attributes: wrapperAttrs,
                children: [
                    Jig.dom.createElement("p", {
                        className: "in-page-sidebar__title in-page-sidebar__title--collapsible",
                        children: headerChildren
                    }),
                    childList
                ]
            });
        } else {
            return Jig.dom.createElement("li", {
                className: "in-page-sidebar__item",
                attributes: wrapperAttrs,
                children: [
                    Jig.dom.createElement("div", {
                        className: "in-page-sidebar__item-header",
                        children: headerChildren
                    }),
                    childList
                ]
            });
        }
    }

    /**
     * @param {PackageType[]} packages
     * @param {Map<string, PackageType[]>} childPackagesMap
     * @param {Map<string, DomainType>} typesMap
     * @returns {void}
     */
    function renderSidebar(packages, childPackagesMap, typesMap) {
        const container = document.getElementById("domain-sidebar-list");
        if (!container) return;
        container.innerHTML = "";

        // 直接の子パッケージ fqn の集合
        const childPackageFqns = new Set();
        packages.forEach(pkg => {
            getDirectChildPackages(pkg, childPackagesMap).forEach(child => {
                childPackageFqns.add(child.fqn);
            });
        });

        // トップレベルのパッケージのみを表示（直接の親を持たないもの）
        // pkgKinds の再帰計算結果は同一 render 中で何度も参照されるためメモ化を共有する
        const kindsMemo = new Map();
        packages.forEach(pkg => {
            if (!childPackageFqns.has(pkg.fqn)) {
                container.appendChild(renderPackageNavItem(pkg, childPackagesMap, typesMap, true, kindsMemo));
            }
        });
    }

    /**
     * @param {PackageType} pkg
     * @param {Map<string, PackageType[]>} childPackagesMap
     * @returns {HTMLElement | null}
     */
    function createChildrenTable(pkg, childPackagesMap) {
        const types = pkg.types;
        const childPackages = getDirectChildPackages(pkg, childPackagesMap);

        // 子パッケージ（▶︎ プレフィックス） + 子タイプ を合わせて表示
        const allChildren = [
            ...childPackages.map(childPkg => ({
                isPackage: true,
                fqn: childPkg.fqn,
                title: Jig.glossary.getPackageTerm(childPkg.fqn).title
            })),
            ...types.map(type => ({
                isPackage: false,
                fqn: type.fqn,
                title: Jig.glossary.getTypeTerm(type.fqn).title
            }))
        ];

        if (allChildren.length === 0) return null;

        const tbody = Jig.dom.createElement("tbody", {
            children: allChildren.map(child => {
                const kind = child.isPackage ? "パッケージ" : "クラス";
                const link = child.isPackage
                    ? Jig.dom.createElement("a", {
                        attributes: {href: "#" + Jig.util.fqnToId("domain", child.fqn)},
                        textContent: child.title
                    })
                    : Jig.dom.type.refElement({fqn: child.fqn});
                const cell = Jig.dom.createElement("td", {
                    children: [Jig.dom.kind.badgeElement(kind), link]
                });
                return Jig.dom.createElement("tr", {children: [cell]});
            })
        });

        return Jig.dom.createElement("table", {
            children: [
                Jig.dom.createElement("thead", {
                    children: [Jig.dom.createElement("tr", {children: [Jig.dom.createElement("th", {textContent: "名前"})]})]
                }),
                tbody
            ]
        });
    }


    /**
     * @param {{enumInfo: EnumInfo | undefined, fqn: string}} type
     * @returns {HTMLElement | null}
     */
    function createEnumSection(type) {
        if (!type.enumInfo) return null;

        const constants = type.enumInfo.constants;
        const dl = Jig.dom.createElement("dl", {
            children: constants.flatMap(constant => {
                const nodes = [Jig.dom.createElement("dt", {textContent: constant.name})];
                const term = Jig.glossary.getFieldTerm(`${type.fqn}#${constant.name}`);
                // 取れたかどうかに関わらず異なる場合のみ出す
                if (term && term.title !== constant.name) {
                    nodes.push(Jig.dom.createElement("dd", {textContent: term.title}));
                }
                return nodes;
            })
        });

        const section = Jig.dom.card.item({title: "列挙値"});
        section.appendChild(dl);

        const parameterNames = type.enumInfo.parameterNames;
        if (parameterNames.length) {
            const thead = Jig.dom.createElement("thead", {
                children: [Jig.dom.createElement("tr", {
                    children: [
                        Jig.dom.createElement("th", {textContent: "列挙定数名"}),
                        ...parameterNames.map(name => Jig.dom.createElement("th", {textContent: name}))
                    ]
                })]
            });
            const tbody = Jig.dom.createElement("tbody", {
                children:
                    constants.map(constant => Jig.dom.createElement("tr", {
                        children: [
                            Jig.dom.createElement("td", {className: "method-name", textContent: constant.name}),
                            ...constant.params.map(param => Jig.dom.createElement("td", {textContent: param}))
                        ]
                    }))
            });

            section.appendChild(Jig.dom.createElement("details", {
                children: [
                    Jig.dom.createElement("summary", {textContent: "列挙引数"}),
                    Jig.dom.createElement("table", {className: "fields", children: [thead, tbody]})
                ]
            }));
        }

        return section;
    }

    function registerDiagramPanel(panel, diagramDef) {
        return Jig.mermaid.diagram.createAndRegister(panel, (container) => {
            renderDiagram(container, diagramDef);
        });
    }

    /**
     * クラスカードに表示する関連クラス一覧
     * @param {DomainType} type
     * @param {Array} typeRelations
     * @param {Map} typesMap
     * @returns {HTMLElement | null}
     */
    function createRelatedClassesList(type, typeRelations, typesMap) {
        const allRelations = typeRelations
            .filter(r => typesMap?.has(r.from) && typesMap?.has(r.to));

        const outgoingFqns = allRelations
            .filter(r => r.from === type.fqn)
            .map(r => r.to);
        const incomingFqns = allRelations
            .filter(r => r.to === type.fqn)
            .map(r => r.from);

        if (outgoingFqns.length === 0 && incomingFqns.length === 0) return null;

        const detailsContent = [];

        if (outgoingFqns.length > 0) {
            detailsContent.push(Jig.dom.createElement("h4", {textContent: `参照するクラス (${outgoingFqns.length})`}));
            detailsContent.push(Jig.dom.createElement("ul", {
                children: outgoingFqns.map(fqn =>
                    Jig.dom.createElement("li", {children: [Jig.dom.type.refElement({fqn})]})
                )
            }));
        }

        if (incomingFqns.length > 0) {
            detailsContent.push(Jig.dom.createElement("h4", {textContent: `参照されるクラス (${incomingFqns.length})`}));
            detailsContent.push(Jig.dom.createElement("ul", {
                children: incomingFqns.map(fqn =>
                    Jig.dom.createElement("li", {children: [Jig.dom.type.refElement({fqn})]})
                )
            }));
        }

        const card = Jig.dom.card.item();
        card.appendChild(Jig.dom.createElement("details", {
            children: [
                Jig.dom.createElement("summary", {textContent: "関連情報"}),
                ...detailsContent
            ]
        }));
        return card;
    }

    function setupTypeDiagramPanel(panel, type, typeRelations, typesMap, diagramType) {
        const outgoingCheckbox = Jig.dom.createElement("input", {
            attributes: {type: "checkbox", class: "type-relation-outgoing"}
        });
        outgoingCheckbox.checked = true;
        const incomingCheckbox = Jig.dom.createElement("input", {
            attributes: {type: "checkbox", class: "type-relation-incoming"}
        });
        incomingCheckbox.checked = true;
        panel.appendChild(Jig.dom.createElement("fieldset", {
            className: "diagram-panel-options",
            children: [
                Jig.dom.createElement("legend", {textContent: "表示"}),
                Jig.dom.createElement("label", {
                    className: "diagram-panel-option",
                    children: [incomingCheckbox, "関連元"]
                }),
                Jig.dom.createElement("label", {
                    className: "diagram-panel-option",
                    children: [outgoingCheckbox, "関連先"]
                }),
            ]
        }));

        const extraControls = [];
        let getClassDefOptions = () => ({});

        if (diagramType === 'classDefinition') {
            const showFieldsCheckbox = Jig.dom.createElement("input", {attributes: {type: "checkbox"}});
            showFieldsCheckbox.checked = true;
            const showMethodsCheckbox = Jig.dom.createElement("input", {attributes: {type: "checkbox"}});
            showMethodsCheckbox.checked = true;
            panel.appendChild(Jig.dom.createElement("fieldset", {
                className: "diagram-panel-options",
                children: [
                    Jig.dom.createElement("legend", {textContent: "メンバ"}),
                    Jig.dom.createElement("label", {
                        className: "diagram-panel-option",
                        children: [showFieldsCheckbox, "フィールド"]
                    }),
                    Jig.dom.createElement("label", {
                        className: "diagram-panel-option",
                        children: [showMethodsCheckbox, "メソッド"]
                    }),
                ]
            }));

            const visibilityRadioGroupName = `visibility-filter-${Math.random().toString(36).slice(2)}`;
            const visibilityRadios = VISIBILITY_ORDER.map((v, i) => {
                const radio = Jig.dom.createElement("input", {
                    attributes: {type: "radio", name: visibilityRadioGroupName, value: v}
                });
                radio.checked = i === VISIBILITY_ORDER.length - 1;
                return radio;
            });
            const visibilityLabels = ['PUBLIC のみ', 'PROTECTED 以上', 'PACKAGE 以上', 'すべて'];
            panel.appendChild(Jig.dom.createElement("fieldset", {
                className: "diagram-panel-options",
                children: [
                    Jig.dom.createElement("legend", {textContent: "可視性"}),
                    ...visibilityRadios.map((radio, i) => Jig.dom.createElement("label", {
                        className: "diagram-panel-option",
                        children: [radio, visibilityLabels[i]]
                    }))
                ]
            }));

            extraControls.push(showFieldsCheckbox, showMethodsCheckbox, ...visibilityRadios);
            getClassDefOptions = () => ({
                showFields: showFieldsCheckbox.checked,
                showMethods: showMethodsCheckbox.checked,
                maxVisibility: visibilityRadios.find(r => r.checked)?.value ?? 'PRIVATE'
            });
        }

        const render = (container) => {
            renderDiagram(container, {
                pkg: undefined, type, diagramType, typeRelations, typesMap,
                showOutgoing: outgoingCheckbox.checked,
                showIncoming: incomingCheckbox.checked,
                ...getClassDefOptions()
            });
        };
        const container = Jig.mermaid.diagram.createAndRegister(panel, render);
        [outgoingCheckbox, incomingCheckbox, ...extraControls].forEach(el => {
            el.addEventListener('change', () => render(container));
        });
    }

    function setupPackageTypeDiagramPanel(panel, pkg, typeRelations, typesMap) {
        const outgoingCheckbox = Jig.dom.createElement("input", {
            attributes: {type: "checkbox", class: "class-relation-external-outgoing"}
        });
        outgoingCheckbox.checked = true;
        const incomingCheckbox = Jig.dom.createElement("input", {
            attributes: {type: "checkbox", class: "class-relation-external-incoming"}
        });
        incomingCheckbox.checked = true;
        panel.appendChild(Jig.dom.createElement("fieldset", {
            className: "diagram-panel-options",
            children: [
                Jig.dom.createElement("legend", {textContent: "表示"}),
                Jig.dom.createElement("label", {
                    className: "diagram-panel-option",
                    children: [incomingCheckbox, "関連元"]
                }),
                Jig.dom.createElement("label", {
                    className: "diagram-panel-option",
                    children: [outgoingCheckbox, "関連先"]
                }),
            ]
        }));

        const render = (container) => {
            renderDiagram(container, {pkg, type: undefined, diagramType: 'type', typeRelations, typesMap});
        };
        const container = Jig.mermaid.diagram.createAndRegister(panel, render);
        outgoingCheckbox.addEventListener('change', () => render(container));
        incomingCheckbox.addEventListener('change', () => render(container));
    }

    function appendConfiguredTabs(cardSection, tabConfigs, options = {}) {
        const enabledTabs = tabConfigs.filter(tab => tab.enabled);
        if (enabledTabs.length === 0) return null;
        const {className, initialActiveId, onTabChange} = options;
        const fullClassName = ["jig-card-section", "tab-content-section", className].filter(Boolean).join(" ");
        const tabDefs = enabledTabs.map(({id, label}) => ({id, label}));
        const tabSection = Jig.dom.tab.buildSection(tabDefs, {
            className: fullClassName,
            initialActiveId,
            onTabChange
        });
        cardSection.appendChild(tabSection.section);
        enabledTabs.forEach(tab => tab.setup(tabSection.panels[tab.id]));
        return tabSection;
    }

    /**
     * @param {PackageType[]} packages
     * @param {Array} typeRelations
     * @param {Map} typesMap
     * @param {Array} allPackageRelations
     * @param {Map<string, PackageType[]>} childPackagesMap
     * @param {HTMLElement} container
     * @returns {void}
     */
    function renderPackages(packages, typeRelations, typesMap, allPackageRelations, childPackagesMap, container) {
        if (packages.length === 0) return;

        const allPackages = Jig.data.domain.getPackages();

        packages.forEach(pkg => {
            const pkgTerm = Jig.glossary.getPackageTerm(pkg.fqn);
            const section = Jig.dom.card.type({
                id: Jig.util.fqnToId("domain", pkg.fqn),
                title: pkgTerm.title,
                fqn: pkg.fqn,
                kind: "パッケージ",
                attributes: {"data-kind-children": [...pkgKinds(pkg, childPackagesMap, typesMap)].join(' ')}
            });

            const pkgDescription = pkgTerm.description;
            if (pkgDescription) {
                section.appendChild(Jig.dom.createElement("section", {
                    className: "jig-card-section description",
                    children: [Jig.dom.createMarkdownElement(pkgDescription)]
                }));
            }

            const childrenTable = createChildrenTable(pkg, childPackagesMap);
            if (childrenTable) {
                section.appendChild(childrenTable);
            }

            appendConfiguredTabs(section, [
                {
                    id: 'direct',
                    label: 'パッケージ関連図',
                    enabled: hasPackageDirectRelationDiagram(pkg, allPackageRelations),
                    setup: panel => registerDiagramPanel(panel, {
                        pkg,
                        type: undefined,
                        diagramType: 'packageDirect',
                        allPackageRelations
                    })
                },
                {
                    id: 'inner-pkg',
                    label: 'パッケージ内パッケージ関連図',
                    enabled: hasPackageRelationDiagram(pkg, allPackages, allPackageRelations),
                    setup: panel => registerDiagramPanel(panel, {
                        pkg,
                        type: undefined,
                        diagramType: 'package',
                        allPackages,
                        allPackageRelations,
                        typeRelations,
                        typesMap
                    })
                },
                {
                    id: 'inner-class',
                    label: 'パッケージ内クラス関連図',
                    enabled: hasPackageTypeRelationDiagram(pkg, typesMap),
                    setup: panel => setupPackageTypeDiagramPanel(panel, pkg, typeRelations, typesMap)
                },
            ], {className: "tab-diagram-section"});

            container.appendChild(section);
        });
    }

    /**
     * @param {DomainType[]} types
     * @param {Array} typeRelations
     * @param {Map} typesMap
     * @param {HTMLElement} container
     * @returns {void}
     */
    function renderTypes(types, typeRelations, typesMap, container) {
        if (types.length === 0) return;

        types.forEach(type => {
            const titleSpan = Jig.dom.createElement("span", {
                textContent: Jig.glossary.getTypeTerm(type.fqn).title,
                className: type.isDeprecated ? "deprecated" : ""
            });

            const lastDot = type.fqn.lastIndexOf('.');
            const packageFqn = lastDot > 0 ? type.fqn.substring(0, lastDot) : null;
            const fqnDiv = Jig.dom.createElement("div", {className: "fully-qualified-name"});
            if (packageFqn) {
                fqnDiv.appendChild(Jig.dom.createElement("a", {
                    textContent: packageFqn,
                    attributes: {href: "#" + Jig.util.fqnToId("domain", packageFqn)}
                }));
                fqnDiv.append("." + type.fqn.substring(lastDot + 1));
            } else {
                fqnDiv.textContent = type.fqn;
            }

            const section = Jig.dom.card.type({
                id: Jig.util.fqnToId("domain", type.fqn),
                title: titleSpan,
                fqn: fqnDiv,
                kind: "クラス",
                attributes: {"data-kind": type.kind || ''}
            });

            const typeDescription = Jig.glossary.getTypeTerm(type.fqn).description;
            if (typeDescription) {
                section.appendChild(Jig.dom.createElement("section", {
                    className: "jig-card-section description",
                    children: [Jig.dom.createMarkdownElement(typeDescription)]
                }));
            }

            if (type.enumInfo) {
                section.appendChild(createEnumSection(type));
            }

            const fieldsList = Jig.dom.type.fieldsList(type.fields, {showTitle: false});
            const methodList = Jig.dom.type.methodsList("メソッド", type.methods, {showTitle: false});
            const staticList = Jig.dom.type.methodsList("staticメソッド", type.staticMethods, {showTitle: false});
            const hasTypeRelation = hasTypeRelationDiagram(type, typeRelations, typesMap);

            appendConfiguredTabs(section, [
                {
                    id: 'fields',
                    label: 'フィールド',
                    enabled: Boolean(fieldsList),
                    setup: panel => panel.appendChild(fieldsList)
                },
                {
                    id: 'methods',
                    label: 'メソッド',
                    enabled: Boolean(methodList),
                    setup: panel => panel.appendChild(methodList)
                },
                {
                    id: 'static-methods',
                    label: 'staticメソッド',
                    enabled: Boolean(staticList),
                    setup: panel => panel.appendChild(staticList)
                },
            ], {className: "tab-member-section"});

            appendConfiguredTabs(section, [
                {
                    id: 'relation',
                    label: 'クラス関連図',
                    enabled: hasTypeRelation,
                    setup: panel => setupTypeDiagramPanel(panel, type, typeRelations, typesMap, 'classDirect')
                },
                {
                    id: 'classdiag',
                    label: 'クラス図',
                    enabled: hasTypeRelation,
                    setup: panel => setupTypeDiagramPanel(panel, type, typeRelations, typesMap, 'classDefinition')
                },
            ], {className: "tab-diagram-section"});

            const relatedList = createRelatedClassesList(type, typeRelations, typesMap);
            if (relatedList) section.appendChild(relatedList);

            container.appendChild(section);
        });
    }

    /**
     * 指定されたダイアグラムを再生成
     * @param {HTMLElement} container
     * @param {Object} diagram - {pkg, type, diagramType, allPackages?, allPackageRelations?}
     */
    function renderDiagram(container, diagram) {
        const {pkg, type, diagramType, allPackages, allPackageRelations, typeRelations, typesMap, showOutgoing = true, showIncoming = true, showFields = true, showMethods = true, maxVisibility = 'PRIVATE'} = diagram;

        container.innerHTML = "";

        const renderIfNonNull = (generator, renderOptions = {}) => {
            if (generator(domainSettings.diagramDirection)) {
                Jig.mermaid.render.renderWithControls(container, generator, {direction: domainSettings.diagramDirection, ...renderOptions});
            }
        };

        if (diagramType === 'packageDirect') {
            renderIfNonNull(
                (dir, opts) => createPackageDirectRelationDiagram(pkg, allPackageRelations, dir, opts?.showPhysicalName),
                {enableLabelToggle: true}
            );
        } else if (diagramType === 'package') {
            renderIfNonNull(
                (dir, opts) => createPackageRelationDiagram(pkg, allPackages, allPackageRelations, dir, opts?.showPhysicalName),
                {enableLabelToggle: true}
            );
        } else if (diagramType === 'classDirect') {
            renderIfNonNull(
                (dir, opts) => createTypeRelationDiagram(type, typeRelations, typesMap, dir, {showOutgoing, showIncoming, showPhysicalName: opts?.showPhysicalName}),
                {enableLabelToggle: true}
            );
        } else if (diagramType === 'classDefinition') {
            renderIfNonNull(
                (dir, opts) => createTypeClassDiagramSource(type, typeRelations, typesMap, dir, {showOutgoing, showIncoming, showFields, showMethods, maxVisibility, showPhysicalName: opts?.showPhysicalName}),
                {enableLabelToggle: true}
            );
        } else if (diagramType === 'type') {
            const panel = typeof container.closest === 'function' ? container.closest('.jig-tab-panel') : null;
            const outgoing = panel?.querySelector('.class-relation-external-outgoing');
            const incoming = panel?.querySelector('.class-relation-external-incoming');
            const showExternalOutgoing = outgoing ? outgoing.checked : true;
            const showExternalIncoming = incoming ? incoming.checked : true;
            renderIfNonNull(
                (dir, opts) => createRelationDiagram(pkg, typeRelations, typesMap, {showExternalOutgoing, showExternalIncoming, direction: dir, showPhysicalName: opts?.showPhysicalName}),
                {enableLabelToggle: true}
            );
        }
    }


    // ----- 表示設定とイベント / 初期化 -----

    /**
     * @returns {void}
     */
    function applyVisibilitySettings() {
        const main = document.getElementById('domain-main');
        if (!main) return;

        main.querySelectorAll('.tab-member-section').forEach(section => {
            section.style.display = domainSettings.showMembers ? '' : 'none';
        });

        const selectedKind = domainSettings.kindFilter;
        const sidebar = document.getElementById('domain-sidebar');

        const packageItemDisplay = (item) => {
            if (selectedKind === 'all' || selectedKind === 'パッケージ') return '';
            return item.dataset.kindChildren.split(' ').includes(selectedKind) ? '' : 'none';
        };
        const typeItemDisplay = (item) => {
            if (selectedKind === 'all') return '';
            if (selectedKind === 'パッケージ') return 'none';
            return item.dataset.kind === selectedKind ? '' : 'none';
        };

        main.querySelectorAll('section.jig-card--type[data-kind-children]').forEach(section => {
            section.style.display = packageItemDisplay(section);
        });
        main.querySelectorAll('section.jig-card--type[data-kind]').forEach(section => {
            section.style.display = typeItemDisplay(section);
        });

        if (sidebar) {
            sidebar.querySelectorAll('[data-kind-children]').forEach(item => {
                item.style.display = packageItemDisplay(item);
            });
            sidebar.querySelectorAll('div[data-kind]').forEach(div => {
                div.closest('li').style.display = typeItemDisplay(div);
            });
        }
        applySidebarTextFilter();
    }

    function applySidebarTextFilter() {
        const filterText = domainSettings.sidebarFilterText.toLowerCase();
        if (!filterText) return;

        const sidebar = document.getElementById('domain-sidebar');
        if (!sidebar) return;

        sidebar.querySelectorAll('div[data-kind]').forEach(div => {
            const link = div.querySelector('a');
            const text = link ? (link.querySelector('span:last-child')?.textContent ?? link.textContent).toLowerCase() : '';
            div.closest('li').style.display = text.includes(filterText) ? '' : 'none';
        });

        [...sidebar.querySelectorAll('[data-kind-children]')]
            .reverse()
            .forEach(item => {
                const link = item.querySelector('a');
                const packageText = link ? (link.querySelector('span:last-child')?.textContent ?? link.textContent).toLowerCase() : '';
                const packageMatches = packageText.includes(filterText);
                const childList = item.querySelector('ul');
                const hasVisible = childList && [...childList.children].some(child =>
                    child.style.display !== 'none'
                );
                item.style.display = (packageMatches || hasVisible) ? '' : 'none';
            });
    }

    /**
     * @returns {void}
     */
    function initSettings() {
        const reductionCheckbox = document.getElementById('transitive-reduction-toggle');
        if (reductionCheckbox) reductionCheckbox.checked = domainSettings.transitiveReductionEnabled;

        document.querySelectorAll('input[name="kind-filter"]').forEach(radio => {
            radio.addEventListener('change', () => {
                if (radio.checked) {
                    domainSettings.kindFilter = radio.value;
                    applyVisibilitySettings();
                }
            });
        });

        [
            {id: 'show-deprecated-nodes',       key: 'showDeprecatedNodes',        after: () => Jig.mermaid.diagram.rerenderVisible()},
            {id: 'transitive-reduction-toggle', key: 'transitiveReductionEnabled', after: () => Jig.mermaid.diagram.rerenderVisible()},
            {id: 'show-diagrams',               key: 'showDiagrams',               after: v => document.body.classList.toggle('hide-domain-diagrams', !v)},
            {id: 'show-descriptions',           key: 'showDescriptions',           after: v => document.body.classList.toggle('hide-domain-descriptions', !v)},
            {id: 'show-members',                key: 'showMembers',                after: applyVisibilitySettings},
        ].forEach(({id, key, after}) => {
            const el = document.getElementById(id);
            if (!el) return;
            el.addEventListener('change', () => {
                domainSettings[key] = el.checked;
                after(el.checked);
            });
        });

        Jig.dom.sidebar.initTextFilter('domain-sidebar-filter', text => {
            domainSettings.sidebarFilterText = text;
            applyVisibilitySettings();
        });

        Jig.dom.sidebar.initCollapseBtn();
    }

    /**
     * @returns {void}
     */
    function init() {
        Jig.data.resetCache();

        const data = Jig.data.domain.get();
        if (!data) {
            const main = document.getElementById("domain-main");
            if (main) {
                main.appendChild(Jig.dom.createElement("p", {
                    className: "jig-data-error",
                    textContent: "ドメインデータ（domain-data.js）が読み込まれていません。JIG を実行してデータファイルを生成してください。"
                }));
            }
            return;
        }

        initSettings();

        // types を FQN → type の Map にインデックス化（O(n) → O(1) 検索）
        // Jig.data.domain 内部のメモ化に保持する
        const typesMap = Jig.data.domain.getTypesMap();

        // domainPackageRoots と types からパッケージを構築
        const packages = buildPackages(data.domainPackageRoots, data.types);
        Jig.data.domain.setPackages(packages);

        // packages の直下の子を事前計算（O(n) → O(1) 取得）
        const childrenMap = new Map(packages.map(p => [p.fqn, []]));
        packages.forEach(p => {
            const parentFqn = p.fqn.substring(0, p.fqn.lastIndexOf('.'));
            if (childrenMap.has(parentFqn)) {
                childrenMap.get(parentFqn).push(p);
            }
        });
        Jig.data.domain.setChildPackagesMap(childrenMap);

        // typeRelations を一度解決（以降は引数経由で渡す）
        const rawRelations = Jig.data.typeRelations.getRelations();
        const typeRelations = rawRelations.filter(r => typesMap?.has(r.from) && typesMap?.has(r.to));
        const allPackageRelations = derivePackageRelations(typeRelations, typesMap);

        const childPackagesMap = Jig.data.domain.getChildPackagesMap();


        renderSidebar(packages, childPackagesMap, typesMap);

        const main = document.getElementById("domain-main");
        if (!main) return;
        main.innerHTML = "";

        // optional データの警告表示
        const warnings = [];
        if (!Jig.data.glossary.get()) {
            warnings.push("用語集（glossary-data.js）が読み込まれていません");
        }
        if (!Jig.data.typeRelations.get()) {
            warnings.push("型関連情報（type-relations-data.js）が読み込まれていません");
        }

        if (warnings.length > 0) {
            warnings.forEach(warning => {
                main.appendChild(Jig.dom.createElement("p", {
                    className: "jig-data-warning",
                    textContent: warning + "。一部の情報が表示されない可能性があります。"
                }));
            });
        }

        renderPackages(packages, typeRelations, typesMap, allPackageRelations, childPackagesMap, main);
        renderTypes(data.types, typeRelations, typesMap, main);
    }

    return {
        init,
        renderPackageNavItem,
        getDirectChildPackages,
        createRelationDiagram,
        createTypeRelationDiagram,
        createTypeClassDiagramSource,
        createPackageRelationDiagram,
        createPackageDirectRelationDiagram,
        buildPackages,
        derivePackageRelations
    };
})();

Jig.bootstrap.register("domain-model", DomainApp.init);

if (typeof module !== "undefined" && module.exports) {
    module.exports = DomainApp;
}
