const InboundApp = (() => {
    const Jig = globalThis.Jig;

    const ADAPTER_ID_PREFIX = "adapter";

    const state = {
        data: null,
        sidebarFilterText: '',
    };

    const Diagram = {
        /**
         * 1. 情報を読み込んで表示のために構造化する
         */
        prepareData(controller, usecaseData) {
            const entrypointFqns = new Set(controller.entrypoints.map(ep => ep.fqn));

            const usecaseMethodToType = new Map();
            if (usecaseData) {
                usecaseData.usecases.forEach(uc => {
                    (uc.methods || []).forEach(m => usecaseMethodToType.set(m.fqn, uc.fqn));
                });
            }

            const allFqns = new Set(controller.entrypoints.map(ep => ep.fqn));
            controller.relations.forEach(edge => {
                allFqns.add(edge.from);
                allFqns.add(edge.to);
            });

            const entrypointGroups = new Map();
            controller.entrypoints.forEach(ep => {
                const typeFqn = ep.fqn.split('#')[0];
                if (!entrypointGroups.has(typeFqn)) entrypointGroups.set(typeFqn, []);
                entrypointGroups.get(typeFqn).push(ep);
            });

            const usecaseGroups = new Map();
            allFqns.forEach(fqn => {
                if (!entrypointFqns.has(fqn) && usecaseMethodToType.has(fqn)) {
                    const typeFqn = usecaseMethodToType.get(fqn);
                    if (!usecaseGroups.has(typeFqn)) usecaseGroups.set(typeFqn, []);
                    usecaseGroups.get(typeFqn).push(fqn);
                }
            });

            const methodGroups = new Map();
            allFqns.forEach(fqn => {
                if (!entrypointFqns.has(fqn) && !usecaseMethodToType.has(fqn)) {
                    const typeFqn = fqn.split('#')[0];
                    if (!methodGroups.has(typeFqn)) methodGroups.set(typeFqn, []);
                    methodGroups.get(typeFqn).push(fqn);
                }
            });

            const adapterFqns = new Set([...entrypointFqns]);
            allFqns.forEach(fqn => {
                if (!entrypointFqns.has(fqn) && !usecaseMethodToType.has(fqn)) {
                    adapterFqns.add(fqn);
                }
            });

            const {edgeLengthByKey} = Jig.mermaid.graph.computeOutboundEdgeLengths({
                nodesInSubgraph: adapterFqns,
                edges: controller.relations
            });

            return {
                controller,
                entrypointFqns,
                usecaseMethodToType,
                allFqns,
                entrypointGroups,
                usecaseGroups,
                methodGroups,
                adapterFqns,
                edgeLengthByKey
            };
        },

        /**
         * 2. 構造化されたデータを元にMermaidBuilderを組み立てる
         */
        buildBuilder(data, builder) {
            const fqnToNodeId = (fqn) => Jig.util.fqnToId("n", fqn);
            builder.applyThemeClassDefs();

            // entrypointノード → クラス単位のsubgraph
            data.entrypointGroups.forEach((eps, typeFqn) => {
                const subgraph = builder.startSubgraph(Jig.util.fqnToId("sg", typeFqn), Jig.glossary.getTypeTerm(typeFqn).title);
                eps.forEach(ep => {
                    const label = Jig.glossary.getMethodTerm(ep.fqn, true).title;
                    builder.addNodeToSubgraph(subgraph, fqnToNodeId(ep.fqn), label, 'method');
                    builder.addClass(fqnToNodeId(ep.fqn), "inbound");
                });
            });

            // パスノードとdotted edge
            const pathNodeShapes = {HTTP_API: 'request', QUEUE_LISTENER: 'queue', SCHEDULER: 'scheduler'};
            data.controller.entrypoints.forEach(ep => {
                const pathNodeId = Jig.util.fqnToId("path", ep.fqn);
                const pathShape = pathNodeShapes[ep.entrypointType];
                builder.addNode(pathNodeId, ep.path, pathShape);
                builder.addEdge(pathNodeId, fqnToNodeId(ep.fqn), "", true);
            });

            // usecaseサブグラフ
            data.usecaseGroups.forEach((methods, typeFqn) => {
                const sgLabel = Jig.glossary.getTypeTerm(typeFqn).title;
                const subgraph = builder.startSubgraph(Jig.util.fqnToId("sg", typeFqn), sgLabel);
                methods.forEach(fqn => {
                    const mId = fqnToNodeId(fqn);
                    const mLabel = Jig.glossary.getMethodTerm(fqn, true).title;
                    builder.addNodeToSubgraph(subgraph, mId, mLabel, 'method');
                    builder.addClass(mId, "usecase");
                    builder.addClick(mId, `./usecase.html#${Jig.util.fqnToId("method", fqn)}`);
                });
            });

            // methodノード
            data.methodGroups.forEach((methods, typeFqn) => {
                const subgraph = builder.startSubgraph(Jig.util.fqnToId("sg", typeFqn), Jig.glossary.getTypeTerm(typeFqn).title);
                methods.forEach(fqn => {
                    const label = Jig.glossary.getMethodTerm(fqn, true).title;
                    const nodeId = fqnToNodeId(fqn);
                    builder.addNodeToSubgraph(subgraph, nodeId, label, 'method');
                    builder.addClass(nodeId, "inactive");
                });
            });

            // エッジ
            data.controller.relations.forEach(edge => {
                const edgeLength = data.edgeLengthByKey.get(`${edge.from}::${edge.to}`) || 1;
                builder.addEdge(fqnToNodeId(edge.from), fqnToNodeId(edge.to), "", false, edgeLength);
            });
        },


    };

    function parseInboundData() {
        return Jig.data.inbound.get();
    }

    function init() {
        // モジュールキャッシュを再ロードしなくても状態がリセットされるよう明示的にクリア
        state.data = null;
        state.sidebarFilterText = '';

        state.data = parseInboundData();
        if (!state.data) {
            return;
        }

        Jig.dom.sidebar.initTextFilter('inbound-sidebar-filter', text => {
            state.sidebarFilterText = text;
            renderSidebar(state.data.inboundAdapters || []);
        });

        render();
    }

    function render() {
        const controllers = state.data.inboundAdapters || [];
        renderSidebar(controllers);
        renderMain(controllers);
    }

    function renderSidebar(adapters) {
        const sidebar = document.getElementById("inbound-sidebar-list");
        if (!sidebar) return;
        sidebar.innerHTML = "";

        const filterText = state.sidebarFilterText.toLowerCase();
        const typeLabels = {
            HTTP_API: "HTTPコントローラー",
            QUEUE_LISTENER: "キューリスナー",
            SCHEDULER: "スケジューラー",
            OTHER: "その他",
        };

        for (const [type, label] of Object.entries(typeLabels)) {
            const items = adapters
                .filter(c => c.entrypoints?.some(ep => ep.entrypointType === type))
                .flatMap(c => {
                    const title = Jig.glossary.getTypeTerm(c.fqn).title;
                    if (filterText && !title.toLowerCase().includes(filterText)) return [];
                    return [{id: Jig.util.fqnToId(ADAPTER_ID_PREFIX,c.fqn), label: title}];
                });
            if (items.length === 0) continue;
            Jig.dom.sidebar.renderSection(sidebar, label, items);
        }
    }

    function splitHttpPath(path) {
        const spaceIdx = (path || '').indexOf(' ');
        return spaceIdx !== -1
            ? [path.slice(0, spaceIdx), path.slice(spaceIdx + 1)]
            : ['', path || ''];
    }

    function renderSummaryTable(inboundTypes) {
        const rows = [];
        inboundTypes.forEach(inboundType => {
            const cardId = Jig.util.fqnToId(ADAPTER_ID_PREFIX, inboundType.fqn);
            (inboundType.entrypoints || []).forEach(ep => rows.push({ep, cardId}));
        });
        if (rows.length === 0) return null;

        return Jig.dom.createElement("table", {
            className: "entrypoint-summary",
            children: [
                Jig.dom.createElement("thead", {
                    children: [Jig.dom.createElement("tr", {
                        children: [
                            Jig.dom.createElement("th", {textContent: "メソッド"}),
                            Jig.dom.createElement("th", {textContent: "パス"}),
                            Jig.dom.createElement("th", {textContent: "エントリーポイント"})
                        ]
                    })]
                }),
                Jig.dom.createElement("tbody", {
                    children: rows.map(({ep, cardId}) => {
                        const [method, path] = splitHttpPath(ep.path);
                        return Jig.dom.createElement("tr", {
                            children: [
                                Jig.dom.createCell(method),
                                Jig.dom.createCell(path),
                                Jig.dom.createElement("td", {
                                    children: [Jig.dom.createElement("a", {
                                        textContent: ep.fqn,
                                        attributes: {href: '#' + cardId}
                                    })]
                                })
                            ]
                        });
                    })
                })
            ]
        });
    }

    function renderMain(inboundTypes) {
        const container = document.getElementById("inbound-list");
        if (!container) return;
        container.innerHTML = "";

        if (!inboundTypes || inboundTypes.length === 0) {
            container.textContent = "データなし";
            return;
        }

        const summaryTable = renderSummaryTable(inboundTypes);
        if (summaryTable) container.appendChild(summaryTable);

        inboundTypes.forEach(inboundType => {
            const typeTerm = Jig.glossary.getTypeTerm(inboundType.fqn);

            const jigCard = Jig.dom.createElement("section", {
                className: "jig-card jig-card--type",
                id: Jig.util.fqnToId(ADAPTER_ID_PREFIX,inboundType.fqn),
                children: [
                    Jig.dom.createElement("h3", {
                        children: [Jig.dom.createElement("a", {textContent: typeTerm.title})]
                    }),
                    Jig.dom.createElement("div", {
                        className: "fully-qualified-name",
                        textContent: inboundType.fqn
                    })
                ]
            });

            // 説明があれば出力
            if (typeTerm.description) {
                jigCard.appendChild(Jig.dom.createMarkdownElement(typeTerm.description));
            }

            // マッピングパスがあれば出力
            // TODO: classPath -> mappingPath
            if (inboundType.classPath) {
                jigCard.appendChild(Jig.dom.createElement("div", {
                    className: "class-path",
                    textContent: inboundType.classPath
                }));
            }

            // エントリーポイントの一覧を出力
            const methodsList = Jig.dom.type.methodsList("エントリーポイント", inboundType.entrypoints);
            if (methodsList) jigCard.appendChild(methodsList);

            // 型単位のダイアグラムを描画
            const diagramGenerator = (dir) => {
                const data = Diagram.prepareData(inboundType, Jig.data.usecase.get());
                const builder = new Jig.mermaid.Builder();
                Diagram.buildBuilder(data, builder);
                return builder.build(dir);
            };
            Jig.mermaid.diagram.createAndRegister(jigCard, (mmdContainer) => {
                Jig.mermaid.render.renderWithControls(mmdContainer, diagramGenerator, {direction: 'LR'});
            });

            container.appendChild(jigCard);
        });
    }

    return {
        init,
        parseInboundData,
        render,
        renderSidebar,
        renderSummaryTable,
        renderMain,
        Diagram
    };
})();

if (typeof document !== 'undefined') {
    document.addEventListener("DOMContentLoaded", () => {
        InboundApp.init();
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = InboundApp;
}
