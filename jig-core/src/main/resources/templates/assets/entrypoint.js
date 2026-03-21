// ===== アプリケーション本体 =====

const EntrypointApp = {
    state: {
        data: null
    },

    init() {
        this.state.data = globalThis.entrypointData;
        if (!this.state.data) return;

        this.render();
    },

    render() {
        const controllers = this.state.data.controllers;
        this.renderSidebar(controllers);
        this.renderControllerList(controllers);
    },

    renderSidebar(controllers) {
        const sidebar = document.getElementById("entrypoint-sidebar-list");
        if (!sidebar) return;
        sidebar.innerHTML = "";

        const items = controllers.map(c => ({id: c.fqn, label: c.label}));
        globalThis.Jig.sidebar.renderSection(sidebar, "コントローラー", items);
    },

    renderControllerList(controllers) {
        const container = document.getElementById("entrypoint-list");
        if (!container) return;
        container.innerHTML = "";

        if (!controllers || controllers.length === 0) {
            container.textContent = "データなし";
            return;
        }

        controllers.forEach(controller => {
            const section = globalThis.Jig.dom.createElement("section", {
                className: "jig-card jig-card--type",
                id: controller.fqn,
                children: [
                    globalThis.Jig.dom.createElement("h3", {
                        children: [globalThis.Jig.dom.createElement("a", {textContent: controller.label})]
                    }),
                    globalThis.Jig.dom.createElement("div", {
                        className: "fully-qualified-name",
                        textContent: controller.fqn
                    })
                ]
            });

            if (controller.description) {
                section.appendChild(globalThis.Jig.dom.createElement("section", {
                    className: "markdown",
                    innerHTML: globalThis.Jig.markdown.parse(controller.description)
                }));
            }

            controller.entrypoints.forEach(ep => {
                const epSection = globalThis.Jig.dom.createElement("article", {
                    className: "jig-card jig-card--item",
                    children: [
                        globalThis.Jig.dom.createElement("h4", {id: ep.methodId, textContent: ep.label}),
                        globalThis.Jig.dom.createElement("div", {
                            className: "fully-qualified-name",
                            textContent: ep.path
                        })
                    ]
                });

                const mmdContainer = globalThis.Jig.dom.createElement("div", {className: "mermaid-diagram"});
                epSection.appendChild(mmdContainer);

                globalThis.Jig.observe.lazyRender(mmdContainer, () => {
                    const builder = new globalThis.Jig.mermaid.Builder();
                    ep.graph.nodes.forEach(node => {
                        let shape = '["$LABEL"]';
                        if (node.type === 'entrypoint') shape = '{{"$LABEL"}}';
                        else if (node.type === 'path') shape = '>"$LABEL"]';
                        builder.addNode(node.id, node.label, shape);
                    });

                    ep.graph.serviceGroups.forEach(sg => {
                        const subgraph = builder.startSubgraph(builder.sanitize(sg.typeId), sg.label);
                        sg.methods.forEach(m => {
                            builder.addNodeToSubgraph(subgraph, m.id, m.label, '(["$LABEL"])');
                            if (m.link) {
                                builder.addClick(m.id, `./usecase.html#${m.link}`);
                            }
                        });
                    });

                    ep.graph.edges.forEach(edge => {
                        builder.addEdge(edge.from, edge.to, "", edge.style === 'dotted');
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
        EntrypointApp.init();
    });
}

// Test-only exports for Node; no-op in browsers.
if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        EntrypointApp
    };
}
