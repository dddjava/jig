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

function createFieldsTable(fields) {
    const thead = createElement("thead", {
        children: [
            createElement("tr", {
                children: [
                    createElement("th", { attributes: { width: "20%" }, textContent: "フィールド" }),
                    createElement("th", { textContent: "フィールド型" })
                ]
            })
        ]
    });

    const tbody = createElement("tbody", {
        children: fields.map(field => createElement("tr", {
            children: [
                createElement("td", {
                    className: field.isDeprecated ? "deprecated" : "",
                    textContent: field.name
                }),
                createElement("td", { innerHTML: field.typeHtml })
            ]
        }))
    });

    return createElement("table", {
        className: "fields",
        children: [thead, tbody]
    });
}

function createMethodsTable(kind, methods) {
    const thead = createElement("thead", {
        children: [
            createElement("tr", {
                children: [
                    createElement("th", { attributes: { width: "20%" }, textContent: kind }),
                    createElement("th", { textContent: "引数" }),
                    createElement("th", { textContent: "戻り値型" }),
                    createElement("th", { textContent: "説明" })
                ]
            })
        ]
    });

    const tbody = createElement("tbody", {
        children: methods.map(method => createElement("tr", {
            children: [
                createElement("td", { className: "method-name", textContent: method.labelWithSymbol }),
                createElement("td", {
                    children: (method.argumentsLinks || []).map(argLink => createElement("span", {
                        className: "method-argument-item",
                        innerHTML: argLink
                    }))
                }),
                createElement("td", { innerHTML: method.returnTypeLink }),
                createElement("td", {
                    className: "markdown",
                    innerHTML: method.description ? marked.parse(method.description) : ""
                })
            ]
        }))
    });

    return createElement("table", { children: [thead, tbody] });
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
        this.styles = [];
        this.clicks = [];
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

    addStyle(id, style) {
        this.styles.push(`style ${id} ${style}`);
    }

    addClick(id, url) {
        this.clicks.push(`click ${id} "${url}"`);
    }

    addClass(id, className) {
        this.styles.push(`class ${id} ${className}`);
    }

    addClassDef(className, style) {
         this.styles.push(`classDef ${className} ${style}`);
    }

    build(direction = 'LR') {
        let code = `graph ${direction}\n`;
        this.nodes.forEach(node => {
            code += `  ${node.trim()}\n`;
        });
        this.edges.forEach(edge => {
            code += `${edge}\n`;
        });
        this.styles.forEach(style => {
            code += `${style}\n`;
        });
        this.clicks.forEach(click => {
            code += `${click}\n`;
        });
        return code;
    }
}

// ===== アプリケーション本体 =====

