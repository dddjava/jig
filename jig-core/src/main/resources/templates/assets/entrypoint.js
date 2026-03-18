// ===== UI ユーティリティ =====

function createElement(tagName, options = {}) {
    const element = document.createElement(tagName);
    if (options.className) element.className = options.className;
    if (options.id) element.id = options.id;
    if (options.textContent) element.textContent = options.textContent;
    if (options.innerHTML) element.innerHTML = options.innerHTML;
    if (options.attributes) {
        for (const [key, value] of Object.entries(options.attributes)) {
            element.setAttribute(key, value);
        }
    }
    if (options.style) {
        Object.assign(element.style, options.style);
    }
    if (options.children) {
        options.children.forEach(child => {
            if (child) element.appendChild(child);
        });
    }
    return element;
}

function createSidebarSection(title, items) {
    if (items.length === 0) return null;

    return createElement("section", {
        className: "in-page-sidebar__section",
        children: [
            createElement("h3", {
                className: "in-page-sidebar__section-title",
                textContent: title
            }),
            createElement("ul", {
                className: "in-page-sidebar__links",
                children: items.map(({id, label}) => createElement("li", {
                    className: "in-page-sidebar__item",
                    children: [
                        createElement("a", {
                            className: "in-page-sidebar__link",
                            attributes: {href: "#" + id},
                            textContent: label
                        })
                    ]
                }))
            })
        ]
    });
}

function renderSidebarSection(container, title, items) {
    if (!container) return;
    const section = createSidebarSection(title, items);
    if (section) {
        container.appendChild(section);
    }
}

function lazyRender(container, renderFn) {
    if (typeof IntersectionObserver === "undefined") {
        renderFn();
        return;
    }

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                renderFn();
                observer.unobserve(container);
            }
        });
    }, {rootMargin: "200px"});
    observer.observe(container);
}

// ===== Mermaid ダイアグラム生成 =====

class MermaidBuilder {
    constructor() {
        this.nodes = [];
        this.edges = [];
        this.subgraphs = [];
        this.edgeSet = new Set();
    }

    sanitize(id) {
        return (id || "").replace(/[^a-zA-Z0-9]/g, '_');
    }

    addNode(id, label, shape = '["$LABEL"]') {
        const escapedLabel = (label || "").replace(/"/g, '\\"');
        const nodeLine = `${id}${shape.replace('$LABEL', escapedLabel)}`;
        if (!this.nodes.includes(nodeLine)) {
            this.nodes.push(nodeLine);
        }
        return id;
    }

    addEdge(from, to, label = "", dotted = false) {
        const edgeType = dotted ? "-.->" : "-->";
        const edgeKey = `${from}--${label}--${edgeType}-->${to}`;
        if (!this.edgeSet.has(edgeKey)) {
            this.edgeSet.add(edgeKey);
            const edgeLine = label ? `  ${from} -- "${label}" ${edgeType} ${to}` : `  ${from} ${edgeType} ${to}`;
            this.edges.push(edgeLine);
        }
    }

    startSubgraph(id, label) {
        const subgraph = {id, label, lines: []};
        this.subgraphs.push(subgraph);
        return subgraph;
    }

    addNodeToSubgraph(subgraph, id, label, shape = '["$LABEL"]') {
        const escapedLabel = (label || "").replace(/"/g, '\\"');
        const nodeLine = `    ${id}${shape.replace('$LABEL', escapedLabel)}`;
        if (!subgraph.lines.includes(nodeLine)) {
            subgraph.lines.push(nodeLine);
        }
        return id;
    }

    build(direction = 'LR') {
        let code = `graph ${direction}\n`;
        this.subgraphs.forEach(sg => {
            code += `  subgraph ${sg.id} ["${sg.label}"]\n`;
            sg.lines.forEach(line => {
                code += `    ${line.trim()}\n`;
            });
            code += `  end\n`;
        });
        this.nodes.forEach(node => {
            code += `  ${node.trim()}\n`;
        });
        this.edges.forEach(edge => {
            code += `${edge}\n`;
        });
        return code;
    }

    isEmpty() {
        return this.nodes.length === 0 && this.edges.length === 0 && this.subgraphs.length === 0;
    }
}

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
        renderSidebarSection(sidebar, "コントローラー", items);
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
            const section = createElement("section", {
                className: "type",
                id: controller.fqn,
                children: [
                    createElement("h2", {
                        children: [createElement("a", {textContent: controller.label})]
                    }),
                    createElement("div", {
                        className: "fully-qualified-name",
                        textContent: controller.fqn
                    })
                ]
            });

            if (controller.description) {
                section.appendChild(createElement("section", {
                    className: "markdown",
                    innerHTML: marked.parse(controller.description)
                }));
            }

            controller.entrypoints.forEach(ep => {
                const epSection = createElement("section", {
                    className: "entrypoint",
                    children: [
                        createElement("h3", {textContent: ep.label}),
                        createElement("p", {textContent: "Path: " + ep.path})
                    ]
                });

                const mmdContainer = createElement("div", {className: "mermaid-diagram"});
                epSection.appendChild(mmdContainer);

                lazyRender(mmdContainer, () => {
                    const builder = new MermaidBuilder();
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
                        });
                    });

                    ep.graph.edges.forEach(edge => {
                        builder.addEdge(edge.from, edge.to, "", edge.style === 'dotted');
                    });

                    let code = builder.build('LR');
                    if (code) {
                        // Click events for services
                        ep.graph.serviceGroups.forEach(sg => {
                            sg.methods.forEach(m => {
                                if (m.link) {
                                    code += `\n  click ${m.id} "./usecase.html#${m.link}"`;
                                }
                            });
                        });

                        const mmdPre = createElement("pre", {
                            className: "mermaid",
                            textContent: code
                        });
                        mmdContainer.appendChild(mmdPre);

                        if (window.mermaid) {
                            mermaid.run({nodes: [mmdPre]});
                        }
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
