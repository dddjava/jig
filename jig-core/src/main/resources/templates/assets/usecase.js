const createElement = globalThis.Jig.dom.createElement;
const createElementForTypeRef = globalThis.Jig.dom.createElementForTypeRef;
const fqnToNodeId = (fqn) => globalThis.Jig.fqnToId("node", fqn);

function getClassFqnFromMethodFqn(fqn) {
    const hashIdx = fqn.indexOf('#');
    return hashIdx === -1 ? fqn : fqn.slice(0, hashIdx);
}

function isUsecase(method) {
    return method.visibility === "PUBLIC";
}

function buildOutboundOperationSet(outboundData) {
    if (!outboundData?.outboundPorts) return new Set();
    const set = new Set();
    outboundData.outboundPorts.forEach(port => {
        (port.operations || []).forEach(op => set.add(op.fqn));
    });
    return set;
}

function buildGraphFromCallMethods(rootMethod, methodMap, outboundOperationSet = new Set(), showDiagramInternalMethods , hideExternalPorts = false) {
    const nodes = new Map();
    const edgeSet = new Set();
    const edges = [];
    const visited = new Set();

    nodes.set(rootMethod.fqn, {fqn: rootMethod.fqn, kind: "usecase"});
    visited.add(rootMethod.fqn);

    function traverse(effectiveCallerFqn, callMethods, inliningPath = new Set()) {
        if (!callMethods) return;
        for (const calleeFqn of callMethods) {
            if (methodMap.has(calleeFqn)) {
                const m = methodMap.get(calleeFqn);
                const isUc = m.kind === "usecase";
                if (showDiagramInternalMethods || isUc) {
                    const edgeKey = effectiveCallerFqn + '\u2192' + calleeFqn;
                    if (!edgeSet.has(edgeKey)) {
                        edgeSet.add(edgeKey);
                        edges.push({from: effectiveCallerFqn, to: calleeFqn});
                    }
                    if (!visited.has(calleeFqn)) {
                        visited.add(calleeFqn);
                        nodes.set(calleeFqn, {fqn: calleeFqn, kind: m.kind});
                        traverse(calleeFqn, m.callMethods, new Set());
                    }
                } else {
                    if (!inliningPath.has(calleeFqn)) {
                        const nextPath = new Set(inliningPath);
                        nextPath.add(calleeFqn);
                        traverse(effectiveCallerFqn, m.callMethods, nextPath);
                    }
                }
            } else if (outboundOperationSet.has(calleeFqn)) {
                if (hideExternalPorts) continue;
                const classFqn = getClassFqnFromMethodFqn(calleeFqn);
                const edgeKey = effectiveCallerFqn + '\u2192' + classFqn;
                if (!edgeSet.has(edgeKey)) {
                    edgeSet.add(edgeKey);
                    edges.push({from: effectiveCallerFqn, to: classFqn});
                }
                if (!nodes.has(classFqn)) {
                    nodes.set(classFqn, {fqn: classFqn, kind: "external"});
                }
            }
        }
    }

    traverse(rootMethod.fqn, rootMethod.callMethods);
    return {nodes: [...nodes.values()], edges};
}

