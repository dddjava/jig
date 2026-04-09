const UsecaseApp = (() => {
    const Jig = globalThis.Jig;

    const state = {
        data: null,
        selectedTabs: new Map(), // methodFqn -> 'usecase' | 'sequence'
        handlerFqns: null,       // ハンドラのみ表示時のFQN集合、nullはすべて表示
        sidebarFilterText: '',
    };

    const fqnToNodeId = (fqn) => Jig.util.fqnToId("node", fqn);    // Mermaid内部ノード
    const fqnToTypeId = (fqn) => Jig.util.fqnToId("type", fqn);    // usecaseクラスのHTML id
    const fqnToMethodId = (fqn) => Jig.util.fqnToId("method", fqn); // usecaseメソッドのHTML id

    /**
     * @param {string} fqn
     * @returns {string}
     */
    function getClassFqnFromMethodFqn(fqn) {
        const hashIdx = fqn.indexOf('#');
        return hashIdx === -1 ? fqn : fqn.slice(0, hashIdx);
    }

    /**
     * @param {UsecaseMethod} method
     * @returns {boolean}
     */
    function isUsecase(method) {
        return method.visibility === "PUBLIC";
    }

    /**
     * inboundクラスから直接呼び出されているメソッドのFQN集合を返す
     * @returns {Set<string>}
     */
    function buildHandlerFqns() {
        const fqns = new Set();
        Jig.data.inbound.getControllers().forEach(controller => {
            (controller.relations || []).forEach(relation => {
                if (relation?.to) fqns.add(relation.to);
            });
        });
        return fqns;
    }

    /**
     * @param {OutboundData} outboundData
     * @returns {Set<string>}
     */
    function buildOutboundOperationSet(outboundData) {
        if (!outboundData?.outboundPorts) return new Set();
        const set = new Set();
        outboundData.outboundPorts.forEach(port => {
            (port.operations || []).forEach(op => set.add(op.fqn));
        });
        return set;
    }

    /**
     * @param {UsecaseMethod} rootMethod
     * @param {DiagramContext} diagramContext
     * @returns {{nodes: DiagramNode[], edges: DiagramEdge[]}}
     */
    function buildUsecaseDiagram(rootMethod, diagramContext) {
        /**
         * @type {Map<string, DiagramNode>}
         */
        const nodes = new Map();
        const edgeSet = new Set();
        /**
         * @type {DiagramEdge[]}
         */
        const edges = [];
        const visited = new Set();

        nodes.set(rootMethod.fqn, {fqn: rootMethod.fqn, kind: "usecase"});
        visited.add(rootMethod.fqn);

        /**
         * @param {string} kind
         * @returns {boolean}
         */
        function shouldIncludeMethodNode(kind) {
            return diagramContext.showDiagramInternalMethods || kind === "usecase";
        }

        /**
         * @type {Map<string, DiagramNode[]>}
         */
        const reverseCallerMap = new Map();

        /**
         * @param {string} calleeFqn
         * @param {DiagramNode} callerNode
         */
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

        Jig.data.inbound.getControllers().forEach(controller => {
            (controller.relations || []).forEach(relation => {
                if (!relation?.from || !relation?.to) return;
                const callerClassFqn = getClassFqnFromMethodFqn(relation.from);
                if (diagramContext.methodMap.has(callerClassFqn)) return;
                addReverseCaller(relation.to, {fqn: callerClassFqn, kind: "inbound-class"});
            });
        });

        /**
         * @param {string} rootFqn
         * @returns {Map<string, string>} callerFqn -> kind
         */
        function collectVisibleCallers(rootFqn) {
            const callers = reverseCallerMap.get(rootFqn) || [];
            /** @type {Map<string, string>} */
            const visible = new Map();

            if (diagramContext.showDiagramInternalMethods) {
                callers.forEach(caller => {
                    if (caller.fqn === rootFqn) return;
                    if (!shouldIncludeMethodNode(caller.kind)) return;
                    visible.set(caller.fqn, caller.kind);
                });
                return visible;
            }

            /**
             * @param {DiagramNode} startCaller
             * @returns {Map<string, string>}
             */
            function collectUsecaseAncestors(startCaller) {
                const queue = [startCaller];
                const visitedCallerFqns = new Set([rootFqn]);
                /** @type {Map<string, string>} */
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

        /**
         * @param {string} effectiveCallerFqn
         * @param {string[]} callMethods
         * @param {Set<string>} inliningPath
         */
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
                        nodes.set(classFqn, {fqn: classFqn, kind: "outbound"});
                    }
                }
            }
        }

        traverse(rootMethod.fqn, rootMethod.callMethods);

        // ドメインモデルノードを追加（引数・戻り値）
        const domainFqnSet = Jig.data.domain.getDomainFqnSet();
        if (diagramContext.showDiagramDomainTypes && domainFqnSet.size > 0) {
            [...nodes.keys()].forEach(fqn => {
                const method = diagramContext.methodMap.get(fqn);
                if (!method) return; // outbound / inbound-class はスキップ

                // 引数の型 → メソッド
                (method.parameterTypeRefs || []).forEach(typeRef => {
                    Jig.util.collectTypeRefFqns(typeRef)
                        .filter(domainFqn => domainFqnSet.has(domainFqn))
                        .forEach(domainFqn => {
                            if (!nodes.has(domainFqn)) {
                                nodes.set(domainFqn, {fqn: domainFqn, kind: "domain-type"});
                            }
                            const edgeKey = domainFqn + '\u2192' + fqn;
                            if (!edgeSet.has(edgeKey)) {
                                edgeSet.add(edgeKey);
                                edges.push({from: domainFqn, to: fqn, dotted: true});
                            }
                        });
                });

                // メソッド → 戻り値の型
                Jig.util.collectTypeRefFqns(method.returnTypeRef)
                    .filter(returnFqn => returnFqn !== 'void' && domainFqnSet.has(returnFqn))
                    .forEach(returnFqn => {
                        if (!nodes.has(returnFqn)) {
                            nodes.set(returnFqn, {fqn: returnFqn, kind: "domain-type"});
                        }
                        const edgeKey = fqn + '\u2192' + returnFqn;
                        if (!edgeSet.has(edgeKey)) {
                            edgeSet.add(edgeKey);
                            edges.push({from: fqn, to: returnFqn, dotted: true});
                        }
                    });
            });
        }

        return {nodes: [...nodes.values()], edges};
    }

    /**
     * @param {Usecase} usecase
     * @param {Set<string>|null} handlerFqns ハンドラのみ表示時のFQN集合、nullはすべて表示
     * @returns {{nodes: DiagramNode[], edges: DiagramEdge[]}}
     */
    function buildClassGraph(usecase, handlerFqns = null) {
        /** @type {DiagramNode[]} */
        const nodes = [];
        /** @type {DiagramEdge[]} */
        const edges = [];
        const edgeSet = new Set();
        const domainNodeSet = new Set();
        const classMethods = [...usecase.methods.filter(m => isUsecase(m) && (!handlerFqns || handlerFqns.has(m.fqn))), ...usecase.staticMethods];
        const methodFqns = new Set(classMethods.map(m => m.fqn));
        const domainFqnSet = Jig.data.domain.getDomainFqnSet();

        classMethods.forEach(method => {
            const kind = isUsecase(method) ? "usecase" : (usecase.staticMethods.includes(method) ? "static-method" : "method");
            nodes.push({fqn: method.fqn, kind});

            (method.callMethods || []).forEach(calleeFqn => {
                if (methodFqns.has(calleeFqn)) {
                    const edgeKey = `${method.fqn}->${calleeFqn}`;
                    if (!edgeSet.has(edgeKey)) {
                        edgeSet.add(edgeKey);
                        edges.push({from: method.fqn, to: calleeFqn});
                    }
                }
            });

            // ドメインモデルノード（引数・戻り値）
            (method.parameterTypeRefs || []).forEach(typeRef => {
                Jig.util.collectTypeRefFqns(typeRef)
                    .filter(domainFqn => domainFqnSet.has(domainFqn))
                    .forEach(domainFqn => {
                        if (!domainNodeSet.has(domainFqn)) {
                            domainNodeSet.add(domainFqn);
                            nodes.push({fqn: domainFqn, kind: "domain-type"});
                        }
                        const edgeKey = `${domainFqn}->${method.fqn}`;
                        if (!edgeSet.has(edgeKey)) {
                            edgeSet.add(edgeKey);
                            edges.push({from: domainFqn, to: method.fqn, dotted: true});
                        }
                    });
            });

            Jig.util.collectTypeRefFqns(method.returnTypeRef)
                .filter(returnFqn => returnFqn !== 'void' && domainFqnSet.has(returnFqn))
                .forEach(returnFqn => {
                    if (!domainNodeSet.has(returnFqn)) {
                        domainNodeSet.add(returnFqn);
                        nodes.push({fqn: returnFqn, kind: "domain-type"});
                    }
                    const edgeKey = `${method.fqn}->${returnFqn}`;
                    if (!edgeSet.has(edgeKey)) {
                        edgeSet.add(edgeKey);
                        edges.push({from: method.fqn, to: returnFqn, dotted: true});
                    }
                });
        });

        // inboundクラスノード（このクラスのメソッドを呼び出すコントローラー）
        const inboundNodeSet = new Set();
        Jig.data.inbound.getControllers().forEach(controller => {
            (controller.relations || []).forEach(relation => {
                if (!relation?.from || !relation?.to) return;
                if (!methodFqns.has(relation.to)) return;
                const callerClassFqn = getClassFqnFromMethodFqn(relation.from);
                if (!inboundNodeSet.has(callerClassFqn)) {
                    inboundNodeSet.add(callerClassFqn);
                    nodes.push({fqn: callerClassFqn, kind: "inbound-class"});
                }
                const edgeKey = `${callerClassFqn}->${relation.to}`;
                if (!edgeSet.has(edgeKey)) {
                    edgeSet.add(edgeKey);
                    edges.push({from: callerClassFqn, to: relation.to});
                }
            });
        });

        return {nodes, edges};
    }

    const SequenceDiagram = {
        /**
         * @param {UsecaseMethod} rootMethod
         * @param {DiagramContext} diagramContext
         * @returns {SequenceDiagram}
         */
        buildDiagram(rootMethod, diagramContext) {
            /** @type {string[]} */
            const participantKeys = [];
            /** @type {Map<string, SequenceParticipant>} */
            const participants = new Map();
            /** @type {SequenceCall[]} */
            const calls = [];
            const visited = new Set();

            /**
             * @param {string} fqn
             * @returns {string}
             */
            function getMethodSimpleName(fqn) {
                const hashIdx = fqn.indexOf('#');
                if (hashIdx === -1) return fqn;
                const parenIdx = fqn.indexOf('(', hashIdx);
                return parenIdx === -1 ? fqn.slice(hashIdx + 1) : fqn.slice(hashIdx + 1, parenIdx);
            }

            /**
             * @param {string} key
             * @param {string} label
             * @param {string} kind
             * @returns {SequenceParticipant}
             */
            function ensureParticipant(key, label, kind) {
                if (!participants.has(key)) {
                    participants.set(key, {id: fqnToNodeId(key), label, kind});
                    participantKeys.push(key);
                }
                return participants.get(key);
            }

            /**
             * @param {string} key
             * @returns {SequenceParticipant}
             */
            function ensureUsecaseParticipant(key) {
                const label = Jig.glossary.getMethodTerm(key, true).title
                return ensureParticipant(key, label, "usecase");
            }

            /**
             * @param {string} effectiveCallerFqn
             * @param {string[]} callMethods
             * @param {Set<string>} inliningPath
             */
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
                        const callee = ensureParticipant(classFqn, Jig.glossary.getTypeTerm(classFqn).title, "outbound");
                        calls.push({from: caller.id, to: callee.id, label: methodName});
                    }
                }
            }

            ensureUsecaseParticipant(rootMethod.fqn);
            visited.add(rootMethod.fqn);
            traverse(rootMethod.fqn, rootMethod.callMethods);

            return {
                participants: participantKeys.map(k => participants.get(k)),
                calls
            };
        },

        /**
         * @param {SequenceDiagram} sequence
         * @returns {string|null}
         */
        buildCode(sequence) {
            if (sequence.calls.length === 0) return null;
            let code = 'sequenceDiagram\n';

            const outbounds = sequence.participants.filter(p => p.kind === "outbound");
            const internal = sequence.participants.filter(p => p.kind !== "outbound");

            internal.forEach(p => {
                code += `  participant ${p.id} as ${p.label}\n`;
            });

            if (outbounds.length > 0) {
                code += '  box outbounds\n';
                outbounds.forEach(p => {
                    code += `    participant ${p.id} as ${p.label}\n`;
                });
                code += '  end\n';
            }

            sequence.calls.forEach(call => {
                code += `  ${call.from}->>${call.to}: ${call.label}\n`;
            });

            return code;
        }
    };

    /**
     * サイドバーの描画
     * @param {Usecase[]} usecases
     */
    function renderSidebar(usecases) {
        const sidebar = document.getElementById("usecase-sidebar-list");
        if (!sidebar) return;
        sidebar.innerHTML = "";

        const handlerFqns = state.handlerFqns;
        const filterText = state.sidebarFilterText.toLowerCase();
        const isVisibleMethod = (method) => isUsecase(method) && (!handlerFqns || handlerFqns.has(method.fqn));

        // テキストフィルタ: クラス名一致→全メソッド表示、メソッド名一致→該当メソッドのみ表示
        const filteredItems = usecases.flatMap(usecase => {
            const visibleMethods = usecase.methods.filter(isVisibleMethod);
            if (visibleMethods.length === 0) return [];
            if (!filterText) return [{usecase, methods: visibleMethods}];

            const classTitle = Jig.glossary.getTypeTerm(usecase.fqn).title.toLowerCase();
            if (classTitle.includes(filterText)) return [{usecase, methods: visibleMethods}];

            const matchingMethods = visibleMethods.filter(m =>
                Jig.glossary.getMethodTerm(m.fqn).title.toLowerCase().includes(filterText)
            );
            return matchingMethods.length > 0 ? [{usecase, methods: matchingMethods}] : [];
        });

        const section = Jig.dom.createElement("section", {
            className: "in-page-sidebar__section",
            children: [
                Jig.dom.createElement("p", {
                    className: "in-page-sidebar__title",
                    textContent: "ユースケース"
                }),
                Jig.dom.createElement("ul", {
                    className: "in-page-sidebar__links",
                    children: filteredItems.map(({usecase, methods}) => {
                        const children = [
                            Jig.dom.createElement("a", {
                                className: "in-page-sidebar__link",
                                attributes: {href: "#" + fqnToTypeId(usecase.fqn)},
                                textContent: Jig.glossary.getTypeTerm(usecase.fqn).title
                            })
                        ];
                        children.push(Jig.dom.createElement("ul", {
                            className: "in-page-sidebar__links",
                            children: methods.map(method =>
                                Jig.dom.createElement("li", {
                                    className: "in-page-sidebar__item",
                                    children: [
                                        Jig.dom.createElement("a", {
                                            className: "in-page-sidebar__link in-page-sidebar__link--sub",
                                            attributes: {href: "#" + fqnToMethodId(method.fqn)},
                                            textContent: Jig.glossary.getMethodTerm(method.fqn).title
                                        })
                                    ]
                                })
                            )
                        }));
                        return Jig.dom.createElement("li", {
                            className: "in-page-sidebar__item",
                            children
                        });
                    })
                })
            ]
        });
        sidebar.appendChild(section);
    }

    /**
     * ユースケース一覧の描画
     * @param {Usecase[]} usecases
     */
    function renderUsecaseList(usecases) {
        const container = document.getElementById("usecase-list");
        if (!container) return;
        container.innerHTML = "";

        if (!usecases || usecases.length === 0) {
            container.textContent = "データなし";
            return;
        }

        /** @type {Map<string, UsecaseMethod>} */
        const methodMap = new Map();
        usecases.forEach(usecase => {
            (usecase.methods || []).forEach(m => methodMap.set(m.fqn, {
                ...m,
                kind: isUsecase(m) ? "usecase" : "method"
            }));
            (usecase.staticMethods || []).forEach(m => methodMap.set(m.fqn, {...m, kind: "static-method"}));
        });

        const outboundOperationSet = buildOutboundOperationSet(Jig.data.outbound.get());
        const showDiagramInternalMethods = document.getElementById('show-diagram-internal-methods').checked;
        const showDiagramOutboundPorts = document.getElementById('show-diagram-outbound-ports').checked;
        const showDiagramDomainTypes = document.getElementById('show-diagram-domain-types').checked;

        /** @type {DiagramContext} */
        const diagramContext = {
            methodMap,
            outboundOperationSet,
            showDiagramInternalMethods,
            showDiagramOutboundPorts,
            showDiagramDomainTypes
        };

        const handlerFqns = state.handlerFqns;
        const isVisibleMethod = (method) => isUsecase(method) && (!handlerFqns || handlerFqns.has(method.fqn));

        usecases.forEach(usecase => {
            const visibleUsecaseMethods = usecase.methods.filter(isVisibleMethod);
            if (handlerFqns && visibleUsecaseMethods.length === 0) return;

            const term = Jig.glossary.getTypeTerm(usecase.fqn);
            const section = Jig.dom.createElement("section", {
                className: "jig-card jig-card--type",
                children: [
                    Jig.dom.createElement("h3", {
                        children: [Jig.dom.createElement("a", {id: fqnToTypeId(usecase.fqn), textContent: term.title})]
                    }),
                    Jig.dom.createElement("div", {
                        className: "declaration",
                        textContent: usecase.fqn
                    })
                ]
            });

            if (term.description) {
                section.appendChild(Jig.dom.createMarkdownElement(term.description));
            }

            // Class diagram (internal relations)
            const classGraph = buildClassGraph(usecase, handlerFqns);
            if (classGraph.edges.length > 0) {
                const classDiagramContainer = Jig.dom.createElement("div", {className: "diagram-container class-diagram"});
                section.appendChild(classDiagramContainer);

                Jig.mermaid.diagram.createAndRegister(classDiagramContainer, (mmdContainer) => {
                    mmdContainer.innerHTML = "";
                    const builder = new Jig.mermaid.Builder();
                    builder.applyThemeClassDefs();

                    classGraph.nodes.forEach(node => {
                        const nodeId = fqnToNodeId(node.fqn);
                        if (node.kind === "inbound-class") {
                            const nodeLabel = Jig.glossary.getTypeTerm(node.fqn).title;
                            builder.addNode(nodeId, nodeLabel, 'class');
                            builder.addClass(nodeId, "inbound");
                            builder.addClick(nodeId, "./inbound.html#" + Jig.util.fqnToId("adapter", node.fqn));
                        } else if (node.kind === "domain-type") {
                            const nodeLabel = Jig.glossary.getTypeTerm(node.fqn).title;
                            builder.addNode(nodeId, nodeLabel, 'class');
                            builder.addClass(nodeId, "domain");
                            builder.addClick(nodeId, "./domain.html#" + Jig.util.fqnToId("domain", node.fqn));
                        } else {
                            const nodeLabel = Jig.glossary.getMethodTerm(node.fqn, true).title;
                            if (node.kind === "usecase") {
                                builder.addNode(nodeId, nodeLabel, 'method');
                                builder.addClass(nodeId, "usecase");
                                builder.addClick(nodeId, "#" + fqnToMethodId(node.fqn));
                            } else {
                                builder.addNode(nodeId, nodeLabel, 'method');
                                builder.addClass(nodeId, "inactive");
                            }
                        }
                    });

                    classGraph.edges.forEach(edge => {
                        builder.addEdge(fqnToNodeId(edge.from), fqnToNodeId(edge.to), "", edge.dotted ?? false);
                    });

                    const generator = (dir) => builder.build(dir);
                    Jig.mermaid.render.renderWithControls(mmdContainer, generator, {direction: 'LR'});
                });
            }

            const fieldsList = Jig.dom.type.fieldsList(usecase.fields, Jig.dom.type.elementForRef);
            if (fieldsList) section.appendChild(fieldsList);

            if (usecase.staticMethods.length > 0) {
                const staticList = Jig.dom.type.methodsList("staticメソッド", usecase.staticMethods, Jig.dom.type.elementForRef);
                if (staticList) {
                    staticList.classList.add("static-methods");
                    section.appendChild(staticList);
                }
            }

            // usecaseとするのはPUBLICのみ
            const internalMethods = usecase.methods.filter(method => !isUsecase(method))
            if (internalMethods.length > 0) {
                const staticList = Jig.dom.type.methodsList("メソッド", internalMethods, Jig.dom.type.elementForRef);
                if (staticList) {
                    staticList.classList.add("methods");
                    section.appendChild(staticList);
                }
            }

            visibleUsecaseMethods.forEach(method => {
                const methodTerm = Jig.glossary.getMethodTerm(method.fqn);
                const methodDescription = methodTerm.description;

                const methodSection = Jig.dom.createElement("article", {
                    className: "jig-card jig-card--item",
                    children: [
                        Jig.dom.createElement("h4", {id: fqnToMethodId(method.fqn), textContent: methodTerm.title}),
                        Jig.dom.createElement("div", {
                            className: "declaration",
                            textContent: methodTerm.shortDeclaration
                        })
                    ]
                });

                // Method Description
                if (methodDescription) {
                    methodSection.appendChild(Jig.dom.createElement("section", {
                        className: "description",
                        children: [Jig.dom.createMarkdownElement(methodDescription)]
                    }));
                }

                // Diagrams
                const usecaseDiagram = buildUsecaseDiagram(method, diagramContext);
                const hasUsecaseDiagram = usecaseDiagram.edges.length > 0;

                const sequenceDiagram = SequenceDiagram.buildDiagram(method, diagramContext);
                const sequenceDiagramCode = SequenceDiagram.buildCode(sequenceDiagram);
                const hasSequenceDiagram = sequenceDiagramCode !== null;

                if (hasUsecaseDiagram || hasSequenceDiagram) {
                    const diagramContainer = Jig.dom.createElement("div", {className: "diagram-container"});
                    methodSection.appendChild(diagramContainer);

                    let usecasePanel = null;
                    let sequencePanel = null;

                    if (hasUsecaseDiagram && hasSequenceDiagram) {
                        const selectedTab = state.selectedTabs.get(method.fqn) || 'usecase';
                        const isUsecaseActive = selectedTab === 'usecase';

                        const usecaseBtn = Jig.dom.createElement("button", {
                            className: "diagram-tab" + (isUsecaseActive ? " active" : ""),
                            textContent: "ユースケース図"
                        });
                        const sequenceBtn = Jig.dom.createElement("button", {
                            className: "diagram-tab" + (!isUsecaseActive ? " active" : ""),
                            textContent: "シーケンス図"
                        });
                        diagramContainer.appendChild(Jig.dom.createElement("div", {
                            className: "diagram-tabs",
                            children: [usecaseBtn, sequenceBtn]
                        }));

                        usecasePanel = Jig.dom.createElement("div", {className: "diagram-panel" + (isUsecaseActive ? "" : " hidden")});
                        sequencePanel = Jig.dom.createElement("div", {className: "diagram-panel" + (!isUsecaseActive ? "" : " hidden")});

                        usecaseBtn.addEventListener('click', () => {
                            usecaseBtn.classList.add('active');
                            sequenceBtn.classList.remove('active');
                            usecasePanel.classList.remove('hidden');
                            sequencePanel.classList.add('hidden');
                            state.selectedTabs.set(method.fqn, 'usecase');
                        });
                        sequenceBtn.addEventListener('click', () => {
                            sequenceBtn.classList.add('active');
                            usecaseBtn.classList.remove('active');
                            sequencePanel.classList.remove('hidden');
                            usecasePanel.classList.add('hidden');
                            state.selectedTabs.set(method.fqn, 'sequence');
                        });

                        diagramContainer.appendChild(usecasePanel);
                        diagramContainer.appendChild(sequencePanel);
                    }

                    if (hasUsecaseDiagram) {
                        Jig.mermaid.diagram.createAndRegister(usecasePanel || diagramContainer, (mmdContainer) => {
                            mmdContainer.innerHTML = "";
                            // 毎回新しい diagramContext を作成（現在の設定値を反映）
                            const showDiagramInternalMethods = document.getElementById('show-diagram-internal-methods').checked;
                            const showDiagramOutboundPorts = document.getElementById('show-diagram-outbound-ports').checked;
                            const showDiagramDomainTypes = document.getElementById('show-diagram-domain-types').checked;
                            const currentDiagramContext = {
                                methodMap,
                                outboundOperationSet,
                                showDiagramInternalMethods,
                                showDiagramOutboundPorts,
                                showDiagramDomainTypes
                            };
                            const currentUsecaseDiagram = buildUsecaseDiagram(method, currentDiagramContext);

                            const builder = new Jig.mermaid.Builder();
                            builder.applyThemeClassDefs();

                            const classSubgraphs = new Map();
                            currentUsecaseDiagram.nodes.forEach(node => {
                                const nodeId = fqnToNodeId(node.fqn);
                                if (node.kind === "outbound" || node.kind === "inbound-class" || node.kind === "domain-type") {
                                    // 外部ポート / inboundクラス / ドメインモデル
                                    const nodeLabel = Jig.glossary.getTypeTerm(node.fqn).title;
                                    builder.addNode(nodeId, nodeLabel, 'class');
                                    if (node.kind === "inbound-class") {
                                        builder.addClass(nodeId, "inbound");
                                        builder.addClick(nodeId, "./inbound.html#" + Jig.util.fqnToId("adapter", node.fqn));
                                    } else if (node.kind === "outbound") {
                                        builder.addClass(nodeId, "outbound");
                                        builder.addClick(nodeId, "./outbound.html#" + Jig.util.fqnToId("port", node.fqn));
                                    } else if (node.kind === "domain-type") {
                                        builder.addClass(nodeId, "domain");
                                        builder.addClick(nodeId, "./domain.html#" + Jig.util.fqnToId("domain", node.fqn));
                                    }
                                } else {
                                    // usecase / method / static-method: クラス単位でsubgraphにグルーピング
                                    const classFqn = getClassFqnFromMethodFqn(node.fqn);
                                    const classNodeId = Jig.util.fqnToId("sg", classFqn);
                                    const classLabel = Jig.glossary.getTypeTerm(classFqn).title;
                                    const subgraph = builder.ensureSubgraph(classSubgraphs, classNodeId, classLabel, 'LR');
                                    if (node.kind === "usecase") {
                                        // ユースケース: 角丸、ページ内リンク
                                        const nodeLabel = Jig.glossary.getMethodTerm(node.fqn, true).title;
                                        builder.addNodeToSubgraph(subgraph, nodeId, nodeLabel, 'method');
                                        builder.addClass(nodeId, "usecase");
                                        // 自身を強調表示
                                        if (node.fqn === method.fqn) {
                                            builder.addStyle(nodeId, "font-weight:bold");
                                        }
                                        builder.addClick(nodeId, "#" + fqnToMethodId(node.fqn));
                                    } else {
                                        // その他(method or static-method)
                                        const nodeLabel = Jig.glossary.getMethodTerm(node.fqn, true).title;
                                        builder.addNodeToSubgraph(subgraph, nodeId, nodeLabel, 'method');
                                        builder.addClass(nodeId, "inactive");
                                    }
                                }
                            });

                            currentUsecaseDiagram.edges.forEach(edge => {
                                builder.addEdge(fqnToNodeId(edge.from), fqnToNodeId(edge.to), "", edge.dotted ?? false);
                            });

                            const generator = (dir) => builder.build(dir);
                            Jig.mermaid.render.renderWithControls(mmdContainer, generator, {direction: 'LR'});
                        });
                    }

                    if (hasSequenceDiagram) {
                        Jig.mermaid.diagram.createAndRegister(sequencePanel || diagramContainer, (sequenceContainer) => {
                            sequenceContainer.innerHTML = "";
                            // 毎回新しい diagramContext を作成（現在の設定値を反映）
                            const showDiagramInternalMethods = document.getElementById('show-diagram-internal-methods').checked;
                            const showDiagramOutboundPorts = document.getElementById('show-diagram-outbound-ports').checked;
                            const showDiagramDomainTypes = document.getElementById('show-diagram-domain-types').checked;
                            const currentDiagramContext = {
                                methodMap,
                                outboundOperationSet,
                                showDiagramInternalMethods,
                                showDiagramOutboundPorts,
                                showDiagramDomainTypes
                            };
                            const currentSequenceDiagram = SequenceDiagram.buildDiagram(method, currentDiagramContext);
                            const currentSequenceDiagramCode = SequenceDiagram.buildCode(currentSequenceDiagram);
                            if (currentSequenceDiagramCode) {
                                Jig.mermaid.render.renderWithControls(sequenceContainer, () => currentSequenceDiagramCode);
                            }
                        });
                    }
                }

                const depends = Jig.dom.createElement("div", {className: "depends"});
                if (method.parameterTypeRefs.length > 0) {
                    const parametersSection = Jig.dom.createElement("section", {className: "depends-section"});
                    parametersSection.appendChild(Jig.dom.createElement("h4", {textContent: "要求するもの（引数）"}));
                    method.parameterTypeRefs.forEach(parameterTypeRef => {
                        parametersSection.appendChild(Jig.dom.createElement("div", {className: "depends-item", children: [Jig.dom.type.elementForRef(parameterTypeRef)]}));
                    });
                    depends.appendChild(parametersSection);
                }
                if (method.returnTypeRef.fqn !== 'void') {
                    const returnSection = Jig.dom.createElement("section", {className: "depends-section"});
                    returnSection.appendChild(Jig.dom.createElement("h4", {textContent: "得られるもの（戻り値）"}));
                    returnSection.appendChild(Jig.dom.createElement("div", {className: "depends-item", children: [Jig.dom.type.elementForRef(method.returnTypeRef)]}));
                    depends.appendChild(returnSection);
                }
                methodSection.appendChild(depends);

                section.appendChild(methodSection);
            });

            container.appendChild(section);
        });
    }

    /**
     * 表示オプションの初期化
     */
    function initControls() {
        const controls = [
            {id: 'show-fields', class: 'hide-usecase-fields'},
            {id: 'show-static-methods', class: 'hide-usecase-static-methods'},
            {id: 'show-diagrams', class: 'hide-usecase-diagrams'},
            {id: 'show-details', class: 'hide-usecase-details'},
            {id: 'show-descriptions', class: 'hide-usecase-descriptions'},
            {id: 'show-declarations', class: 'hide-usecase-declarations'},
            {id: 'show-diagram-internal-methods', reRender: true},
            {id: 'show-diagram-outbound-ports', reRender: true},
            {id: 'show-diagram-domain-types', reRender: true}
        ];

        controls.forEach(control => {
            const checkbox = document.getElementById(control.id);
            if (!checkbox) return;

            const update = () => {
                if (control.class) {
                    document.body.classList.toggle(control.class, !checkbox.checked);
                }
                if (control.reRender) {
                    Jig.mermaid.diagram.rerenderVisible();
                }
            };

            checkbox.addEventListener('change', update);
            update();
        });

        // 表示対象ラジオボタン
        ['display-target-all', 'display-target-handlers-only'].forEach(id => {
            const radio = document.getElementById(id);
            if (radio) radio.addEventListener('change', () => {
                const handlersOnly = document.getElementById('display-target-handlers-only')?.checked ?? false;
                state.handlerFqns = handlersOnly ? buildHandlerFqns() : null;
                const usecases = state.data.usecases;
                renderSidebar(usecases);
                renderUsecaseList(usecases);
                Jig.mermaid.diagram.rerenderVisible();
            });
        });

        Jig.dom.sidebar.initTextFilter('usecase-sidebar-filter', text => {
            state.sidebarFilterText = text;
            renderSidebar(state.data.usecases);
        });
    }

    /**
     * 画面の描画
     */
    function render() {
        const handlersOnly = document.getElementById('display-target-handlers-only')?.checked ?? false;
        state.handlerFqns = handlersOnly ? buildHandlerFqns() : null;
        const usecases = state.data.usecases;
        renderSidebar(usecases);
        renderUsecaseList(usecases);
    }

    function init() {
        if (typeof document === "undefined") return;
        if (!document.body.classList.contains("usecase-model")) return;

        // モジュールキャッシュを再ロードしなくても状態がリセットされるよう、毎回 init で state をクリア
        state.data = null;
        state.selectedTabs = new Map();
        state.handlerFqns = null;
        state.sidebarFilterText = '';

        state.data = Jig.data.usecase.get();
        if (!state.data) return;

        Jig.data.resetCache();

        initControls();
        render();
    }

    return {
        init,
        state,
        buildOutboundOperationSet,
        buildUsecaseDiagram,
        buildClassGraph,
        SequenceDiagram,
        render,
        renderSidebar,
        renderUsecaseList,
    };
})();

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", () => {
        UsecaseApp.init();
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = UsecaseApp;
}
