const createElement = globalThis.Jig.dom.createElement;

// ===== アプリケーション本体 =====

const InboundApp = {
    state: {
        data: null
    },

    init() {
        this.state.data = globalThis.inboundData;
        if (!this.state.data) return;

        this.render();
    },

    render() {
        const controllers = this.state.data.controllers;
        this.renderSidebar(controllers);
        this.renderControllerList(controllers);
    },

    renderSidebar(controllers) {
        const sidebar = document.getElementById("inbound-sidebar-list");
        if (!sidebar) return;
        sidebar.innerHTML = "";

        const items = controllers.map(c => ({
            id: c.fqn,
            label: globalThis.Jig.glossary.getTypeTerm(c.fqn).title
        }));
        globalThis.Jig.sidebar.renderSection(sidebar, "コントローラー", items);
    },

    renderControllerList(controllers) {
        const container = document.getElementById("inbound-list");
        if (!container) return;
        container.innerHTML = "";

        if (!controllers || controllers.length === 0) {
            container.textContent = "データなし";
            return;
        }

        controllers.forEach(controller => {
            const typeTerm = globalThis.Jig.glossary.getTypeTerm(controller.fqn);
            const section = createElement("section", {
                className: "jig-card jig-card--type",
                id: controller.fqn,
                children: [
                    createElement("h3", {
                        children: [createElement("a", {textContent: typeTerm.title})]
                    }),
                    createElement("div", {
                        className: "fully-qualified-name",
                        textContent: controller.fqn
                    })
                ]
            });

            if (typeTerm.description) {
                section.appendChild(createElement("section", {
                    className: "markdown",
                    innerHTML: globalThis.Jig.markdown.parse(typeTerm.description)
                }));
            }

            controller.entrypoints.forEach(ep => {
                const methodTerm = globalThis.Jig.glossary.getMethodTerm(ep.fqn, true);
                const epSection = createElement("article", {
                    className: "jig-card jig-card--item",
                    children: [
                        createElement("h4", {id: ep.fqn, textContent: methodTerm.title}),
                        createElement("div", {
                            className: "fully-qualified-name",
                            textContent: ep.path
                        })
                    ]
                });

                const mmdContainer = createElement("div", {className: "mermaid-diagram"});
                epSection.appendChild(mmdContainer);

                globalThis.Jig.observe.lazyRender(mmdContainer, () => {
                    const fqnToNodeId = (fqn) => globalThis.Jig.fqnToId("n", fqn);
                    const builder = new globalThis.Jig.mermaid.Builder();

                    ep.graph.nodes.forEach(node => {
                        const nodeId = fqnToNodeId(node.fqn);
                        const label = globalThis.Jig.glossary.getMethodTerm(node.fqn, true).title;
                        let shape = '["$LABEL"]';
                        if (node.type === 'entrypoint') shape = '{{"$LABEL"}}';
                        builder.addNode(nodeId, label, shape);
                    });

                    // パスノードとdotted edgeをJS側で生成
                    const pathNodeId = globalThis.Jig.fqnToId("path", ep.fqn);
                    builder.addNode(pathNodeId, ep.path, '>"$LABEL"]');
                    builder.addEdge(pathNodeId, fqnToNodeId(ep.fqn), "", true);

                    ep.graph.serviceGroups.forEach(sg => {
                        const sgLabel = globalThis.Jig.glossary.getTypeTerm(sg.fqn).title;
                        const subgraph = builder.startSubgraph(globalThis.Jig.fqnToId("sg", sg.fqn), sgLabel);
                        sg.methods.forEach(m => {
                            const mId = fqnToNodeId(m.fqn);
                            const mLabel = globalThis.Jig.glossary.getMethodTerm(m.fqn, true).title;
                            builder.addNodeToSubgraph(subgraph, mId, mLabel, '(["$LABEL"])');
                            builder.addClick(mId, `./usecase.html#${m.fqn}`);
                        });
                    });

                    ep.graph.edges.forEach(edge => {
                        builder.addEdge(fqnToNodeId(edge.from), fqnToNodeId(edge.to));
                    });

                    const code = builder.build('LR');
                    if (code) {
                        mmdContainer.innerHTML = "";
                        globalThis.Jig.mermaid.renderWithControls(mmdContainer, code);
                    }
                });

                section.appendChild(epSection);
            });

            container.appendChild(section);
        });
    }
};

if (typeof document !== 'undefined') {
    document.addEventListener("DOMContentLoaded", () => {
        InboundApp.init();
    });
}

// Test-only exports for Node; no-op in browsers.
if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        InboundApp
    };
}