const UsecaseApp = {
    state: {
        data: null
    },

    init() {
        this.state.data = globalThis.usecaseData;
        if (!this.state.data) return;

        this.initControls();
        this.render();
    },

    initControls() {
        const controls = [
            { id: 'show-fields', class: 'hide-usecase-fields' },
            { id: 'show-static-methods', class: 'hide-usecase-static-methods' },
            { id: 'show-diagrams', class: 'hide-usecase-diagrams' },
            { id: 'show-details', class: 'hide-usecase-details' },
            { id: 'show-descriptions', class: 'hide-usecase-descriptions' },
            { id: 'show-declarations', class: 'hide-usecase-declarations' }
        ];

        controls.forEach(control => {
            const checkbox = document.getElementById(control.id);
            if (!checkbox) return;

            const storageKey = `jig-usecase-${control.id}`;
            const savedValue = localStorage.getItem(storageKey);
            
            if (savedValue !== null) {
                checkbox.checked = savedValue === 'true';
            }

            const update = () => {
                document.body.classList.toggle(control.class, !checkbox.checked);
                localStorage.setItem(storageKey, checkbox.checked);
            };

            checkbox.addEventListener('change', update);
            update();
        });
    },

    render() {
        const usecases = this.state.data.usecases;
        this.renderSidebar(usecases);
        this.renderUsecaseList(usecases);
    },

    renderSidebar(usecases) {
        const sidebar = document.getElementById("usecase-sidebar-list");
        if (!sidebar) return;
        sidebar.innerHTML = "";

        const items = usecases.map(c => ({id: c.typeId, label: c.label}));
        renderSidebarSection(sidebar, "ユースケース", items);
    },

    renderUsecaseList(usecases) {
        const container = document.getElementById("usecase-list");
        if (!container) return;
        container.innerHTML = "";

        if (!usecases || usecases.length === 0) {
            container.textContent = "データなし";
            return;
        }

        usecases.forEach(usecase => {
            const section = createElement("section", {
                className: "type",
                children: [
                    createElement("h2", {
                        children: [createElement("a", {id: usecase.typeId, textContent: usecase.label})]
                    }),
                    createElement("div", {
                        className: "fully-qualified-name",
                        textContent: usecase.typeId
                    })
                ]
            });

            if (usecase.description) {
                section.appendChild(createElement("section", {
                    className: "markdown",
                    innerHTML: marked.parse(usecase.description)
                }));
            }

            if (usecase.fields && usecase.fields.length > 0) {
                section.appendChild(createFieldsTable(usecase.fields));
            }

            if (usecase.staticMethods && usecase.staticMethods.length > 0) {
                const staticMethodsTable = createMethodsTable("staticメソッド", usecase.staticMethods);
                staticMethodsTable.classList.add("static-methods");
                section.appendChild(staticMethodsTable);
            }

            usecase.methods.forEach(method => {
                const methodSection = createElement("section", {
                    className: "method",
                    children: [
                        createElement("h3", {id: method.methodId, textContent: method.label}),
                        createElement("div", {
                            className: "fully-qualified-name",
                            textContent: method.declaration
                        })
                    ]
                });

                // Mermaid Graph
                if (method.graph && (method.graph.nodes.length > 0 || method.graph.edges.length > 0)) {
                    const mmdContainer = createElement("div", {className: "mermaid-diagram"});
                    // Add directly to section before rendering mermaid to ensure layout
                    methodSection.appendChild(mmdContainer);

                    lazyRender(mmdContainer, () => {
                        const builder = new MermaidBuilder();

                        // Add class definitions
                        builder.addClassDef("others", "fill:#AAA,font-size:90%;");
                        builder.addClassDef("lambda", "fill:#999,font-size:80%;");

                        method.graph.nodes.forEach(node => {
                            let shape = '["$LABEL"]';
                            if (node.type === 'usecase') shape = '(["$LABEL"])';
                            else if (node.type === 'lambda') shape = '["$LABEL"]';
                            
                            builder.addNode(node.id, node.label, shape);

                            if (node.highlight) {
                                builder.addStyle(node.id, "font-weight:bold");
                            }
                            if (node.link) {
                                builder.addClick(node.id, "#" + node.link);
                            }
                            if (node.type === 'other') {
                                builder.addClass(node.id, "others");
                            }
                            if (node.type === 'lambda') {
                                builder.addClass(node.id, "lambda");
                            }
                        });

                        method.graph.edges.forEach(edge => {
                            builder.addEdge(edge.from, edge.to);
                        });

                        const code = builder.build('LR');
                        const mmdPre = createElement("pre", {
                            className: "mermaid",
                            textContent: code
                        });
                        mmdContainer.innerHTML = ''; // clear loading state if any
                        mmdContainer.appendChild(mmdPre);
                        
                        // We use global mermaid.run if available, scanning the newly added element
                        if (window.mermaid) {
                            mermaid.run({ nodes: [mmdPre] });
                        }
                    });
                }

                // Arguments and Return
                const dl = createElement("dl", { className: "depends" });
                
                // Arguments
                if (method.argumentsLinks && method.argumentsLinks.length > 0) {
                     dl.appendChild(createElement("dt", { textContent: "要求するもの（引数）" }));
                     method.argumentsLinks.forEach(argLink => {
                         dl.appendChild(createElement("dd", { innerHTML: argLink }));
                     });
                }

                // Return
                if (method.returnTypeLink && method.returnTypeLink !== '<span class="weak">void</span>') {
                    dl.appendChild(createElement("dt", { textContent: "得られるもの（戻り値）" }));
                    dl.appendChild(createElement("dd", { innerHTML: method.returnTypeLink }));
                }
                
                methodSection.appendChild(dl);

                // Method Description
                if (method.description) {
                    methodSection.appendChild(createElement("section", {
                        className: "description markdown",
                        innerHTML: marked.parse(method.description)
                    }));
                }

                section.appendChild(methodSection);
            });

            container.appendChild(section);
        });
    }
};

if (typeof document !== 'undefined') {
    document.addEventListener("DOMContentLoaded", () => {
        UsecaseApp.init();
    });
}

// Test-only exports for Node; no-op in browsers.
if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        UsecaseApp
    };
}
