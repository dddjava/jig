const DomainApp = (() => {
    const Jig = globalThis.Jig;

    const domainSettings = {
        diagramDirection: 'TB',
        showDiagrams: true,
        showDeprecatedNodes: true,
        showFields: true,
        showMethods: true,
        showStaticMethods: true,
        showEnumOnly: false,
        transitiveReductionEnabled: true,
        sidebarFilterText: '',
    };

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

            let current = pkgFqn;
            while (true) {
                if (domainPackageRoots.includes(current)) break;
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
     * パッケージが enum 型を含むかを判定する（再帰的）
     * @param {PackageType} pkg
     * @param {Map<string, PackageType[]>} childPackagesMap
     * @param {Map<string, DomainType>} typesMap
     * @returns {boolean}
     */
    function pkgHasEnum(pkg, childPackagesMap, typesMap) {
        // このパッケージのタイプに enum があるか
        if (pkg.types.some(type => typesMap?.get(type.fqn)?.enumInfo)) {
            return true;
        }
        // 子パッケージに enum があるか
        const childPackages = getDirectChildPackages(pkg, childPackagesMap);
        return childPackages.some(childPkg => pkgHasEnum(childPkg, childPackagesMap, typesMap));
    }

    /**
     * @param {PackageType} pkg
     * @param {Map<string, PackageType[]>} childPackagesMap
     * @param {Map<string, DomainType>} typesMap
     * @returns {HTMLElement}
     */
    function renderPackageNavItem(pkg, childPackagesMap, typesMap) {
        // 子が1つだけでタイプを持たないパッケージを統合して表示
        let currentPkg = pkg;
        const mergedNames = [Jig.glossary.getTypeTerm(pkg.fqn).title];

        while (true) {
            const childPackages = getDirectChildPackages(currentPkg, childPackagesMap);
            if (childPackages.length !== 1) break;
            if (currentPkg.types.length > 0) break;

            const childPkg = childPackages[0];
            mergedNames.push(Jig.glossary.getTypeTerm(childPkg.fqn).title);
            currentPkg = childPkg;
        }

        const summaryLink = Jig.dom.createElement("a", {
            attributes: {href: "#" + Jig.util.fqnToId("domain", currentPkg.fqn)},
            textContent: mergedNames.join("/")
        });
        const details = Jig.dom.createElement("details", {
            attributes: {
                open: "",
                "data-has-enum-children": pkgHasEnum(currentPkg, childPackagesMap, typesMap) ? "true" : "false"
            },
            children: [
                Jig.dom.createElement("summary", {
                    className: "package",
                    children: [summaryLink, "/"]
                })
            ]
        });

        // 子パッケージを表示（統合後の currentPkg の直下のみ）
        const childPackages = getDirectChildPackages(currentPkg, childPackagesMap);
        childPackages.forEach(childPkg => {
            details.appendChild(renderPackageNavItem(childPkg, childPackagesMap, typesMap));
        });

        // 子タイプを表示
        currentPkg.types.forEach(child => {
            const domainType = typesMap?.get(child.fqn);
            const link = Jig.dom.createElement("a", {
                attributes: {href: "#" + Jig.util.fqnToId("domain", child.fqn)},
                className: domainType?.isDeprecated ? "deprecated" : "",
                textContent: Jig.glossary.getTypeTerm(child.fqn).title
            });
            details.appendChild(Jig.dom.createElement("div", {
                attributes: {"data-has-enum": domainType?.enumInfo ? "true" : "false"},
                children: [link]
            }));
        });

        return details;
    }

    /**
     * パッケージカードに表示するパッケージ関連図（同階層の直接関連）
     * @param {{fqn: string}} pkg
     * @param {{from: string, to: string}[]} allPackageRelations
     * @param {string} direction
     * @returns {string|null}
     */
    function createPackageDirectRelationDiagram(pkg, allPackageRelations, direction = domainSettings.diagramDirection) {
        const directRelations = allPackageRelations.filter(r => r.from === pkg.fqn || r.to === pkg.fqn);
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
            }
        );
        return source;
    }

    /**
     * パッケージカードに表示するパッケージ内パッケージ関連図
     * @param pkg
     * @param allPackages
     * @param allPackageRelations
     * @param {string} direction
     * @return {string|null}
     */
    function createPackageRelationDiagram(pkg, allPackages, allPackageRelations, direction = domainSettings.diagramDirection) {
        return Jig.mermaid.createPackageLevelDiagram(
            pkg, allPackages, allPackageRelations,
            {
                transitiveReductionEnabled: domainSettings.transitiveReductionEnabled,
                diagramDirection: direction,
                nodeClickUrlCallback: (fqn) => "#" + Jig.util.fqnToId("domain", fqn),
            }
        );
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
                    Jig.dom.createElement("li", {children: [Jig.dom.type.elementForRef({fqn})]})
                )
            }));
        }

        if (incomingFqns.length > 0) {
            detailsContent.push(Jig.dom.createElement("h4", {textContent: `参照されるクラス (${incomingFqns.length})`}));
            detailsContent.push(Jig.dom.createElement("ul", {
                children: incomingFqns.map(fqn =>
                    Jig.dom.createElement("li", {children: [Jig.dom.type.elementForRef({fqn})]})
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

    /**
     * 型の直接関連エッジと関与 FQN セットを収集する（deprecated フィルタ適用済み）
     * @param {DomainType} type
     * @param {Array} typeRelations
     * @param {Map} typesMap
     * @returns {{edges: Array, involvedFqns: Set<string>} | null}
     */
    function collectTypeRelationEdges(type, typeRelations, typesMap) {
        const allRelations = typeRelations
            .filter(r => typesMap?.has(r.from) && typesMap?.has(r.to));

        const outgoing = allRelations.filter(r => r.from === type.fqn);
        const incoming = allRelations.filter(r => r.to === type.fqn);

        if (outgoing.length === 0 && incoming.length === 0) return null;

        const filteredOut = domainSettings.showDeprecatedNodes
            ? outgoing
            : outgoing.filter(r => !typesMap?.get(r.to)?.isDeprecated);
        const filteredIn = domainSettings.showDeprecatedNodes
            ? incoming
            : incoming.filter(r => !typesMap?.get(r.from)?.isDeprecated);

        if (filteredOut.length === 0 && filteredIn.length === 0) return null;

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
     * @returns {string | null}
     */
    function createTypeRelationDiagram(type, typeRelations, typesMap, direction = domainSettings.diagramDirection) {
        const result = collectTypeRelationEdges(type, typeRelations, typesMap);
        if (!result) return null;
        const {edges, involvedFqns} = result;

        const fqnToMermaidId = (fqn) => Jig.util.fqnToId("n", fqn);
        const fqnToHtmlId = (fqn) => Jig.util.fqnToId("domain", fqn);

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
                const sg = builder.startSubgraph(Jig.util.fqnToId("sg", pkgFqn), Jig.glossary.getTypeTerm(pkgFqn).title);
                fqns.forEach(fqn => builder.addNodeToSubgraph(sg, fqnToMermaidId(fqn), Jig.glossary.getTypeTerm(fqn).title));
            } else {
                fqns.forEach(fqn => builder.addNode(fqnToMermaidId(fqn), Jig.glossary.getTypeTerm(fqn).title));
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
     * @returns {string | null}
     */
    function createTypeClassDiagramSource(type, typeRelations, typesMap, direction = domainSettings.diagramDirection) {
        const result = collectTypeRelationEdges(type, typeRelations, typesMap);
        if (!result) return null;
        const {edges, involvedFqns} = result;

        const fqnToNodeId = (fqn) => Jig.util.fqnToId("n", fqn);
        const fqnToHtmlId = (fqn) => Jig.util.fqnToId("domain", fqn);

        function edgeTypeFromKinds(kinds) {
            if (!kinds) return 'dependency';
            if (kinds.includes('継承クラス')) return 'realization';
            if (kinds.includes('実装インタフェース')) return 'inheritance';
            if (kinds.includes('フィールド型') || kinds.includes('フィールド型引数')) return 'association';
            return 'dependency';
        }

        const builder = new Jig.mermaid.ClassDiagramBuilder();

        involvedFqns.forEach(fqn => {
            const nodeId = fqnToNodeId(fqn);
            builder.addClass(nodeId, Jig.glossary.typeSimpleName(fqn));

            const domainType = typesMap?.get(fqn);
            (domainType?.fields || []).forEach(f => {
                builder.addField(nodeId, typeNameWithGenerics(f.typeRef), f.name);
            });
            (domainType?.methods || []).forEach(m => {
                const {name, params, returnType} = parseMethodInfo(m);
                builder.addMethod(nodeId, m.visibility, name, params, returnType, false);
            });
            (domainType?.staticMethods || []).forEach(m => {
                const {name, params, returnType} = parseMethodInfo(m);
                builder.addMethod(nodeId, m.visibility, name, params, returnType, true);
            });

            builder.addClick(nodeId, `#${fqnToHtmlId(fqn)}`);
        });
        edges.forEach(r => builder.addEdge(fqnToNodeId(r.from), fqnToNodeId(r.to), edgeTypeFromKinds(r.kinds)));

        return builder.build(direction);
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
        direction = domainSettings.diagramDirection
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

        const builder = new Jig.mermaid.Builder();
        const sg = builder.startSubgraph(Jig.util.fqnToId("sg", pkg.fqn), Jig.glossary.getTypeTerm(pkg.fqn).title, direction);
        internalFqns.forEach(fqn => builder.addNodeToSubgraph(sg, fqnToMermaidId(fqn), Jig.glossary.getTypeTerm(fqn).title));
        externalPkgFqns.forEach(fqn => builder.addNode(fqnToMermaidId(fqn), Jig.glossary.getTypeTerm(fqn).title, 'package'));
        [...internalFqns, ...externalPkgFqns].forEach(fqn =>
            builder.addClick(fqnToMermaidId(fqn), `#${fqnToHtmlId(fqn)}`)
        );
        edges.forEach(edge => {
            const edgeLength = edgeLengthByKey.get(`${edge.from}::${edge.to}`) || 1;
            builder.addEdge(fqnToMermaidId(edge.from), fqnToMermaidId(edge.to), "", false, edgeLength);
        });

        return builder.build(direction);
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
            const children = getDirectChildPackages(pkg, childPackagesMap);
            children.forEach(child => {
                childPackageFqns.add(child.fqn);
            });
        });

        // トップレベルのパッケージのみを表示（直接の親を持たないもの）
        packages.forEach(pkg => {
            if (!childPackageFqns.has(pkg.fqn)) {
                container.appendChild(renderPackageNavItem(pkg, childPackagesMap, typesMap));
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
                title: Jig.glossary.getTypeTerm(childPkg.fqn).title
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
                const prefix = child.isPackage ? "▶︎ " : "";
                // 型の場合は createTypeLink を使用して deprecated 処理を統一
                const link = child.isPackage
                    ? Jig.dom.createElement("a", {
                        attributes: {href: "#" + Jig.util.fqnToId("domain", child.fqn)},
                        textContent: child.title
                    })
                    : Jig.dom.type.elementForRef({fqn: child.fqn});
                const cell = Jig.dom.createElement("td", {
                    children: [prefix, link]
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

    function createFieldsList(fields) {
        return Jig.dom.type.fieldsList(fields, Jig.dom.type.elementForRef);
    }

    function createMethodsList(kind, methods) {
        return Jig.dom.type.methodsList(kind, methods, Jig.dom.type.elementForRef);
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
            const section = Jig.dom.card.type({
                id: Jig.util.fqnToId("domain", pkg.fqn),
                title: Jig.glossary.getTypeTerm(pkg.fqn).title,
                fqn: pkg.fqn,
                kind: "パッケージ",
                attributes: {"data-has-enum-children": pkgHasEnum(pkg, childPackagesMap, typesMap) ? "true" : "false"}
            });

            const pkgDescription = Jig.glossary.getTypeTerm(pkg.fqn).description;
            if (pkgDescription) {
                section.appendChild(Jig.dom.createMarkdownElement(pkgDescription));
            }

            const childrenTable = createChildrenTable(pkg, childPackagesMap);
            if (childrenTable) {
                section.appendChild(childrenTable);
            }

            // データのあるダイアグラムのみタブとして表示
            const tabDefs = [
                createPackageDirectRelationDiagram(pkg, allPackageRelations) !== null
                && {id: 'direct', label: 'パッケージ関連図', diagramType: 'packageDirect'},
                createPackageRelationDiagram(pkg, allPackages, allPackageRelations) !== null
                && {id: 'inner-pkg', label: 'パッケージ内パッケージ関連図', diagramType: 'package'},
                pkg.types.length > 0 && createRelationDiagram(pkg, typeRelations, typesMap) !== null
                && {id: 'inner-class', label: 'パッケージ内クラス関連図', diagramType: 'type'},
            ].filter(Boolean);

            if (tabDefs.length > 0) {
                const {panels, section: diagramSection} = Jig.mermaid.diagram.buildTabSection(tabDefs, {className: "jig-card jig-card--item domain-diagrams-section"});
                section.appendChild(diagramSection);

                if (panels['direct']) {
                    Jig.mermaid.diagram.createAndRegister(panels['direct'], (container) => {
                        const diagramDef = {pkg, type: undefined, diagramType: 'packageDirect', allPackageRelations};
                        renderDiagram(container, diagramDef);
                    });
                }
                if (panels['inner-pkg']) {
                    Jig.mermaid.diagram.createAndRegister(panels['inner-pkg'], (container) => {
                        const diagramDef = {pkg, type: undefined, diagramType: 'package', allPackages, allPackageRelations, typeRelations, typesMap};
                        renderDiagram(container, diagramDef);
                    });
                }
                if (panels['inner-class']) {
                    const outgoingCheckbox = Jig.dom.createElement("input", {
                        attributes: {type: "checkbox", class: "class-relation-external-outgoing"}
                    });
                    outgoingCheckbox.checked = true;
                    const incomingCheckbox = Jig.dom.createElement("input", {
                        attributes: {type: "checkbox", class: "class-relation-external-incoming"}
                    });
                    incomingCheckbox.checked = true;
                    panels['inner-class'].appendChild(Jig.dom.createElement("fieldset", {
                        className: "diagram-panel-options",
                        children: [
                            Jig.dom.createElement("legend", {textContent: "パッケージ外クラス"}),
                            Jig.dom.createElement("label", {
                                className: "diagram-panel-option",
                                children: [outgoingCheckbox, "関連先"]
                            }),
                            Jig.dom.createElement("label", {
                                className: "diagram-panel-option",
                                children: [incomingCheckbox, "関連元"]
                            }),
                        ]
                    }));

                    const render = (container) => {
                        const diagramDef = {container, pkg, type: undefined, diagramType: 'type', typeRelations, typesMap};
                        renderDiagram(container, diagramDef);
                    };
                    const c = Jig.mermaid.diagram.createAndRegister(panels['inner-class'], render);
                    outgoingCheckbox.addEventListener('change', () => render(c));
                    incomingCheckbox.addEventListener('change', () => render(c));
                }
            }

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
                attributes: {"data-has-enum": type.enumInfo ? "true" : "false"}
            });

            const typeDescription = Jig.glossary.getTypeTerm(type.fqn).description;
            if (typeDescription) {
                section.appendChild(Jig.dom.createMarkdownElement(typeDescription));
            }

            if (type.enumInfo) {
                section.appendChild(createEnumSection(type));
            }

            const fieldsList = createFieldsList(type.fields);
            if (fieldsList) section.appendChild(fieldsList);

            const methodList = createMethodsList("メソッド", type.methods);
            if (methodList) section.appendChild(methodList);

            const staticList = createMethodsList("staticメソッド", type.staticMethods);
            if (staticList) section.appendChild(staticList);

            if (createTypeRelationDiagram(type, typeRelations, typesMap) !== null) {
                const tabDefs = [
                    {id: 'relation', label: 'クラス関連図', diagramType: 'classDirect'},
                    {id: 'classdiag', label: 'クラス図', diagramType: 'classDefinition'},
                ];
                const {panels, section: diagramSection} = Jig.mermaid.diagram.buildTabSection(tabDefs, {className: "jig-card jig-card--item domain-diagrams-section"});
                section.appendChild(diagramSection);
                tabDefs.forEach(tab => {
                    Jig.mermaid.diagram.createAndRegister(panels[tab.id], (container) => {
                        renderDiagram(container, {pkg: undefined, type, diagramType: tab.diagramType, typeRelations, typesMap});
                    });
                });
            }

            const relatedList = createRelatedClassesList(type, typeRelations, typesMap);
            if (relatedList) section.appendChild(relatedList);

            container.appendChild(section);
        });
    }

    /**
     * @returns {void}
     */
    function updateDirectionIcon() {
        const verticalArrows = document.querySelector('.vertical-arrows');
        const horizontalArrows = document.querySelector('.horizontal-arrows');
        if (verticalArrows && horizontalArrows) {
            if (domainSettings.diagramDirection === 'TB') {
                verticalArrows.style.display = '';
                horizontalArrows.style.display = 'none';
            } else {
                verticalArrows.style.display = 'none';
                horizontalArrows.style.display = '';
            }
        }
    }

    /**
     * 指定されたダイアグラムを再生成
     * @param {HTMLElement} container
     * @param {Object} diagram - {pkg, type, diagramType, allPackages?, allPackageRelations?}
     */
    function renderDiagram(container, diagram) {
        const {pkg, type, diagramType, allPackages, allPackageRelations, typeRelations, typesMap} = diagram;

        container.innerHTML = "";

        const renderIfNonNull = (generator) => {
            if (generator(domainSettings.diagramDirection)) {
                Jig.mermaid.render.renderWithControls(container, generator, {direction: domainSettings.diagramDirection});
            }
        };

        if (diagramType === 'packageDirect') {
            renderIfNonNull((dir) => createPackageDirectRelationDiagram(pkg, allPackageRelations, dir));
        } else if (diagramType === 'package') {
            renderIfNonNull((dir) => createPackageRelationDiagram(pkg, allPackages, allPackageRelations, dir));
        } else if (diagramType === 'classDirect') {
            renderIfNonNull((dir) => createTypeRelationDiagram(type, typeRelations, typesMap, dir));
        } else if (diagramType === 'classDefinition') {
            renderIfNonNull((dir) => createTypeClassDiagramSource(type, typeRelations, typesMap, dir));
        } else {
            // テスト環境など closest が使えない場合に対応
            const panel = typeof container.closest === 'function' ? container.closest('.diagram-panel') : null;
            const outgoing = panel?.querySelector('.class-relation-external-outgoing');
            const incoming = panel?.querySelector('.class-relation-external-incoming');
            const showExternalOutgoing = outgoing ? outgoing.checked : true;
            const showExternalIncoming = incoming ? incoming.checked : true;
            renderIfNonNull((dir) => createRelationDiagram(pkg, typeRelations, typesMap, {
                showExternalOutgoing,
                showExternalIncoming,
                direction: dir
            }));
        }
    }


    /**
     * @returns {void}
     */
    function applyVisibilitySettings() {
        const main = document.getElementById('domain-main');
        if (!main) return;

        const fieldsSections = main.querySelectorAll('section.methods-section');
        fieldsSections.forEach(section => {
            const h4 = section.querySelector('h4');
            if (h4 && h4.textContent === 'フィールド') {
                section.style.display = domainSettings.showFields ? '' : 'none';
            } else if (h4 && h4.textContent === 'メソッド') {
                section.style.display = domainSettings.showMethods ? '' : 'none';
            } else if (h4 && h4.textContent === 'staticメソッド') {
                section.style.display = domainSettings.showStaticMethods ? '' : 'none';
            }
        });

        // 「列挙のみ表示」フィルター
        if (domainSettings.showEnumOnly) {
            // メインのパッケージセクションは全て非表示
            const packageSections = main.querySelectorAll('section.jig-card--type[data-has-enum-children]');
            packageSections.forEach(section => {
                section.style.display = 'none';
            });

            // メインのタイプセクションのフィルター（enum でないタイプは非表示）
            const typeSections = main.querySelectorAll('section.jig-card--type[data-has-enum]');
            typeSections.forEach(section => {
                section.style.display = section.dataset.hasEnum === 'true' ? '' : 'none';
            });

            // サイドバーのパッケージのフィルター（enum を含まないパッケージは非表示）
            const sidebar = document.getElementById('domain-sidebar');
            if (sidebar) {
                const packageDetails = sidebar.querySelectorAll('details[data-has-enum-children]');
                packageDetails.forEach(details => {
                    details.style.display = details.dataset.hasEnumChildren === 'true' ? '' : 'none';
                });

                // サイドバーの型リンクのフィルター（enum でない型は非表示）
                const typeItems = sidebar.querySelectorAll('div[data-has-enum]');
                typeItems.forEach(div => {
                    div.style.display = div.dataset.hasEnum === 'true' ? '' : 'none';
                });
            }
        } else {
            // 全て表示
            const allSections = main.querySelectorAll('section.jig-card--type');
            allSections.forEach(section => {
                section.style.display = '';
            });

            const sidebar = document.getElementById('domain-sidebar');
            if (sidebar) {
                const packageDetails = sidebar.querySelectorAll('details[data-has-enum-children]');
                packageDetails.forEach(details => {
                    details.style.display = '';
                });

                const typeItems = sidebar.querySelectorAll('div[data-has-enum]');
                typeItems.forEach(div => {
                    div.style.display = '';
                });
            }
        }
        applySidebarTextFilter();
    }

    function applySidebarTextFilter() {
        const filterText = domainSettings.sidebarFilterText.toLowerCase();
        if (!filterText) return;

        const sidebar = document.getElementById('domain-sidebar');
        if (!sidebar) return;

        sidebar.querySelectorAll('div[data-has-enum]').forEach(div => {
            const link = div.querySelector('a');
            const text = link ? link.textContent.toLowerCase() : '';
            div.style.display = text.includes(filterText) ? '' : 'none';
        });

        Array.from(sidebar.querySelectorAll('details[data-has-enum-children]'))
            .reverse()
            .forEach(details => {
                const hasVisible = Array.from(details.children).some(child =>
                    child.tagName !== 'SUMMARY' && child.style.display !== 'none'
                );
                details.style.display = hasVisible ? '' : 'none';
            });
    }

    /**
     * @returns {void}
     */
    function initSettings() {
        const directionToggle = document.getElementById('direction-toggle');
        if (directionToggle) {
            directionToggle.addEventListener('click', () => {
                domainSettings.diagramDirection = domainSettings.diagramDirection === 'TB' ? 'LR' : 'TB';
                updateDirectionIcon();
                Jig.mermaid.diagram.rerenderVisible();
            });
        }

        const reductionCheckbox = document.getElementById('transitive-reduction-toggle');
        if (reductionCheckbox) reductionCheckbox.checked = domainSettings.transitiveReductionEnabled;

        [
            {id: 'show-deprecated-nodes',       key: 'showDeprecatedNodes',        after: () => Jig.mermaid.diagram.rerenderVisible()},
            {id: 'transitive-reduction-toggle', key: 'transitiveReductionEnabled', after: () => Jig.mermaid.diagram.rerenderVisible()},
            {id: 'show-diagrams',               key: 'showDiagrams',               after: v => document.body.classList.toggle('hide-domain-diagrams', !v)},
            {id: 'show-fields',                 key: 'showFields',                 after: applyVisibilitySettings},
            {id: 'show-methods',                key: 'showMethods',                after: applyVisibilitySettings},
            {id: 'show-static-methods',         key: 'showStaticMethods',          after: applyVisibilitySettings},
            {id: 'show-enum-only',              key: 'showEnumOnly',               after: applyVisibilitySettings},
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
    }

    /**
     * @returns {void}
     */
    function init() {
        if (typeof document === "undefined" || !document.body.classList.contains("domain-model")) return;

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

if (typeof document !== 'undefined') {
    document.addEventListener("DOMContentLoaded", () => {
        DomainApp.init();
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = DomainApp;
}
