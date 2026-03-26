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

function buildSequenceFromCallMethods(rootMethod, methodMap) {
    const participantKeys = [];
    const participants = new Map();
    const calls = [];
    const visited = new Set();

    function getMethodSimpleName(fqn) {
        const hashIdx = fqn.indexOf('#');
        if (hashIdx === -1) return fqn;
        const parenIdx = fqn.indexOf('(', hashIdx);
        return parenIdx === -1 ? fqn.slice(hashIdx + 1) : fqn.slice(hashIdx + 1, parenIdx);
    }

    function getClassFqnFromMethodFqn(fqn) {
        const hashIdx = fqn.indexOf('#');
        return hashIdx === -1 ? fqn : fqn.slice(0, hashIdx);
    }

    function getSimpleClassName(classFqn) {
        const dotIdx = classFqn.lastIndexOf('.');
        return dotIdx === -1 ? classFqn : classFqn.slice(dotIdx + 1);
    }

    function ensureParticipant(key, label, isExternal) {
        if (!participants.has(key)) {
            participants.set(key, {id: fqnToNodeId(key), label, isExternal});
            participantKeys.push(key);
        }
        return participants.get(key);
    }

    ensureParticipant(rootMethod.fqn, getMethodSimpleName(rootMethod.fqn), false);
    visited.add(rootMethod.fqn);

    function traverse(callerFqn, callMethods) {
        if (!callMethods) return;
        for (const calleeFqn of callMethods) {
            const caller = participants.get(callerFqn);
            if (methodMap.has(calleeFqn)) {
                const callee = ensureParticipant(calleeFqn, getMethodSimpleName(calleeFqn), false);
                calls.push({from: caller.id, to: callee.id, label: ''});
                if (!visited.has(calleeFqn)) {
                    visited.add(calleeFqn);
                    traverse(calleeFqn, methodMap.get(calleeFqn).callMethods);
                }
            } else {
                const classFqn = getClassFqnFromMethodFqn(calleeFqn);
                const methodName = getMethodSimpleName(calleeFqn);
                const callee = ensureParticipant(classFqn, getSimpleClassName(classFqn), true);
                calls.push({from: caller.id, to: callee.id, label: methodName});
            }
        }
    }

    traverse(rootMethod.fqn, rootMethod.callMethods);

    return {
        participants: participantKeys.map(k => participants.get(k)),
        calls
    };
}

function buildSequenceDiagramCode(sequence) {
    if (sequence.calls.length === 0) return null;
    let code = 'sequenceDiagram\n';
    sequence.participants.forEach(p => {
        code += `  participant ${p.id} as ${p.label}\n`;
    });
    sequence.calls.forEach(call => {
        code += `  ${call.from}->>${call.to}: ${call.label}\n`;
    });
    return code;
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

        const section = createElement("section", {
            className: "in-page-sidebar__section",
            children: [
                createElement("p", {
                    className: "in-page-sidebar__title",
                    textContent: "ユースケース"
                }),
                createElement("ul", {
                    className: "in-page-sidebar__links",
                    children: usecases.map(usecase => {
                        const children = [
                            createElement("a", {
                                className: "in-page-sidebar__link",
                                attributes: {href: "#" + usecase.fqn},
                                textContent: globalThis.Jig.glossary.getTypeTerm(usecase.fqn).title
                            })
                        ];
                        if (usecase.methods && usecase.methods.length > 0) {
                            children.push(createElement("ul", {
                                className: "in-page-sidebar__links",
                                children: usecase.methods.map(method =>
                                    createElement("li", {
                                        className: "in-page-sidebar__item",
                                        children: [
                                            createElement("a", {
                                                className: "in-page-sidebar__link in-page-sidebar__link--sub",
                                                attributes: {href: "#" + method.fqn},
                                                textContent: globalThis.Jig.glossary.getMethodTerm(method.fqn).title
                                            })
                                        ]
                                    })
                                )
                            }));
                        }
                        return createElement("li", {
                            className: "in-page-sidebar__item",
                            children
                        });
                    })
                })
            ]
        });
        sidebar.appendChild(section);
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

                // Diagrams
                const graph = buildGraphFromCallMethods(method, methodMap);
                const hasGraph = graph.edges.length > 0;

                const sequence = buildSequenceFromCallMethods(method, methodMap);
                const seqCode = buildSequenceDiagramCode(sequence);
                const hasSequence = seqCode !== null;

                if (hasGraph || hasSequence) {
                    const diagramContainer = createElement("div", {className: "diagram-container"});
                    methodSection.appendChild(diagramContainer);

                    let graphPanel = null;
                    let seqPanel = null;

                    if (hasGraph && hasSequence) {
                        const graphBtn = createElement("button", {
                            className: "diagram-tab active",
                            textContent: "ユースケース図"
                        });
                        const seqBtn = createElement("button", {
                            className: "diagram-tab",
                            textContent: "シーケンス図"
                        });
                        diagramContainer.appendChild(createElement("div", {
                            className: "diagram-tabs",
                            children: [graphBtn, seqBtn]
                        }));

                        graphPanel = createElement("div", {className: "diagram-panel"});
                        seqPanel = createElement("div", {className: "diagram-panel hidden"});

                        graphBtn.addEventListener('click', () => {
                            graphBtn.classList.add('active');
                            seqBtn.classList.remove('active');
                            graphPanel.classList.remove('hidden');
                            seqPanel.classList.add('hidden');
                        });
                        seqBtn.addEventListener('click', () => {
                            seqBtn.classList.add('active');
                            graphBtn.classList.remove('active');
                            seqPanel.classList.remove('hidden');
                            graphPanel.classList.add('hidden');
                        });

                        diagramContainer.appendChild(graphPanel);
                        diagramContainer.appendChild(seqPanel);
                    }

                    if (hasGraph) {
                        const mmdContainer = createElement("div", {className: "mermaid-diagram"});
                        (graphPanel || diagramContainer).appendChild(mmdContainer);

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

                    if (hasSequence) {
                        const seqContainer = createElement("div", {className: "mermaid-diagram"});
                        (seqPanel || diagramContainer).appendChild(seqContainer);

                        globalThis.Jig.observe.lazyRender(seqContainer, () => {
                            globalThis.Jig.mermaid.renderWithControls(seqContainer, seqCode);
                        });
                    }
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
        UsecaseApp,
        buildSequenceFromCallMethods,
        buildSequenceDiagramCode
    };
}
