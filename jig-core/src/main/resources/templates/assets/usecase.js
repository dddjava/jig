const createElement = globalThis.Jig.dom.createElement;
const createElementForTypeRef = globalThis.Jig.dom.createElementForTypeRef;
const fqnToNodeId = (fqn) => globalThis.Jig.fqnToId("node", fqn);

/**
 * @typedef {Object} TypeRef
 * @property {string} fqn
 * @property {TypeRef[]} [typeArgumentRefs]
 */

/**
 * @typedef {Object} JigField
 * @property {string} name
 * @property {TypeRef} typeRef
 * @property {boolean} isDeprecated
 */

/**
 * @typedef {Object} UsecaseMethod
 * @property {string} fqn
 * @property {TypeRef[]} parameterTypeRefs
 * @property {TypeRef} returnTypeRef
 * @property {boolean} isDeprecated
 * @property {string[]} callMethods 呼び出しているメソッドのFQN
 */

/**
 * @typedef {Object} Usecase
 * @property {string} fqn
 * @property {JigField[]} fields
 * @property {UsecaseMethod[]} staticMethods
 * @property {UsecaseMethod[]} methods
 */

/**
 * @return {Object} UsecaseData
 * @property {Usecase[]} usecases
 */
function getUsecaseData() {
    return globalThis.usecaseData;
}

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

