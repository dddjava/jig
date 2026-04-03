const InboundApp = (() => {
    const Jig = globalThis.Jig;

    const state = {
        data: null
    };

    function parseInboundData() {
        return globalThis.inboundData ?? null;
    }

    function init() {
        // モジュールキャッシュを再ロードしなくても状態がリセットされるよう明示的にクリア
        state.data = null;

        state.data = parseInboundData();
        if (!state.data) {
            return;
        }
        render();
    }

    function render() {
        const controllers = state.data.controllers || [];
        renderSidebar(controllers);
        renderControllerList(controllers);
    }

    function renderSidebar(controllers) {
        const sidebar = document.getElementById("inbound-sidebar-list");
        if (!sidebar) return;
        sidebar.innerHTML = "";

        const items = controllers.map(c => ({
            id: Jig.fqnToId("adapter", c.fqn),
            label: Jig.glossary.getTypeTerm(c.fqn).title
        }));
        Jig.dom.sidebar.renderSection(sidebar, "コントローラー", items);
    }

    function renderControllerList(controllers) {
        const container = document.getElementById("inbound-list");
        if (!container) return;
        container.innerHTML = "";

        if (!controllers || controllers.length === 0) {
            container.textContent = "データなし";
            return;
        }

        controllers.forEach(controller => {
            const typeTerm = Jig.glossary.getTypeTerm(controller.fqn);
            const section = Jig.dom.createElement("section", {
                className: "jig-card jig-card--type",
                id: Jig.fqnToId("adapter", controller.fqn),
                children: [
                    Jig.dom.createElement("h3", {
                        children: [Jig.dom.createElement("a", {textContent: typeTerm.title})]
                    }),
                    Jig.dom.createElement("div", {
                        className: "fully-qualified-name",
                        textContent: controller.fqn
                    })
                ]
            });

            if (typeTerm.description) {
                section.appendChild(Jig.dom.createElement("section", {
                    className: "markdown",
                    innerHTML: Jig.dom.parseMarkdown(typeTerm.description)
                }));
            }

            if (controller.classPath) {
                section.appendChild(Jig.dom.createElement("div", {
                    className: "class-path",
                    textContent: controller.classPath
                }));
            }

            const methodsList = Jig.dom.type.methodsList("エントリーポイント", controller.entrypoints);
            if (methodsList) section.appendChild(methodsList);

            const mmdContainer = Jig.dom.createElement("div", {className: "mermaid-diagram"});
            section.appendChild(mmdContainer);

            Jig.dom.lazyRender(mmdContainer, () => {
                const fqnToNodeId = (fqn) => Jig.fqnToId("n", fqn);
                const builder = new Jig.mermaid.Builder();
                builder.applyThemeClassDefs();

                const entrypointFqns = new Set(controller.entrypoints.map(ep => ep.fqn));

                // usecaseData から methodFqn → typeFqn のインデックスを構築
                const usecaseMethodToType = new Map();
                if (globalThis.usecaseData) {
                    globalThis.usecaseData.usecases.forEach(uc => {
                        (uc.methods || []).forEach(m => usecaseMethodToType.set(m.fqn, uc.fqn));
                    });
                }

                // edges に登場する全 FQN を収集（重複排除）
                const allFqns = new Set(controller.entrypoints.map(ep => ep.fqn));
                controller.relations.forEach(edge => {
                    allFqns.add(edge.from);
                    allFqns.add(edge.to);
                });

                // entrypointノード → クラス単位のsubgraph
                const entrypointGroups = new Map();
                controller.entrypoints.forEach(ep => {
                    const typeFqn = ep.fqn.split('#')[0];
                    const subgraph = builder.ensureSubgraph(entrypointGroups, Jig.fqnToId("sg", typeFqn), Jig.glossary.getTypeTerm(typeFqn).title);
                    const label = Jig.glossary.getMethodTerm(ep.fqn, true).title;
                    builder.addNodeToSubgraph(subgraph, fqnToNodeId(ep.fqn), label, 'method');
                    builder.addClass(fqnToNodeId(ep.fqn), "inbound");
                });

                // パスノードとdotted edge
                controller.entrypoints.forEach(ep => {
                    const pathNodeId = Jig.fqnToId("path", ep.fqn);
                    builder.addNode(pathNodeId, ep.path, '>"$LABEL"]');
                    builder.addEdge(pathNodeId, fqnToNodeId(ep.fqn), "", true);
                });

                // usecaseサブグラフ（allFqns × usecaseMethodToType の交差）
                const usecaseGroups = new Map();
                allFqns.forEach(fqn => {
                    if (!entrypointFqns.has(fqn) && usecaseMethodToType.has(fqn)) {
                        const typeFqn = usecaseMethodToType.get(fqn);
                        if (!usecaseGroups.has(typeFqn)) usecaseGroups.set(typeFqn, []);
                        usecaseGroups.get(typeFqn).push(fqn);
                    }
                });
                usecaseGroups.forEach((methods, typeFqn) => {
                    const sgLabel = Jig.glossary.getTypeTerm(typeFqn).title;
                    const subgraph = builder.startSubgraph(Jig.fqnToId("sg", typeFqn), sgLabel);
                    methods.forEach(fqn => {
                        const mId = fqnToNodeId(fqn);
                        const mLabel = Jig.glossary.getMethodTerm(fqn, true).title;
                        builder.addNodeToSubgraph(subgraph, mId, mLabel, 'method');
                        builder.addClass(mId, "usecase");
                        builder.addClick(mId, `./usecase.html#${Jig.fqnToId("method", fqn)}`);
                    });
                });

                // methodノード（entrypointでもusecaseでもないFQN）→ クラス単位のsubgraph
                const methodGroups = new Map();
                allFqns.forEach(fqn => {
                    if (!entrypointFqns.has(fqn) && !usecaseMethodToType.has(fqn)) {
                        const typeFqn = fqn.split('#')[0];
                        const subgraph = builder.ensureSubgraph(methodGroups, Jig.fqnToId("sg", typeFqn), Jig.glossary.getTypeTerm(typeFqn).title);
                        const label = Jig.glossary.getMethodTerm(fqn, true).title;
                        const nodeId = fqnToNodeId(fqn);
                        builder.addNodeToSubgraph(subgraph, nodeId, label, 'method');
                        builder.addClass(nodeId, "inactive");
                    }
                });

                // アダプターノードセット（entrypoint + 非usecase メソッド）
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

                // エッジ
                controller.relations.forEach(edge => {
                    const edgeLength = edgeLengthByKey.get(`${edge.from}::${edge.to}`) || 1;
                    builder.addEdge(fqnToNodeId(edge.from), fqnToNodeId(edge.to), "", false, edgeLength);
                });

                const generator = (dir) => builder.build(dir);
                if (generator('LR')) {
                    mmdContainer.innerHTML = "";
                    Jig.mermaid.render.renderWithControls(mmdContainer, generator, {direction: 'LR'});
                }
            });

            container.appendChild(section);
        });
    }

    return {
        init,
        parseInboundData,
        render,
        renderSidebar,
        renderControllerList,
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
