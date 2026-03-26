const createElement = globalThis.Jig.dom.createElement;
const createElementForTypeRef = globalThis.Jig.dom.createElementForTypeRef;
const fqnToNodeId = (fqn) => globalThis.Jig.fqnToId("node", fqn);

function buildGraphFromCallMethods(rootMethod, methodMap) {
    const nodes = new Map();
    const edgeSet = new Set();
    const edges = [];
    const visited = new Set();

    nodes.set(rootMethod.fqn, {fqn: rootMethod.fqn, type: 'usecase'});
    visited.add(rootMethod.fqn);

    function traverse(callerFqn, callMethods) {
        if (!callMethods) return;
        for (const calleeFqn of callMethods) {
            if (!methodMap.has(calleeFqn)) continue;
            const edgeKey = callerFqn + '\u2192' + calleeFqn;
            if (!edgeSet.has(edgeKey)) {
                edgeSet.add(edgeKey);
                edges.push({from: callerFqn, to: calleeFqn});
            }
            if (!visited.has(calleeFqn)) {
                visited.add(calleeFqn);
                nodes.set(calleeFqn, {fqn: calleeFqn, type: 'usecase'});
                traverse(calleeFqn, methodMap.get(calleeFqn).callMethods);
            }
        }
    }

    traverse(rootMethod.fqn, rootMethod.callMethods);
    return {nodes: [...nodes.values()], edges};
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

        const items = usecases.map(c => {
            return {id: c.fqn, label: globalThis.Jig.glossary.getTypeTerm(c.fqn).title};
        });
        globalThis.Jig.sidebar.renderSection(sidebar, "ユースケース", items);
    },

    renderUsecaseList(usecases) {
        const container = document.getElementById("usecase-list");
        if (!container) return;
        container.innerHTML = "";

        if (!usecases || usecases.length === 0) {
            container.textContent = "データなし";
            return;
        }

        const methodMap = new Map();
        usecases.forEach(usecase => {
            (usecase.methods || []).forEach(m => methodMap.set(m.fqn, m));
            (usecase.staticMethods || []).forEach(m => methodMap.set(m.fqn, m));
        });

        usecases.forEach(usecase => {
            const term = globalThis.Jig.glossary.getTypeTerm(usecase.fqn);
            const section = createElement("section", {
                className: "jig-card jig-card--type",
                children: [
                    createElement("h3", {
                        children: [createElement("a", {id: usecase.fqn, textContent: term.title})]
                    }),
                    createElement("div", {
                        className: "fully-qualified-name",
                        textContent: usecase.fqn
                    })
                ]
            });

            if (term.description) {
                section.appendChild(createElement("section", {
                    className: "markdown",
                    innerHTML: globalThis.Jig.markdown.parse(term.description)
                }));
            }

            const fieldsList = globalThis.Jig.dom.createFieldsList(usecase.fields, createElementForTypeRef);
            if (fieldsList) section.appendChild(fieldsList);

            if (usecase.staticMethods && usecase.staticMethods.length > 0) {
                const staticList = globalThis.Jig.dom.createMethodsList("staticメソッド", usecase.staticMethods, createElementForTypeRef);
                if (staticList) {
                    staticList.classList.add("static-methods");
                    section.appendChild(staticList);
                }
            }

            usecase.methods.forEach(method => {
                const methodTerm = globalThis.Jig.glossary.getMethodTerm(method.fqn);
                const methodDescription = methodTerm.description;

                const methodSection = createElement("article", {
                    className: "jig-card jig-card--item",
                    children: [
                        createElement("h4", {id: method.fqn, textContent: methodTerm.title}),
                        createElement("div", {
                            className: "fully-qualified-name",
                            textContent: method.declaration
                        })
                    ]
                });

                // Mermaid Graph
                const graph = buildGraphFromCallMethods(method, methodMap);
                if (graph.edges.length > 0) {
                    const mmdContainer = createElement("div", {className: "mermaid-diagram"});
                    // Add directly to section before rendering mermaid to ensure layout
                    methodSection.appendChild(mmdContainer);

                    globalThis.Jig.observe.lazyRender(mmdContainer, () => {
                        const builder = new globalThis.Jig.mermaid.Builder();

                        graph.nodes.forEach(node => {
                            const shape = '(["$LABEL"])';
                            const nodeLabel = globalThis.Jig.glossary.getMethodTerm(node.fqn, true).title;

                            const nodeId = fqnToNodeId(node.fqn);
                            builder.addNode(nodeId, nodeLabel, shape);

                            // 自身を強調表示
                            if (node.fqn === method.fqn) {
                                builder.addStyle(nodeId, "font-weight:bold");
                            }
                            // ページ内リンク
                            builder.addClick(nodeId, "#" + node.fqn);
                        });

                        graph.edges.forEach(edge => {
                            builder.addEdge(fqnToNodeId(edge.from), fqnToNodeId(edge.to));
                        });

                        const code = builder.build('LR');
                        mmdContainer.innerHTML = ''; // clear loading state if any
                        globalThis.Jig.mermaid.renderWithControls(mmdContainer, code);
                    });
                }

                const dl = createElement("dl", { className: "depends" });
                
                if (method.parameterTypeRefs.length > 0) {
                     dl.appendChild(createElement("dt", { textContent: "要求するもの（引数）" }));
                     method.parameterTypeRefs.forEach(parameterTypeRef => {
                         dl.appendChild(createElement("dd", { children: [createElementForTypeRef(parameterTypeRef)] }));
                     });
                }

                if (method.returnTypeRef.fqn !== 'void') {
                    dl.appendChild(createElement("dt", { textContent: "得られるもの（戻り値）" }));
                    dl.appendChild(createElement("dd", { children: [createElementForTypeRef(method.returnTypeRef)] }));
                }
                
                methodSection.appendChild(dl);

                // Method Description
                if (methodDescription) {
                    methodSection.appendChild(createElement("section", {
                        className: "description markdown",
                        innerHTML: globalThis.Jig.markdown.parse(methodDescription)
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