function buildGraphFromCallMethods(rootMethod, diagramContext) {
    /**
     * @type {Map<string, {fqn: string, kind: string}>}
     */
    const nodes = new Map();
    const edgeSet = new Set();
    /**
     * @type {[from: string, to: string]}
     */
    const edges = [];
    const visited = new Set();

    nodes.set(rootMethod.fqn, {fqn: rootMethod.fqn, kind: "usecase"});
    visited.add(rootMethod.fqn);

    function shouldIncludeMethodNode(kind) {
        return diagramContext.showDiagramInternalMethods || kind === "usecase";
    }

    const reverseCallerMap = new Map();

    function addReverseCaller(calleeFqn, callerNode) {
        if (!calleeFqn || !callerNode?.fqn) return;
        if (!reverseCallerMap.has(calleeFqn)) reverseCallerMap.set(calleeFqn, []);
        reverseCallerMap.get(calleeFqn).push(callerNode);
    }

    for (const method of diagramContext.methodMap.values()) {
        (method.callMethods || []).forEach(calleeFqn => {
            addReverseCaller(calleeFqn, {fqn: method.fqn, kind: method.kind});
        });
    }

    (globalThis.inboundData?.controllers || []).forEach(controller => {
        (controller.relations || []).forEach(relation => {
            if (!relation?.from || !relation?.to) return;
            const callerClassFqn = getClassFqnFromMethodFqn(relation.from);
            if (diagramContext.methodMap.has(callerClassFqn)) return;
            addReverseCaller(relation.to, {fqn: callerClassFqn, kind: "inbound-class"});
        });
    });

    /**
     * @returns {Map<string, string>} callerFqn -> kind
     */
    function collectVisibleCallers(rootFqn) {
        const callers = reverseCallerMap.get(rootFqn) || [];
        const visible = new Map();

        if (diagramContext.showDiagramInternalMethods) {
            callers.forEach(caller => {
                if (caller.fqn === rootFqn) return;
                if (!shouldIncludeMethodNode(caller.kind)) return;
                visible.set(caller.fqn, caller.kind);
            });
            return visible;
        }

        function collectUsecaseAncestors(startCaller) {
            const queue = [startCaller];
            const visitedCallerFqns = new Set([rootFqn]);
            const usecaseAncestors = new Map();

            while (queue.length > 0) {
                const current = queue.shift();
                if (!current || visitedCallerFqns.has(current.fqn)) continue;
                visitedCallerFqns.add(current.fqn);

                if (current.kind === "usecase") {
                    usecaseAncestors.set(current.fqn, current.kind);
                    continue;
                }

                const parents = reverseCallerMap.get(current.fqn) || [];
                parents.forEach(parent => queue.push(parent));
            }

            return usecaseAncestors;
        }

        callers.forEach(caller => {
            if (caller.fqn === rootFqn) return;
            if (caller.kind === "usecase") {
                visible.set(caller.fqn, caller.kind);
                return;
            }

            const usecaseAncestors = collectUsecaseAncestors(caller);
            if (usecaseAncestors.size > 0) {
                usecaseAncestors.forEach((kind, fqn) => visible.set(fqn, kind));
            } else {
                visible.set(caller.fqn, caller.kind);
            }
        });

        return visible;
    }

    collectVisibleCallers(rootMethod.fqn).forEach((kind, callerFqn) => {
        const edgeKey = callerFqn + '\u2192' + rootMethod.fqn;
        if (!edgeSet.has(edgeKey)) {
            edgeSet.add(edgeKey);
            edges.push({from: callerFqn, to: rootMethod.fqn});
        }
        if (!nodes.has(callerFqn)) {
            nodes.set(callerFqn, {fqn: callerFqn, kind});
        }
    });

    function traverse(effectiveCallerFqn, callMethods, inliningPath = new Set()) {
        if (!callMethods) return;
        for (const calleeFqn of callMethods) {
            if (diagramContext.methodMap.has(calleeFqn)) {
                const m = diagramContext.methodMap.get(calleeFqn);
                if (shouldIncludeMethodNode(m.kind)) {
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
            } else if (diagramContext.outboundOperationSet.has(calleeFqn)) {
                if (!diagramContext.showDiagramOutboundPorts) continue;
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

function buildSequenceFromCallMethods(rootMethod, diagramContext) {
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
            if (diagramContext.methodMap.has(calleeFqn)) {
                const m = diagramContext.methodMap.get(calleeFqn);
                const isUc = m.kind === "usecase";
                if (diagramContext.showDiagramInternalMethods || isUc) {
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
            } else if (diagramContext.outboundOperationSet.has(calleeFqn)) {
                if (!diagramContext.showDiagramOutboundPorts) continue;
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
        this.state.data = getUsecaseData();
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
            { id: 'show-diagram-outbound-ports', reRender: true }
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
        const showDiagramInternalMethods = document.getElementById('show-diagram-internal-methods').checked;
        const showDiagramOutboundPorts = document.getElementById('show-diagram-outbound-ports').checked;
        
        const diagramContext = {
            methodMap,
            outboundOperationSet,
            showDiagramInternalMethods,
            showDiagramOutboundPorts
        };

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
                const graph = buildGraphFromCallMethods(method, diagramContext);
                const hasGraph = graph.edges.length > 0;

                const sequence = buildSequenceFromCallMethods(method, diagramContext);
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

                            const classSubgraphs = new Map();
                            graph.nodes.forEach(node => {
                                const nodeId = fqnToNodeId(node.fqn);
                                if (node.kind === "external" || node.kind === "inbound-class") {
                                    // 外部ポート / inboundクラス: 四角、グレー
                                    const nodeLabel = globalThis.Jig.glossary.getTypeTerm(node.fqn).title;
                                    builder.addNode(nodeId, nodeLabel, '["$LABEL"]');
                                    builder.addStyle(nodeId, "fill:#e0e0e0,stroke:#aaa");
                                    if (node.kind === "inbound-class") {
                                        builder.addClick(nodeId, "./inbound.html#" + node.fqn);
                                    }
                                } else {
                                    // usecase / method / static-method: クラス単位でsubgraphにグルーピング
                                    const classFqn = getClassFqnFromMethodFqn(node.fqn);
                                    const classNodeId = globalThis.Jig.fqnToId("sg", classFqn);
                                    const classLabel = globalThis.Jig.glossary.getTypeTerm(classFqn).title;
                                    const subgraph = builder.ensureSubgraph(classSubgraphs, classNodeId, classLabel, 'LR');
                                    if (node.kind === "usecase") {
                                        // ユースケース: 角丸、ページ内リンク
                                        const nodeLabel = globalThis.Jig.glossary.getMethodTerm(node.fqn, true).title;
                                        builder.addNodeToSubgraph(subgraph, nodeId, nodeLabel, '(["$LABEL"])');
                                        // 自身を強調表示
                                        if (node.fqn === method.fqn) {
                                            builder.addStyle(nodeId, "font-weight:bold");
                                        }
                                        builder.addClick(nodeId, "#" + node.fqn);
                                    } else {
                                        // その他(method or static-method): 角丸、グレー
                                        const nodeLabel = globalThis.Jig.glossary.getMethodTerm(node.fqn, true).title;
                                        builder.addNodeToSubgraph(subgraph, nodeId, nodeLabel, '(["$LABEL"])');
                                        builder.addStyle(nodeId, "fill:#e0e0e0,stroke:#aaa");
                                    }
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
