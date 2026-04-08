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
    };

    /**
     * @returns {DomainData}
     */
    function getDomainData() {
        return globalThis.domainData;
    }

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

            if (!packageTypesMap.has(pkgFqn)) packageTypesMap.set(pkgFqn, []);
            packageTypesMap.get(pkgFqn).push({fqn: type.fqn});

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
     * @returns {PackageType[]}
     */
    function getDirectChildPackages(pkg) {
        return getDomainData()._childPackagesMap.get(pkg.fqn) ?? [];
    }

    /**
     * パッケージが enum 型を含むかを判定する（再帰的）
     * @param {PackageType} pkg
     * @returns {boolean}
     */
    function pkgHasEnum(pkg) {
        // このパッケージのタイプに enum があるか
        if (pkg.types.some(type => getDomainData()._typesMap?.get(type.fqn)?.enumInfo)) {
            return true;
        }
        // 子パッケージに enum があるか
        const childPackages = getDirectChildPackages(pkg);
        return childPackages.some(childPkg => pkgHasEnum(childPkg));
    }

    /**
     * @param {PackageType} pkg
     * @returns {HTMLElement}
     */
    function renderPackageNavItem(pkg) {
        // 子が1つだけでタイプを持たないパッケージを統合して表示
        let currentPkg = pkg;
        const mergedNames = [Jig.glossary.getTypeTerm(pkg.fqn).title];

        while (true) {
            const childPackages = getDirectChildPackages(currentPkg);
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
                "data-has-enum-children": pkgHasEnum(currentPkg) ? "true" : "false"
            },
            children: [
                Jig.dom.createElement("summary", {
                    className: "package",
                    children: [summaryLink, document.createTextNode("/")]
                })
            ]
        });

        // 子パッケージを表示（統合後の currentPkg の直下のみ）
        const childPackages = getDirectChildPackages(currentPkg);
        childPackages.forEach(childPkg => {
            details.appendChild(renderPackageNavItem(childPkg));
        });

        // 子タイプを表示
        currentPkg.types.forEach(child => {
            const domainType = getDomainData()._typesMap?.get(child.fqn);
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

        return Jig.dom.createElement("section", {
            className: "jig-card--item",
            children: [Jig.dom.createElement("details", {
                children: [
                    Jig.dom.createElement("summary", {textContent: "関連情報"}),
                    ...detailsContent
                ]
            })]
        });
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
        const allRelations = typeRelations
            .filter(r => typesMap?.has(r.from) && typesMap?.has(r.to));

        const outgoing = allRelations.filter(r => r.from === type.fqn);
        const incoming = allRelations.filter(r => r.to === type.fqn);

        if (outgoing.length === 0 && incoming.length === 0) return null;

        // Deprecated ノード非表示の場合、deprecated 型を除外
        const filteredOut = domainSettings.showDeprecatedNodes
            ? outgoing
            : outgoing.filter(r => !typesMap?.get(r.to)?.isDeprecated);
        const filteredIn = domainSettings.showDeprecatedNodes
            ? incoming
            : incoming.filter(r => !typesMap?.get(r.from)?.isDeprecated);

        if (filteredOut.length === 0 && filteredIn.length === 0) return null;

        // エッジの重複排除（A→B と B→A が両方ある場合も含む）
        const edgeMap = new Map();
        [...filteredOut, ...filteredIn].forEach(r => edgeMap.set(`${r.from}::${r.to}`, r));
        const edges = Array.from(edgeMap.values());

        const involvedFqns = new Set([type.fqn]);
        edges.forEach(r => {
            involvedFqns.add(r.from);
            involvedFqns.add(r.to);
        });

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
            if (!byPackage.has(pkg)) byPackage.set(pkg, []);
            byPackage.get(pkg).push(fqn);
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
     * @returns {void}
     */
    function renderSidebar(packages) {
        const container = document.getElementById("domain-sidebar-list");
        if (!container) return;
        container.innerHTML = "";

        // 直接の子パッケージ fqn の集合
        const childPackageFqns = new Set();
        packages.forEach(pkg => {
            const children = getDirectChildPackages(pkg);
            children.forEach(child => {
                childPackageFqns.add(child.fqn);
            });
        });

        // トップレベルのパッケージのみを表示（直接の親を持たないもの）
        packages.forEach(pkg => {
            if (!childPackageFqns.has(pkg.fqn)) {
                container.appendChild(renderPackageNavItem(pkg));
            }
        });
    }

    /**
     * @param {PackageType} pkg
     * @returns {HTMLElement | null}
     */
    function createChildrenTable(pkg) {
        const types = pkg.types;
        const childPackages = getDirectChildPackages(pkg);

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
                    children: [document.createTextNode(prefix), link]
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

        const section = Jig.dom.createElement("section", {
            className: "jig-card jig-card--item",
            children: [
                Jig.dom.createElement("h4", {textContent: "列挙値"}),
                dl
            ]
        });

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
     * @param {HTMLElement} container
     * @returns {void}
     */
    function renderPackages(packages, typeRelations, typesMap, allPackageRelations, container) {
        if (packages.length === 0) return;

        const allPackages = getDomainData()._packages;

        packages.forEach(pkg => {
            const section = Jig.dom.createElement("section", {
                className: "jig-card jig-card--type",
                id: Jig.util.fqnToId("domain", pkg.fqn),
                attributes: {"data-has-enum-children": pkgHasEnum(pkg) ? "true" : "false"},
                children: [
                    Jig.dom.createElement("h3", {
                        children: [Jig.dom.kind.badgeElement("パッケージ"), document.createTextNode(Jig.glossary.getTypeTerm(pkg.fqn).title)]
                    }),
                    Jig.dom.createElement("div", {
                        className: "fully-qualified-name",
                        textContent: pkg.fqn
                    })
                ]
            });

            const pkgDescription = Jig.glossary.getTypeTerm(pkg.fqn).description;
            if (pkgDescription) {
                section.appendChild(Jig.dom.createMarkdownElement(pkgDescription));
            }

            const childrenTable = createChildrenTable(pkg);
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
                const tabsBar = Jig.dom.createElement("div", {className: "diagram-tabs"});
                const panels = {};
                tabDefs.forEach((tab, i) => {
                    panels[tab.id] = Jig.dom.createElement("div", {className: "diagram-panel" + (i > 0 ? " hidden" : "")});
                });
                tabDefs.forEach((tab, i) => {
                    const btn = Jig.dom.createElement("button", {
                        className: "diagram-tab" + (i === 0 ? " active" : ""),
                        textContent: tab.label,
                    });
                    btn.addEventListener('click', () => {
                        tabsBar.querySelectorAll('.diagram-tab').forEach(b => b.classList.remove('active'));
                        Object.values(panels).forEach(p => p.classList.add('hidden'));
                        btn.classList.add('active');
                        panels[tab.id].classList.remove('hidden');
                    });
                    tabsBar.appendChild(btn);
                });

                section.appendChild(Jig.dom.createElement("section", {
                    className: "jig-card--item domain-diagrams-section",
                    children: [tabsBar, ...Object.values(panels)],
                }));

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
                    panels['inner-class'].appendChild(Jig.dom.createElement("div", {
                        className: "diagram-panel-options",
                        children: [
                            Jig.dom.createElement("label", {
                                className: "diagram-panel-option",
                                children: [outgoingCheckbox, document.createTextNode("関連先")]
                            }),
                            Jig.dom.createElement("label", {
                                className: "diagram-panel-option",
                                children: [incomingCheckbox, document.createTextNode("関連元")]
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
                fqnDiv.appendChild(document.createTextNode("." + type.fqn.substring(lastDot + 1)));
            } else {
                fqnDiv.textContent = type.fqn;
            }

            const section = Jig.dom.createElement("section", {
                className: "jig-card jig-card--type",
                id: Jig.util.fqnToId("domain", type.fqn),
                attributes: {"data-has-enum": type.enumInfo ? "true" : "false"},
                children: [
                    Jig.dom.createElement("h3", {children: [Jig.dom.kind.badgeElement("クラス"), titleSpan]}),
                    fqnDiv
                ]
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

            Jig.mermaid.diagram.createAndRegister(section, (container) => {
                const diagramDef = {container, pkg: undefined, type, diagramType: 'classDirect', typeRelations, typesMap};
                renderDiagram(container, diagramDef);
                // 見出しはまだ追加されていない場合のみ追加
                if (!section.querySelector('h4.diagram-heading')) {
                    section.insertBefore(Jig.dom.createElement("h4", {
                        textContent: "クラス関連図",
                        className: "diagram-heading"
                    }), container);
                }
            });

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
        if (diagramType === 'packageDirect') {
            const generator = (dir) => createPackageDirectRelationDiagram(pkg, allPackageRelations, dir);
            if (generator(domainSettings.diagramDirection)) {
                Jig.mermaid.render.renderWithControls(container, generator, {direction: domainSettings.diagramDirection});
            }
        } else if (diagramType === 'package') {
            const generator = (dir) => createPackageRelationDiagram(pkg, allPackages, allPackageRelations, dir);
            if (generator(domainSettings.diagramDirection)) {
                Jig.mermaid.render.renderWithControls(container, generator, {direction: domainSettings.diagramDirection});
            }
        } else if (diagramType === 'classDirect') {
            const generator = (dir) => createTypeRelationDiagram(type, typeRelations, typesMap, dir);
            if (generator(domainSettings.diagramDirection)) {
                Jig.mermaid.render.renderWithControls(container, generator, {direction: domainSettings.diagramDirection});
            }
        } else {
            // テスト環境など closest が使えない場合に対応
            const panel = typeof container.closest === 'function' ? container.closest('.diagram-panel') : null;
            const outgoing = panel?.querySelector('.class-relation-external-outgoing');
            const incoming = panel?.querySelector('.class-relation-external-incoming');
            const showExternalOutgoing = outgoing ? outgoing.checked : true;
            const showExternalIncoming = incoming ? incoming.checked : true;
            const generator = (dir) => createRelationDiagram(pkg, typeRelations, typesMap, {
                showExternalOutgoing,
                showExternalIncoming,
                direction: dir
            });
            if (generator(domainSettings.diagramDirection)) {
                Jig.mermaid.render.renderWithControls(container, generator, {direction: domainSettings.diagramDirection});
            }
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

        const deprecatedCheckbox = document.getElementById('show-deprecated-nodes');
        if (deprecatedCheckbox) {
            deprecatedCheckbox.addEventListener('change', () => {
                domainSettings.showDeprecatedNodes = deprecatedCheckbox.checked;
                Jig.mermaid.diagram.rerenderVisible();
            });
        }

        const reductionCheckbox = document.getElementById('transitive-reduction-toggle');
        if (reductionCheckbox) {
            reductionCheckbox.checked = domainSettings.transitiveReductionEnabled;
            reductionCheckbox.addEventListener('change', () => {
                domainSettings.transitiveReductionEnabled = reductionCheckbox.checked;
                Jig.mermaid.diagram.rerenderVisible();
            });
        }

        const diagramsCheckbox = document.getElementById('show-diagrams');
        if (diagramsCheckbox) {
            diagramsCheckbox.addEventListener('change', () => {
                domainSettings.showDiagrams = diagramsCheckbox.checked;
                document.body.classList.toggle('hide-domain-diagrams', !domainSettings.showDiagrams);
            });
        }

        const fieldsCheckbox = document.getElementById('show-fields');
        if (fieldsCheckbox) {
            fieldsCheckbox.addEventListener('change', () => {
                domainSettings.showFields = fieldsCheckbox.checked;
                applyVisibilitySettings();
            });
        }

        const methodsCheckbox = document.getElementById('show-methods');
        if (methodsCheckbox) {
            methodsCheckbox.addEventListener('change', () => {
                domainSettings.showMethods = methodsCheckbox.checked;
                applyVisibilitySettings();
            });
        }

        const staticMethodsCheckbox = document.getElementById('show-static-methods');
        if (staticMethodsCheckbox) {
            staticMethodsCheckbox.addEventListener('change', () => {
                domainSettings.showStaticMethods = staticMethodsCheckbox.checked;
                applyVisibilitySettings();
            });
        }

        const enumOnlyCheckbox = document.getElementById('show-enum-only');
        if (enumOnlyCheckbox) {
            enumOnlyCheckbox.addEventListener('change', () => {
                domainSettings.showEnumOnly = enumOnlyCheckbox.checked;
                applyVisibilitySettings();
            });
        }
    }

    /**
     * @returns {void}
     */
    function init() {
        if (typeof document === "undefined" || !document.body.classList.contains("domain-model")) return;

        const data = getDomainData();
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
        globalThis.domainData._typesMap = new Map(
            data.types.map(type => [type.fqn, type])
        );

        // domainPackageRoots と types からパッケージを構築
        const packages = buildPackages(data.domainPackageRoots, data.types);
        globalThis.domainData._packages = packages;

        // packages の直下の子を事前計算（O(n) → O(1) 取得）
        const childrenMap = new Map(packages.map(p => [p.fqn, []]));
        packages.forEach(p => {
            const parentFqn = p.fqn.substring(0, p.fqn.lastIndexOf('.'));
            if (childrenMap.has(parentFqn)) {
                childrenMap.get(parentFqn).push(p);
            }
        });
        globalThis.domainData._childPackagesMap = childrenMap;

        // typeRelations を一度解決（以降は引数経由で渡す）
        const typesMap = globalThis.domainData._typesMap;
        const rawRelations = globalThis.typeRelationsData?.relations || [];
        const typeRelations = rawRelations.filter(r => typesMap?.has(r.from) && typesMap?.has(r.to));
        const allPackageRelations = derivePackageRelations(typeRelations, typesMap);

        Jig.dom.type.setResolver((fqn) => {
            const domainType = getDomainData()?._typesMap?.get(fqn);
            if (domainType) {
                return {
                    href: '#' + Jig.util.fqnToId("domain", fqn),
                    className: domainType.isDeprecated ? 'deprecated' : undefined
                };
            }
            // domain型でなければ単純名 + weakクラス
            return {
                className: 'weak',
                text: fqn.substring(fqn.lastIndexOf('.') + 1)
            };
        });

        renderSidebar(packages);

        const main = document.getElementById("domain-main");
        if (!main) return;
        main.innerHTML = "";

        // optional データの警告表示
        const warnings = [];
        if (!globalThis.glossaryData) {
            warnings.push("用語集（glossary-data.js）が読み込まれていません");
        }
        if (!globalThis.typeRelationsData) {
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

        renderPackages(packages, typeRelations, typesMap, allPackageRelations, main);
        renderTypes(data.types, typeRelations, typesMap, main);
    }

    return {
        init,
        renderPackageNavItem,
        getDirectChildPackages,
        createRelationDiagram,
        createTypeRelationDiagram,
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