function buildSequenceFromCallMethods(rootMethod, methodMap, outboundOperationSet = new Set(), hideNonUsecases = false, hideExternalPorts = false) {
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

    function ensureUsecaseParticipant(key) {
        const label = globalThis.Jig.glossary.getMethodTerm(key, true).title
        return ensureParticipant(key, label, "usecase");
    }

    function ensureParticipant(key, label, kind) {
        if (!participants.has(key)) {
            participants.set(key, {id: fqnToNodeId(key), label, kind});
            participantKeys.push(key);
        }
        return participants.get(key);
    }

    ensureUsecaseParticipant(rootMethod.fqn);
    visited.add(rootMethod.fqn);

    function traverse(effectiveCallerFqn, callMethods, inliningPath = new Set()) {
        if (!callMethods) return;
        for (const calleeFqn of callMethods) {
            const caller = participants.get(effectiveCallerFqn);
            if (methodMap.has(calleeFqn)) {
                const m = methodMap.get(calleeFqn);
                const isUc = m.kind === "usecase";
                if (!hideNonUsecases || isUc) {
                    const callee = ensureUsecaseParticipant(calleeFqn);
                    calls.push({from: caller.id, to: callee.id, label: ''});
                    if (!visited.has(calleeFqn)) {
                        visited.add(calleeFqn);
                        traverse(calleeFqn, m.callMethods, new Set());
                    }
                } else {
                    if (!inliningPath.has(calleeFqn)) {
                        const nextPath = new Set(inliningPath);
                        nextPath.add(calleeFqn);
                        traverse(effectiveCallerFqn, m.callMethods, nextPath);
                    }
                }
            } else if (outboundOperationSet.has(calleeFqn)) {
                if (hideExternalPorts) continue;
                const classFqn = getClassFqnFromMethodFqn(calleeFqn);
                const methodName = getMethodSimpleName(calleeFqn);
                const callee = ensureParticipant(classFqn,  globalThis.Jig.glossary.getTypeTerm(classFqn).title, "external");
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

    const external = sequence.participants.filter(p => p.kind === "external");
    const internal = sequence.participants.filter(p => p.kind !== "external");

    internal.forEach(p => {
        code += `  participant ${p.id} as ${p.label}\n`;
    });

    if (external.length > 0) {
        code += '  box outbounds\n';
        external.forEach(p => { code += `    participant ${p.id} as ${p.label}\n`; });
        code += '  end\n';
    }

    sequence.calls.forEach(call => {
        code += `  ${call.from}->>${call.to}: ${call.label}\n`;
    });

    return code;
}

// ===== アプリケーション本体 =====

const UsecaseApp = {
    state: {
        data: null,
        selectedTabs: new Map() // methodFqn -> 'graph' | 'sequence'
    },

    init() {
        this.state.data = globalThis.usecaseData;
        if (!this.state.data) return;

        const domainData = globalThis.domainData;
        if (domainData && domainData.types) {
            if (!domainData._typesMap) {
                domainData._typesMap = new Map(domainData.types.map(t => [t.fqn, t]));
            }
            globalThis.Jig.dom.typeLinkResolver = (fqn) => {
                const domainType = domainData._typesMap.get(fqn);
                if (domainType) {
                    return {
                        href: 'domain.html#' + fqn,
                        className: domainType.isDeprecated ? 'deprecated' : undefined
                    };
                }
                return null;
            };
        }

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
            { id: 'show-declarations', class: 'hide-usecase-declarations' },
            { id: 'show-diagram-internal-methods', reRender: true },
            { id: 'hide-external-ports', reRender: true }
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
                if (control.class) {
                    document.body.classList.toggle(control.class, !checkbox.checked);
                }
                localStorage.setItem(storageKey, checkbox.checked);
                if (control.reRender) {
                    this.render();
                }
            };

            checkbox.addEventListener('change', update);
            update();
        });
    },

    render() {
        const scrollInfo = this.getScrollInfo();
        const usecases = this.state.data.usecases;
        this.renderSidebar(usecases);
        this.renderUsecaseList(usecases);
        this.restoreScroll(scrollInfo);
    },

    getScrollInfo() {
        const main = document.querySelector('.split-view > main');
        if (!main) return null;

        const elements = main.querySelectorAll('.jig-card--type h3 a[id], .jig-card--item h4[id]');
        const containerRect = main.getBoundingClientRect();

        for (const el of elements) {
            const rect = el.getBoundingClientRect();
            if (rect.top >= containerRect.top) {
                return { id: el.id, offset: rect.top - containerRect.top };
            }
        }
        return { scrollTop: main.scrollTop };
    },

    restoreScroll(info) {
        const main = document.querySelector('.split-view > main');
        if (!main || !info) return;

        if (info.id) {
            const el = document.getElementById(info.id);
            if (el) {
                const containerRect = main.getBoundingClientRect();
                const newRect = el.getBoundingClientRect();
                main.scrollTop += (newRect.top - containerRect.top - info.offset);
                return;
            }
        }

        if (info.scrollTop !== undefined) {
            main.scrollTop = info.scrollTop;
        }
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
                        if (usecase.methods.length > 0) {
                            children.push(createElement("ul", {
                                className: "in-page-sidebar__links",
                                children: usecase.methods
                                    .filter(isUsecase)
                                    .map(method =>
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
            (usecase.methods || []).forEach(m => methodMap.set(m.fqn, {...m, kind: isUsecase(m) ? "usecase" : "method"}));
            (usecase.staticMethods || []).forEach(m => methodMap.set(m.fqn, {...m, kind: "static-method"}));
        });

        const outboundOperationSet = buildOutboundOperationSet(globalThis.outboundData);
        const showDiagramInternalMethods = document.getElementById('show-diagram-internal-methods')?.checked || false;
        const hideExternalPorts = document.getElementById('hide-external-ports')?.checked || false;

        usecases.forEach(usecase => {
            const term = globalThis.Jig.glossary.getTypeTerm(usecase.fqn);
            const section = createElement("section", {
                className: "jig-card jig-card--type",
                children: [
                    createElement("h3", {
                        children: [createElement("a", {id: usecase.fqn, textContent: term.title})]
                    }),
                    createElement("div", {
                        className: "declaration",
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

            if (usecase.staticMethods.length > 0) {
                const staticList = globalThis.Jig.dom.createMethodsList("staticメソッド", usecase.staticMethods, createElementForTypeRef);
                if (staticList) {
                    staticList.classList.add("static-methods");
                    section.appendChild(staticList);
                }
            }

            // usecaseとするのはPUBLICのみ
            const internalMethods = usecase.methods.filter(method => !isUsecase(method))
            if (internalMethods.length > 0) {
                const staticList = globalThis.Jig.dom.createMethodsList("メソッド", internalMethods, createElementForTypeRef);
                if (staticList) {
                    staticList.classList.add("methods");
                    section.appendChild(staticList);
                }
            }

            usecase.methods.filter(isUsecase).forEach(method => {
                const methodTerm = globalThis.Jig.glossary.getMethodTerm(method.fqn);
                const methodDescription = methodTerm.description;

                const methodSection = createElement("article", {
                    className: "jig-card jig-card--item",
                    children: [
                        createElement("h4", {id: method.fqn, textContent: methodTerm.title}),
                        createElement("div", {
                            className: "declaration",
                            textContent: methodTerm.shortDeclaration
                        })
                    ]
                });

                // Diagrams
                const graph = buildGraphFromCallMethods(method, methodMap, outboundOperationSet, showDiagramInternalMethods, hideExternalPorts);
                const hasGraph = graph.edges.length > 0;

                const sequence = buildSequenceFromCallMethods(method, methodMap, outboundOperationSet, showDiagramInternalMethods, hideExternalPorts);
                const seqCode = buildSequenceDiagramCode(sequence);
                const hasSequence = seqCode !== null;

                if (hasGraph || hasSequence) {
                    const diagramContainer = createElement("div", {className: "diagram-container"});
                    methodSection.appendChild(diagramContainer);

                    let graphPanel = null;
                    let seqPanel = null;

                    if (hasGraph && hasSequence) {
                        const selectedTab = this.state.selectedTabs.get(method.fqn) || 'graph';
                        const isGraphActive = selectedTab === 'graph';

                        const graphBtn = createElement("button", {
                            className: "diagram-tab" + (isGraphActive ? " active" : ""),
                            textContent: "ユースケース図"
                        });
                        const seqBtn = createElement("button", {
                            className: "diagram-tab" + (!isGraphActive ? " active" : ""),
                            textContent: "シーケンス図"
                        });
                        diagramContainer.appendChild(createElement("div", {
                            className: "diagram-tabs",
                            children: [graphBtn, seqBtn]
                        }));

                        graphPanel = createElement("div", {className: "diagram-panel" + (isGraphActive ? "" : " hidden")});
                        seqPanel = createElement("div", {className: "diagram-panel" + (!isGraphActive ? "" : " hidden")});

                        graphBtn.addEventListener('click', () => {
                            graphBtn.classList.add('active');
                            seqBtn.classList.remove('active');
                            graphPanel.classList.remove('hidden');
                            seqPanel.classList.add('hidden');
                            this.state.selectedTabs.set(method.fqn, 'graph');
                        });
                        seqBtn.addEventListener('click', () => {
                            seqBtn.classList.add('active');
                            graphBtn.classList.remove('active');
                            seqPanel.classList.remove('hidden');
                            graphPanel.classList.add('hidden');
                            this.state.selectedTabs.set(method.fqn, 'sequence');
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
                                const nodeId = fqnToNodeId(node.fqn);
                                if (node.kind === "usecase") {
                                    // ユースケース: 角丸、ページ内リンク
                                    const shape = '(["$LABEL"])';
                                    const nodeLabel = globalThis.Jig.glossary.getMethodTerm(node.fqn, true).title;

                                    builder.addNode(nodeId, nodeLabel, shape);
                                    // 自身を強調表示
                                    if (node.fqn === method.fqn) {
                                        builder.addStyle(nodeId, "font-weight:bold");
                                    }
                                    builder.addClick(nodeId, "#" + node.fqn);
                                } else if (node.kind === "external") {
                                    // 外部ポート: 四角、グレー
                                    const shape = '["$LABEL"]';
                                    const nodeLabel = globalThis.Jig.glossary.getTypeTerm(node.fqn).title

                                    builder.addNode(nodeId, nodeLabel, shape);
                                    builder.addStyle(nodeId, "fill:#e0e0e0,stroke:#aaa");
                                } else {
                                    // その他(method or static-method): 角丸、グレー
                                    const shape = '(["$LABEL"])';
                                    const nodeLabel = globalThis.Jig.glossary.getMethodTerm(node.fqn, true).title;

                                    builder.addNode(nodeId, nodeLabel, shape);
                                    builder.addStyle(nodeId, "fill:#e0e0e0,stroke:#aaa");
                                }
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
        buildOutboundOperationSet,
        buildGraphFromCallMethods,
        buildSequenceFromCallMethods,
        buildSequenceDiagramCode
    };
}
