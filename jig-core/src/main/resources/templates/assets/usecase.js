const UsecaseApp = (() => {
    const Jig = globalThis.Jig;

    function createInitialState() {
        return {
            data: null,
            selectedTabs: new Map(), // methodFqn -> 'usecase' | 'sequence'
            handlerFqns: null,       // ハンドラのみ表示時のFQN集合、nullはすべて表示
            sidebarFilterText: '',
        };
    }

    const state = createInitialState();

    const fqnToNodeId = (fqn) => Jig.util.fqnToId("node", fqn);    // Mermaid内部ノード
    const fqnToTypeId = (fqn) => Jig.util.fqnToId("type", fqn);    // usecaseクラスのHTML id
    const fqnToMethodId = (fqn) => Jig.util.fqnToId("method", fqn); // usecaseメソッドのHTML id
    const fqnToPackageId = (fqn) => Jig.util.fqnToId("package", fqn); // パッケージ見出しのHTML id

    // 有向エッジを Set でユニーク化するためのキー。FQNには現れない区切り文字を使用する。
    const makeEdgeKey = (from, to) => `${from} ${to}`;

    /**
     * 未登録のエッジだけを追加する
     * @param {Set<string>} edgeSet
     * @param {DiagramEdge[]} edges
     */
    function addEdgeOnce(edgeSet, edges, from, to, dotted = false) {
        const edgeKey = makeEdgeKey(from, to);
        if (edgeSet.has(edgeKey)) return;
        edgeSet.add(edgeKey);
        edges.push(dotted ? {from, to, dotted: true} : {from, to});
    }

    /**
     * 未登録のノードだけを追加する
     * @param {Set<string>} addedFqns
     * @param {DiagramNode[]} nodes
     */
    function addNodeOnce(addedFqns, nodes, fqn, kind) {
        if (addedFqns.has(fqn)) return;
        addedFqns.add(fqn);
        nodes.push({fqn, kind});
    }

    // 図の表示対象トグル。checkbox idとDiagramContextキーの対応を一元管理する
    const DIAGRAM_TOGGLES = [
        {id: 'show-diagram-callers', key: 'showDiagramCallers'},
        {id: 'show-diagram-callees', key: 'showDiagramCallees'},
        {id: 'show-diagram-internal-methods', key: 'showDiagramInternalMethods'},
        {id: 'show-diagram-inbound-classes', key: 'showDiagramInboundClasses'},
        {id: 'show-diagram-outbound-ports', key: 'showDiagramOutboundPorts'},
        {id: 'show-diagram-domain-types', key: 'showDiagramDomainTypes'},
        {id: 'show-diagram-arguments', key: 'showDiagramArguments'}
    ];

    // クラス図・パッケージ図で共通のコンテキストメニュー項目
    const CLASS_LEVEL_DIAGRAM_MENU_TOGGLES = [
        {key: 'showDiagramInboundClasses', label: '入力インタフェース'},
        {key: 'showDiagramOutboundPorts', label: '出力インタフェース'},
        {key: 'showDiagramDomainTypes', label: 'ドメインモデル'}
    ];

    // パッケージ図固有のコンテキストメニュー項目。ノード粒度をクラス単位/メソッド単位で切り替える
    const PACKAGE_DIAGRAM_MENU_TOGGLES = [
        ...CLASS_LEVEL_DIAGRAM_MENU_TOGGLES,
        {key: 'showDiagramMethodLevel', label: 'メソッド単位'}
    ];

    /**
     * 図の有無の判定用に、表示対象トグルをすべてONにしたコンテキストを返す。
     * 現在のトグル状態で判定すると、OFFのトグルでしか関連が生まれない図はコンテナごと
     * 生成されず、後からトグルをONにしても出現できないため（トグル変更は再描画のみで
     * コンテナを再生成しない）。
     * @param {DiagramContext} diagramContext
     * @returns {DiagramContext}
     */
    function withAllDiagramToggles(diagramContext) {
        const context = {...diagramContext};
        DIAGRAM_TOGGLES.forEach(({key}) => context[key] = true);
        return context;
    }

    /**
     * クラス図・パッケージ図で使う表示対象トグルと関連データをコンテキストから取り出す
     * @param {DiagramContext} diagramContext
     */
    function resolveClassLevelDiagramContext(diagramContext) {
        return {
            outboundOperationSet: diagramContext.outboundOperationSet || new Set(),
            inboundCallerIndex: diagramContext.inboundCallerIndex ?? buildInboundCallerIndex(),
            showDiagramOutboundPorts: diagramContext.showDiagramOutboundPorts ?? true,
            showDiagramDomainTypes: diagramContext.showDiagramDomainTypes ?? false,
            showDiagramInboundClasses: diagramContext.showDiagramInboundClasses ?? true,
        };
    }

    /**
     * inboundクラスからの呼び出し関係を、呼び出し先クラスFQNで引けるインデックスにする
     * @returns {Map<string, {callerClassFqn: string, calleeMethodFqn: string}[]>}
     */
    function buildInboundCallerIndex() {
        const map = new Map();
        Jig.data.inbound.getControllers().forEach(controller => {
            (controller.relations || []).forEach(relation => {
                if (!relation?.from || !relation?.to) return;
                Jig.util.pushToMap(map, getClassFqnFromMethodFqn(relation.to), {
                    callerClassFqn: getClassFqnFromMethodFqn(relation.from),
                    calleeMethodFqn: relation.to
                });
            });
        });
        return map;
    }

    /**
     * メソッドのパラメータ・戻り値からドメイン型ノード・エッジを収集する
     * @param {UsecaseMethod} method
     * @param {string} methodFqn
     * @param {Set<string>} domainFqnSet
     * @param {Set<string>} edgeSet
     * @param {DiagramEdge[]} edges
     * @param {function(string): void} addDomainNode
     */
    function collectDomainTypeNodesAndEdges(method, methodFqn, domainFqnSet, edgeSet, edges, addDomainNode) {
        (method.parameters || []).forEach(param => {
            Jig.util.collectTypeRefFqns(param.typeRef)
                .filter(domainFqn => domainFqnSet.has(domainFqn))
                .forEach(domainFqn => {
                    addDomainNode(domainFqn);
                    addEdgeOnce(edgeSet, edges, domainFqn, methodFqn, true);
                });
        });
        Jig.util.collectTypeRefFqns(method.returnTypeRef)
            .filter(returnFqn => returnFqn !== 'void' && domainFqnSet.has(returnFqn))
            .forEach(returnFqn => {
                addDomainNode(returnFqn);
                addEdgeOnce(edgeSet, edges, methodFqn, returnFqn, true);
            });
    }

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
     * 出力インタフェースのオペレーションFQNからパラメータリストを引けるようにする
     * @param {OutboundData} outboundData
     * @returns {Map<string, MethodParameter[]>}
     */
    function buildOutboundOperationParameterMap(outboundData) {
        const map = new Map();
        (outboundData?.outboundPorts || []).forEach(port => {
            (port.operations || []).forEach(op => map.set(op.fqn, op.parameters || []));
        });
        return map;
    }

    /**
     * 呼び出し先メソッドFQNからパラメータリストを取得する（内部メソッド/出力インタフェースの両方に対応）
     * @param {string} calleeFqn
     * @param {DiagramContext} diagramContext
     * @returns {MethodParameter[]}
     */
    function resolveCalleeParameters(calleeFqn, diagramContext) {
        const method = diagramContext.methodMap?.get(calleeFqn);
        if (method) return method.parameters || [];
        return diagramContext.outboundOperationParameterMap?.get(calleeFqn) || [];
    }

    /**
     * パラメータリストから呼び出しエッジのラベル用テキストを組み立てる。
     * 実引数の値ではなく、呼び出し先メソッドの仮引数（名前が取得できていれば名前、できなければ型のみ）を示す。
     * @param {MethodParameter[]} parameters
     * @param {function(string): string} typeLabel
     * @returns {string}
     */
    function buildArgumentsLabel(parameters, typeLabel) {
        if (!parameters || parameters.length === 0) return '';
        return parameters.map(param => {
            const type = typeLabel(param.typeRef?.fqn);
            return param.nameSource === 'METHOD_PARAMETERS' ? `${param.name}: ${type}` : type;
        }).join(', ');
    }

    /**
     * @param {Usecase[]} usecases
     * @returns {Map<string, UsecaseMethod>}
     */
    function buildMethodMap(usecases) {
        const map = new Map();
        usecases.forEach(usecase => {
            (usecase.methods || []).forEach(m => map.set(m.fqn, {...m, kind: isUsecase(m) ? "usecase" : "method"}));
            (usecase.staticMethods || []).forEach(m => map.set(m.fqn, {...m, kind: "static-method"}));
        });
        return map;
    }

    /**
     * @param {Map<string, UsecaseMethod>} methodMap
     * @returns {Map<string, DiagramNode[]>}
     */
    function buildReverseCallerMap(methodMap) {
        const map = new Map();
        function addEntry(calleeFqn, callerNode) {
            if (!calleeFqn || !callerNode?.fqn) return;
            Jig.util.pushToMap(map, calleeFqn, callerNode);
        }
        for (const method of methodMap.values()) {
            (method.callMethods || []).forEach(calleeFqn =>
                addEntry(calleeFqn, {fqn: method.fqn, kind: method.kind})
            );
        }
        Jig.data.inbound.getControllers().forEach(controller => {
            (controller.relations || []).forEach(relation => {
                if (!relation?.from || !relation?.to) return;
                const callerClassFqn = getClassFqnFromMethodFqn(relation.from);
                if (methodMap.has(callerClassFqn)) return;
                addEntry(relation.to, {fqn: relation.from, kind: "inbound-method"});
            });
        });
        return map;
    }

    /**
     * @param {UsecaseMethod} rootMethod
     * @param {DiagramContext} diagramContext
     * @returns {{nodes: DiagramNode[], edges: DiagramEdge[]}}
     */
    function buildUsecaseDiagram(rootMethod, diagramContext) {
        const nodes = new Map();
        const edgeSet = new Set();
        const edges = [];
        const visited = new Set();

        nodes.set(rootMethod.fqn, {fqn: rootMethod.fqn, kind: "usecase"});
        visited.add(rootMethod.fqn);

        function shouldIncludeMethodNode(kind) {
            return diagramContext.showDiagramInternalMethods || kind === "usecase";
        }

        const reverseCallerMap = diagramContext.reverseCallerMap;

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

        if (diagramContext.showDiagramCallers !== false) {
            collectVisibleCallers(rootMethod.fqn).forEach((kind, callerFqn) => {
                const edgeKey = makeEdgeKey(callerFqn, rootMethod.fqn);
                if (!edgeSet.has(edgeKey)) {
                    edgeSet.add(edgeKey);
                    edges.push({from: callerFqn, to: rootMethod.fqn});
                }
                if (!nodes.has(callerFqn)) {
                    nodes.set(callerFqn, {fqn: callerFqn, kind});
                }
            });
        }

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
                        const edgeKey = makeEdgeKey(effectiveCallerFqn, calleeFqn);
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
                    const edgeKey = makeEdgeKey(effectiveCallerFqn, calleeFqn);
                    if (!edgeSet.has(edgeKey)) {
                        edgeSet.add(edgeKey);
                        edges.push({from: effectiveCallerFqn, to: calleeFqn});
                    }
                    if (!nodes.has(calleeFqn)) {
                        nodes.set(calleeFqn, {fqn: calleeFqn, kind: "outbound-method"});
                    }
                }
            }
        }

        if (diagramContext.showDiagramCallees !== false) {
            traverse(rootMethod.fqn, rootMethod.callMethods);
        }

        const domainFqnSet = Jig.data.domain.getDomainFqnSet();
        if (diagramContext.showDiagramDomainTypes && domainFqnSet.size > 0) {
            [...nodes.keys()].forEach(fqn => {
                const method = diagramContext.methodMap.get(fqn);
                if (!method) return; // outbound / inbound-class はスキップ
                collectDomainTypeNodesAndEdges(method, fqn, domainFqnSet, edgeSet, edges, domainFqn => {
                    if (!nodes.has(domainFqn)) {
                        nodes.set(domainFqn, {fqn: domainFqn, kind: "domain-type"});
                    }
                });
            });
        }

        return {nodes: [...nodes.values()], edges};
    }

    /**
     * @param {Usecase} usecase
     * @param {Set<string>|null} handlerFqns ハンドラのみ表示時のFQN集合、nullはすべて表示
     * @param {DiagramContext} [diagramContext]
     * @returns {{nodes: DiagramNode[], edges: DiagramEdge[]}}
     */
    function buildClassGraph(usecase, handlerFqns = null, diagramContext = {}) {
        /** @type {DiagramNode[]} */
        const nodes = [];
        /** @type {DiagramEdge[]} */
        const edges = [];
        const edgeSet = new Set();
        const domainNodeSet = new Set();
        const outboundNodeSet = new Set();
        const classMethods = [...usecase.methods.filter(m => isUsecase(m) && (!handlerFqns || handlerFqns.has(m.fqn))), ...usecase.staticMethods];
        const methodFqns = new Set(classMethods.map(m => m.fqn));
        const staticMethodFqns = new Set(usecase.staticMethods.map(m => m.fqn));
        const domainFqnSet = Jig.data.domain.getDomainFqnSet();
        const {outboundOperationSet, inboundCallerIndex, showDiagramOutboundPorts, showDiagramDomainTypes, showDiagramInboundClasses}
            = resolveClassLevelDiagramContext(diagramContext);

        classMethods.forEach(method => {
            const kind = isUsecase(method) ? "usecase" : (staticMethodFqns.has(method.fqn) ? "static-method" : "method");
            nodes.push({fqn: method.fqn, kind});

            (method.callMethods || []).forEach(calleeFqn => {
                if (methodFqns.has(calleeFqn)) {
                    addEdgeOnce(edgeSet, edges, method.fqn, calleeFqn);
                } else if (showDiagramOutboundPorts && outboundOperationSet.has(calleeFqn)) {
                    const outboundClassFqn = getClassFqnFromMethodFqn(calleeFqn);
                    addNodeOnce(outboundNodeSet, nodes, outboundClassFqn, "outbound-class");
                    addEdgeOnce(edgeSet, edges, method.fqn, outboundClassFqn);
                }
            });

            if (showDiagramDomainTypes) {
                collectDomainTypeNodesAndEdges(method, method.fqn, domainFqnSet, edgeSet, edges,
                    domainFqn => addNodeOnce(domainNodeSet, nodes, domainFqn, "domain-type"));
            }
        });

        if (showDiagramInboundClasses) {
            const inboundNodeSet = new Set();
            (inboundCallerIndex.get(usecase.fqn) || [])
                .filter(({calleeMethodFqn}) => methodFqns.has(calleeMethodFqn))
                .forEach(({callerClassFqn, calleeMethodFqn}) => {
                    addNodeOnce(inboundNodeSet, nodes, callerClassFqn, "inbound-class");
                    addEdgeOnce(edgeSet, edges, callerClassFqn, calleeMethodFqn);
                });
        }

        return {nodes, edges};
    }

    /**
     * パッケージに含まれるクラスをユースケースと見立てたグラフを組み立てる。
     * クラス間の呼び出しは全メソッド由来、ドメインモデルの入出力は公開インタフェース（ユースケースとstaticメソッド）由来とする。
     * @param {Usecase[]} packageUsecases
     * @param {DiagramContext} [diagramContext]
     * @returns {{nodes: DiagramNode[], edges: DiagramEdge[]}}
     */
    function buildPackageGraph(packageUsecases, diagramContext = {}) {
        /** @type {DiagramNode[]} */
        const nodes = [];
        /** @type {DiagramEdge[]} */
        const edges = [];
        const edgeSet = new Set();
        const domainNodeSet = new Set();
        const outboundNodeSet = new Set();
        const domainFqnSet = Jig.data.domain.getDomainFqnSet();
        const {outboundOperationSet, inboundCallerIndex, showDiagramOutboundPorts, showDiagramDomainTypes, showDiagramInboundClasses}
            = resolveClassLevelDiagramContext(diagramContext);
        const classFqns = new Set(packageUsecases.map(usecase => usecase.fqn));

        packageUsecases.forEach(usecase => {
            nodes.push({fqn: usecase.fqn, kind: "usecase"});

            const collectCallEdges = (method) => {
                (method.callMethods || []).forEach(calleeFqn => {
                    const calleeClassFqn = getClassFqnFromMethodFqn(calleeFqn);
                    if (calleeClassFqn === usecase.fqn) return;
                    if (classFqns.has(calleeClassFqn)) {
                        addEdgeOnce(edgeSet, edges, usecase.fqn, calleeClassFqn);
                    } else if (showDiagramOutboundPorts && outboundOperationSet.has(calleeFqn)) {
                        addNodeOnce(outboundNodeSet, nodes, calleeClassFqn, "outbound-class");
                        addEdgeOnce(edgeSet, edges, usecase.fqn, calleeClassFqn);
                    }
                });
            };
            const collectDomainEdges = (method) =>
                collectDomainTypeNodesAndEdges(method, usecase.fqn, domainFqnSet, edgeSet, edges,
                    domainFqn => addNodeOnce(domainNodeSet, nodes, domainFqn, "domain-type"));

            usecase.methods.forEach(method => {
                collectCallEdges(method);
                if (showDiagramDomainTypes && isUsecase(method)) collectDomainEdges(method);
            });
            usecase.staticMethods.forEach(method => {
                collectCallEdges(method);
                if (showDiagramDomainTypes) collectDomainEdges(method);
            });
        });

        if (showDiagramInboundClasses) {
            const inboundNodeSet = new Set();
            classFqns.forEach(classFqn => {
                (inboundCallerIndex.get(classFqn) || []).forEach(({callerClassFqn}) => {
                    if (classFqns.has(callerClassFqn)) return;
                    addNodeOnce(inboundNodeSet, nodes, callerClassFqn, "inbound-class");
                    addEdgeOnce(edgeSet, edges, callerClassFqn, classFqn);
                });
            });
        }

        return {nodes, edges};
    }

    /**
     * パッケージに含まれるクラスの公開メソッド（ユースケースとstaticメソッド）をユースケースと見立てたグラフを組み立てる。
     * クラスはノードをまとめるための subgraph 情報（classFqn）としてのみ扱う。
     * @param {Usecase[]} packageUsecases
     * @param {DiagramContext} [diagramContext]
     * @returns {{nodes: DiagramNode[], edges: DiagramEdge[]}}
     */
    function buildPackageMethodGraph(packageUsecases, diagramContext = {}) {
        /** @type {DiagramNode[]} */
        const nodes = [];
        /** @type {DiagramEdge[]} */
        const edges = [];
        const edgeSet = new Set();
        const domainNodeSet = new Set();
        const outboundNodeSet = new Set();
        const domainFqnSet = Jig.data.domain.getDomainFqnSet();
        const {outboundOperationSet, inboundCallerIndex, showDiagramOutboundPorts, showDiagramDomainTypes, showDiagramInboundClasses}
            = resolveClassLevelDiagramContext(diagramContext);
        const classFqns = new Set(packageUsecases.map(usecase => usecase.fqn));
        const methodFqns = new Set();
        packageUsecases.forEach(usecase => {
            usecase.methods.filter(isUsecase).forEach(method => methodFqns.add(method.fqn));
            usecase.staticMethods.forEach(method => methodFqns.add(method.fqn));
        });

        packageUsecases.forEach(usecase => {
            const publicMethods = [...usecase.methods.filter(isUsecase), ...usecase.staticMethods];

            publicMethods.forEach(method => {
                nodes.push({fqn: method.fqn, kind: "usecase", classFqn: usecase.fqn});

                (method.callMethods || []).forEach(calleeFqn => {
                    if (methodFqns.has(calleeFqn)) {
                        addEdgeOnce(edgeSet, edges, method.fqn, calleeFqn);
                    } else if (showDiagramOutboundPorts && outboundOperationSet.has(calleeFqn)) {
                        const outboundClassFqn = getClassFqnFromMethodFqn(calleeFqn);
                        addNodeOnce(outboundNodeSet, nodes, outboundClassFqn, "outbound-class");
                        addEdgeOnce(edgeSet, edges, method.fqn, outboundClassFqn);
                    }
                });

                if (showDiagramDomainTypes) {
                    collectDomainTypeNodesAndEdges(method, method.fqn, domainFqnSet, edgeSet, edges,
                        domainFqn => addNodeOnce(domainNodeSet, nodes, domainFqn, "domain-type"));
                }
            });
        });

        if (showDiagramInboundClasses) {
            const inboundNodeSet = new Set();
            classFqns.forEach(classFqn => {
                (inboundCallerIndex.get(classFqn) || [])
                    .filter(({calleeMethodFqn}) => methodFqns.has(calleeMethodFqn))
                    .forEach(({callerClassFqn, calleeMethodFqn}) => {
                        if (classFqns.has(callerClassFqn)) return;
                        addNodeOnce(inboundNodeSet, nodes, callerClassFqn, "inbound-class");
                        addEdgeOnce(edgeSet, edges, callerClassFqn, calleeMethodFqn);
                    });
            });
        }

        return {nodes, edges};
    }

    const SequenceDiagram = {
        /**
         * @param {UsecaseMethod} rootMethod
         * @param {DiagramContext} diagramContext
         * @returns {SequenceDiagram}
         */
        buildDiagram(rootMethod, diagramContext) {
            // シーケンス図は物理名切り替えに対応していないため常に用語名表示
            const {type: typeLabel} = Jig.glossary.makeLabels(false);
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
                            const label = diagramContext.showDiagramArguments
                                ? buildArgumentsLabel(m.parameters, typeLabel)
                                : '';
                            calls.push({from: caller.id, to: callee.id, label});
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
                        const argumentsLabel = diagramContext.showDiagramArguments
                            ? buildArgumentsLabel(resolveCalleeParameters(calleeFqn, diagramContext), typeLabel)
                            : '';
                        const label = argumentsLabel ? `${methodName}(${argumentsLabel})` : methodName;
                        calls.push({from: caller.id, to: callee.id, label});
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

        // テキストフィルタ: クラス名/所属パッケージ名一致→全メソッド表示、メソッド名一致→該当メソッドのみ表示
        const filteredItems = usecases.flatMap(usecase => {
            const visibleMethods = usecase.methods.filter(isVisibleMethod);
            if (visibleMethods.length === 0) return [];
            if (!filterText) return [{usecase, methods: visibleMethods}];

            const classTitle = Jig.glossary.getTypeTerm(usecase.fqn).title.toLowerCase();
            if (classTitle.includes(filterText) || Jig.glossary.packageHierarchyMatchesFilter(usecase.fqn, filterText)) {
                return [{usecase, methods: visibleMethods}];
            }

            const matchingMethods = visibleMethods.filter(m =>
                Jig.glossary.getMethodTerm(m.fqn).title.toLowerCase().includes(filterText)
            );
            return matchingMethods.length > 0 ? [{usecase, methods: matchingMethods}] : [];
        });

        Jig.dom.sidebar.renderTreeSection(sidebar, {
            // グループが「ユースケース」の1つしかなく見出しが冗長なため非表示にする
            showTitle: false,
            title: "usecase", // 表示されないが定義しておく
            items: filteredItems,
            getFqn: item => item.usecase.fqn,
            renderLeaf: ({usecase, methods}) => {
                const methodList = Jig.dom.createElement("ul", {
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
                });
                const header = Jig.dom.createElement("div", {
                    className: "in-page-sidebar__item-header",
                    children: [
                        Jig.dom.createElement("a", {
                            className: "in-page-sidebar__link",
                            attributes: {href: "#" + fqnToTypeId(usecase.fqn)},
                            textContent: Jig.glossary.getTypeTerm(usecase.fqn).title
                        }),
                        Jig.dom.sidebar.createToggle(methodList)
                    ]
                });
                return Jig.dom.createElement("li", {
                    className: "in-page-sidebar__item",
                    children: [header, methodList]
                });
            },
            packageHref: Jig.dom.sidebar.packageHeadingHref
        });
    }

    /**
     * サイドバーの表示設定（全体設定）に対して、ダイアグラムごとの上書きを保持しつつ
     * コンテキストメニュー項目を組み立てる共通ヘルパー。jig-mermaid.js の
     * createDiagramSettingsOverride（domain.js と共有）に、DiagramContext形式での取得を足したもの。
     * @param {function(): DiagramContext} buildCurrentDiagramContext
     * @param {{key: string, label: string}[]} toggles
     * @returns {{getContext: function(): DiagramContext, buildExtraMenuItems: function(function(): void): object[]}}
     */
    function createDiagramContextOverrideMenu(buildCurrentDiagramContext, toggles) {
        const settingsOverride = Jig.mermaid.render.createDiagramSettingsOverride(
            toggles.map(({key, label}) => ({key, label, getGlobalValue: () => !!buildCurrentDiagramContext()[key]}))
        );
        return {
            getContext: () => ({...buildCurrentDiagramContext(), ...settingsOverride.getValues()}),
            buildExtraMenuItems: settingsOverride.buildExtraMenuItems
        };
    }

    /**
     * @param {UsecaseMethod} method
     * @param {function(): DiagramContext} buildCurrentDiagramContext
     * @returns {function}
     */
    function createUsecaseDiagramGenerator(method, buildCurrentDiagramContext) {
        const contextMenu = createDiagramContextOverrideMenu(buildCurrentDiagramContext, [
            {key: 'showDiagramCallers', label: '呼び出し元'},
            {key: 'showDiagramCallees', label: '呼び出し先'},
            {key: 'showDiagramInternalMethods', label: '内部メソッド'},
            {key: 'showDiagramOutboundPorts', label: '出力インタフェース'},
            {key: 'showDiagramDomainTypes', label: 'ドメインモデル'},
            {key: 'showDiagramArguments', label: '引数'}
        ]);

        const generator = (dir, opts) => {
            const {type: typeLabel, method: mLabel} = Jig.glossary.makeLabels(opts?.showPhysicalName);
            const context = contextMenu.getContext();
            const currentUsecaseDiagram = buildUsecaseDiagram(method, context);
            const builder = Jig.mermaid.createBuilder();
            const classSubgraphs = new Map();
            const ensureClassSubgraph = (fqn) => {
                const classFqn = getClassFqnFromMethodFqn(fqn);
                return {classFqn, subgraph: builder.ensureSubgraph(classSubgraphs, Jig.util.fqnToId("sg", classFqn), typeLabel(classFqn), 'LR')};
            };
            currentUsecaseDiagram.nodes.forEach(node => {
                const nodeId = fqnToNodeId(node.fqn);
                if (node.kind === "inbound-method") {
                    const {subgraph, classFqn} = ensureClassSubgraph(node.fqn);
                    builder.addNodeToSubgraph(subgraph, nodeId, mLabel(node.fqn), 'method');
                    builder.addClass(nodeId, "inbound");
                    builder.addClick(nodeId, Jig.mermaid.nav.inboundAdapterUrl(classFqn), node.fqn);
                } else if (node.kind === "outbound-method") {
                    const {subgraph, classFqn} = ensureClassSubgraph(node.fqn);
                    builder.addNodeToSubgraph(subgraph, nodeId, mLabel(node.fqn), 'method');
                    builder.addClass(nodeId, "outbound");
                    builder.addClick(nodeId, Jig.mermaid.nav.outboundPortUrl(classFqn), node.fqn);
                } else if (node.kind === "domain-type") {
                    builder.addNode(nodeId, typeLabel(node.fqn), 'class');
                    builder.addClass(nodeId, "domain");
                    builder.addClick(nodeId, Jig.mermaid.nav.domainTypeUrl(node.fqn), node.fqn);
                } else if (node.kind === "usecase") {
                    const {subgraph} = ensureClassSubgraph(node.fqn);
                    builder.addNodeToSubgraph(subgraph, nodeId, mLabel(node.fqn), 'method');
                    builder.addClass(nodeId, "usecase");
                    if (node.fqn === method.fqn) builder.addStyle(nodeId, "font-weight:bold");
                    builder.addClick(nodeId, "#" + fqnToMethodId(node.fqn), node.fqn);
                } else {
                    const {subgraph} = ensureClassSubgraph(node.fqn);
                    builder.addNodeToSubgraph(subgraph, nodeId, mLabel(node.fqn), 'method');
                    builder.addClass(nodeId, "inactive");
                    builder.addTooltip(nodeId, node.fqn);
                }
            });
            currentUsecaseDiagram.edges.forEach(edge => {
                const label = (!edge.dotted && context.showDiagramArguments)
                    ? buildArgumentsLabel(resolveCalleeParameters(edge.to, context), typeLabel)
                    : "";
                builder.addEdge(fqnToNodeId(edge.from), fqnToNodeId(edge.to), label, edge.dotted ?? false);
            });
            return builder.build(dir);
        };

        generator.buildExtraMenuItems = contextMenu.buildExtraMenuItems;

        return generator;
    }

    /**
     * @param {UsecaseMethod} method
     * @param {function(): DiagramContext} buildCurrentDiagramContext
     * @returns {function}
     */
    function createSequenceDiagramGenerator(method, buildCurrentDiagramContext) {
        const contextMenu = createDiagramContextOverrideMenu(buildCurrentDiagramContext, [
            {key: 'showDiagramInternalMethods', label: '内部メソッド'},
            {key: 'showDiagramOutboundPorts', label: '出力インタフェース'},
            {key: 'showDiagramArguments', label: '引数'}
        ]);

        const generator = () => {
            const sequenceDiagram = SequenceDiagram.buildDiagram(method, contextMenu.getContext());
            return SequenceDiagram.buildCode(sequenceDiagram);
        };

        generator.buildExtraMenuItems = contextMenu.buildExtraMenuItems;

        return generator;
    }

    /**
     * @param {UsecaseMethod} method
     * @param {function(): DiagramContext} buildCurrentDiagramContext
     * @returns {HTMLElement}
     */
    function renderMethodSection(method, buildCurrentDiagramContext) {
        const methodTerm = Jig.glossary.getMethodTerm(method.fqn);
        const methodSection = Jig.dom.card.item({id: fqnToMethodId(method.fqn), title: methodTerm.title, tagName: "article"});
        methodSection.appendChild(Jig.dom.createElement("div", {className: "declaration", textContent: methodTerm.shortDeclaration}));
        methodSection.appendChild(Jig.dom.type.methodIOSection(method.parameters, method.returnTypeRef));

        if (methodTerm.description) {
            methodSection.appendChild(Jig.dom.createElement("section", {
                className: "description",
                children: [Jig.dom.createMarkdownElement(methodTerm.description)]
            }));
        }

        // 図の有無は全トグルONの文脈で判定する（描画自体は現在のトグル状態に従う）
        const gateContext = withAllDiagramToggles(buildCurrentDiagramContext());
        const usecaseDiagram = buildUsecaseDiagram(method, gateContext);
        const hasUsecaseDiagram = usecaseDiagram.edges.length > 0;

        const sequenceDiagram = SequenceDiagram.buildDiagram(method, gateContext);
        const sequenceDiagramCode = SequenceDiagram.buildCode(sequenceDiagram);
        const hasSequenceDiagram = sequenceDiagramCode !== null;

        if (hasUsecaseDiagram || hasSequenceDiagram) {
            let usecaseTarget, sequenceTarget;

            if (hasUsecaseDiagram && hasSequenceDiagram) {
                const selectedTab = state.selectedTabs.get(method.fqn) || 'usecase';
                const {panels, section} = Jig.dom.tab.buildSection(
                    [{id: 'usecase', label: 'ユースケース図'}, {id: 'sequence', label: 'シーケンス図'}],
                    {className: "jig-card-section tab-content-section tab-diagram-section", initialActiveId: selectedTab, onTabChange: id => state.selectedTabs.set(method.fqn, id)}
                );
                methodSection.appendChild(section);
                usecaseTarget = panels['usecase'];
                sequenceTarget = panels['sequence'];
            } else {
                const container = Jig.dom.createElement("div", {className: "jig-card-section diagram-container"});
                methodSection.appendChild(container);
                if (hasUsecaseDiagram) usecaseTarget = container;
                else sequenceTarget = container;
            }

            if (hasUsecaseDiagram) {
                Jig.mermaid.diagram.createAndRegister(usecaseTarget, (mmdContainer) => {
                    mmdContainer.innerHTML = "";
                    Jig.mermaid.render.renderWithControls(mmdContainer, createUsecaseDiagramGenerator(method, buildCurrentDiagramContext), {direction: 'LR', enableLabelToggle: true});
                });
            }

            if (hasSequenceDiagram) {
                Jig.mermaid.diagram.createAndRegister(sequenceTarget, (sequenceContainer) => {
                    sequenceContainer.innerHTML = "";
                    Jig.mermaid.render.renderWithControls(sequenceContainer, createSequenceDiagramGenerator(method, buildCurrentDiagramContext));
                });
            }
        }

        return methodSection;
    }

    function visibleUsecaseMethodsOf(usecase, handlerFqns) {
        return usecase.methods.filter(
            method => isUsecase(method) && (!handlerFqns || handlerFqns.has(method.fqn))
        );
    }

    function renderUsecaseCard(usecase, visibleUsecaseMethods, handlerFqns, buildCurrentDiagramContext) {
        const term = Jig.glossary.getTypeTerm(usecase.fqn);
        const section = Jig.dom.card.type({id: fqnToTypeId(usecase.fqn), title: term.title, fqn: usecase.fqn, titleSuffix: Jig.glossary.sourceLink(usecase.fqn)});

        if (term.description) {
            section.appendChild(Jig.dom.createElement("section", {
                className: "jig-card-section description",
                children: [Jig.dom.createMarkdownElement(term.description)]
            }));
        }

        // 図の有無は全トグルONの文脈で判定する（描画自体は現在のトグル状態に従う）
        const classGraph = buildClassGraph(usecase, handlerFqns, withAllDiagramToggles(buildCurrentDiagramContext()));
        if (classGraph.edges.length > 0) {
            const classDiagramContainer = Jig.dom.createElement("div", {className: "jig-card-section diagram-container class-diagram"});
            section.appendChild(classDiagramContainer);
            Jig.mermaid.diagram.createAndRegister(classDiagramContainer, (mmdContainer) => {
                mmdContainer.innerHTML = "";
                Jig.mermaid.render.renderWithControls(mmdContainer, createClassDiagramGenerator(usecase, handlerFqns, buildCurrentDiagramContext), {direction: 'LR', enableLabelToggle: true});
            });
        }

        const fieldsList = Jig.dom.type.fieldsList(usecase.fields, {showTitle: false});
        if (fieldsList) fieldsList.classList.add("fields");

        const staticList = usecase.staticMethods.length > 0
            ? Jig.dom.type.methodsList("staticメソッド", usecase.staticMethods, {showTitle: false})
            : null;
        if (staticList) staticList.classList.add("static-methods");

        const internalMethods = usecase.methods.filter(method => !isUsecase(method));
        const methodList = internalMethods.length > 0
            ? Jig.dom.type.methodsList("メソッド", internalMethods, {showTitle: false})
            : null;
        if (methodList) methodList.classList.add("methods");

        const memberTabDefs = [
            fieldsList && {id: 'fields', label: 'フィールド', el: fieldsList},
            staticList && {id: 'static-methods', label: 'staticメソッド', el: staticList},
            methodList && {id: 'methods', label: 'メソッド', el: methodList},
        ].filter(Boolean);

        if (memberTabDefs.length > 0) {
            const {panels, section: memberSection} = Jig.dom.tab.buildSection(memberTabDefs, {className: "jig-card-section tab-content-section tab-member-section"});
            section.appendChild(memberSection);
            memberTabDefs.forEach(tab => panels[tab.id].appendChild(tab.el));
        }

        visibleUsecaseMethods.forEach(method => {
            section.appendChild(renderMethodSection(method, buildCurrentDiagramContext));
        });

        return section;
    }

    /**
     * inbound-class/outbound-class/domain-typeのノードをリンク付きでbuilderへ追加する。
     * @param {MermaidBuilder} builder
     * @param {DiagramNode} node
     * @param {function(string): string} typeLabel
     * @returns {boolean} 該当するkindを追加した場合true
     */
    function addLinkedClassNode(builder, node, typeLabel) {
        const nodeId = fqnToNodeId(node.fqn);
        if (node.kind === "inbound-class") {
            builder.addNode(nodeId, typeLabel(node.fqn), 'class');
            builder.addClass(nodeId, "inbound");
            builder.addClick(nodeId, Jig.mermaid.nav.inboundAdapterUrl(node.fqn), node.fqn);
        } else if (node.kind === "outbound-class") {
            builder.addNode(nodeId, typeLabel(node.fqn), 'class');
            builder.addClass(nodeId, "outbound");
            builder.addClick(nodeId, Jig.mermaid.nav.outboundPortUrl(node.fqn), node.fqn);
        } else if (node.kind === "domain-type") {
            builder.addNode(nodeId, typeLabel(node.fqn), 'class');
            builder.addClass(nodeId, "domain");
            builder.addClick(nodeId, Jig.mermaid.nav.domainTypeUrl(node.fqn), node.fqn);
        } else {
            return false;
        }
        return true;
    }

    /**
     * パッケージに含まれるクラスをユースケースと見立てたユースケース図のジェネレータ
     * @param {Usecase[]} packageUsecases
     * @param {function(): DiagramContext} buildCurrentDiagramContext
     * @returns {function}
     */
    function createPackageDiagramGenerator(packageUsecases, buildCurrentDiagramContext) {
        const contextMenu = createDiagramContextOverrideMenu(buildCurrentDiagramContext, PACKAGE_DIAGRAM_MENU_TOGGLES);

        const generator = (dir, opts) => {
            const {type: typeLabel, method: mLabel, pkg: pkgLabel} = Jig.glossary.makeLabels(opts?.showPhysicalName);
            const context = contextMenu.getContext();
            const methodLevel = !!context.showDiagramMethodLevel;
            const packageGraph = methodLevel
                ? buildPackageMethodGraph(packageUsecases, context)
                : buildPackageGraph(packageUsecases, context);
            const builder = Jig.mermaid.createBuilder();
            const subgraphs = new Map();
            packageGraph.nodes.forEach(node => {
                if (addLinkedClassNode(builder, node, typeLabel)) return;
                const nodeId = fqnToNodeId(node.fqn);
                if (methodLevel) {
                    const subgraph = builder.ensureSubgraph(subgraphs, Jig.util.fqnToId("sg", node.classFqn), typeLabel(node.classFqn), 'LR');
                    builder.addNodeToSubgraph(subgraph, nodeId, mLabel(node.fqn), 'method');
                    builder.addClass(nodeId, "usecase");
                    builder.addClick(nodeId, "#" + fqnToMethodId(node.fqn), node.fqn);
                } else {
                    const packageFqn = Jig.util.getPackageFqnFromTypeFqn(node.fqn);
                    const subgraph = builder.ensureSubgraph(subgraphs, Jig.util.fqnToId("sg", packageFqn), pkgLabel(packageFqn), 'LR');
                    builder.addNodeToSubgraph(subgraph, nodeId, typeLabel(node.fqn), 'method');
                    builder.addClass(nodeId, "usecase");
                    builder.addClick(nodeId, "#" + fqnToTypeId(node.fqn), node.fqn);
                }
            });
            packageGraph.edges.forEach(edge => {
                builder.addEdge(fqnToNodeId(edge.from), fqnToNodeId(edge.to), "", edge.dotted ?? false);
            });
            return builder.build(dir);
        };

        generator.buildExtraMenuItems = contextMenu.buildExtraMenuItems;

        return generator;
    }

    /**
     * @param {Usecase} usecase
     * @param {Set<string>|null} handlerFqns
     * @param {function(): DiagramContext} buildCurrentDiagramContext
     * @returns {function}
     */
    function createClassDiagramGenerator(usecase, handlerFqns, buildCurrentDiagramContext) {
        const contextMenu = createDiagramContextOverrideMenu(buildCurrentDiagramContext, CLASS_LEVEL_DIAGRAM_MENU_TOGGLES);

        const generator = (dir, opts) => {
            const {type: typeLabel, method: mLabel} = Jig.glossary.makeLabels(opts?.showPhysicalName);
            const classGraph = buildClassGraph(usecase, handlerFqns, contextMenu.getContext());
            const builder = Jig.mermaid.createBuilder();
            const subgraph = builder.startSubgraph(Jig.util.fqnToId("sg", usecase.fqn), typeLabel(usecase.fqn), 'LR');
            classGraph.nodes.forEach(node => {
                const nodeId = fqnToNodeId(node.fqn);
                if (addLinkedClassNode(builder, node, typeLabel)) return;
                if (node.kind === "usecase") {
                    builder.addNodeToSubgraph(subgraph, nodeId, mLabel(node.fqn), 'method');
                    builder.addClass(nodeId, "usecase");
                    builder.addClick(nodeId, "#" + fqnToMethodId(node.fqn), node.fqn);
                } else {
                    builder.addNodeToSubgraph(subgraph, nodeId, mLabel(node.fqn), 'method');
                    builder.addClass(nodeId, "inactive");
                    builder.addTooltip(nodeId, node.fqn);
                }
            });
            classGraph.edges.forEach(edge => {
                builder.addEdge(fqnToNodeId(edge.from), fqnToNodeId(edge.to), "", edge.dotted ?? false);
            });
            return builder.build(dir);
        };

        generator.buildExtraMenuItems = contextMenu.buildExtraMenuItems;

        return generator;
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

        const methodMap = buildMethodMap(usecases);
        const reverseCallerMap = buildReverseCallerMap(methodMap);
        const inboundCallerIndex = buildInboundCallerIndex();

        const outboundOperationSet = buildOutboundOperationSet(Jig.data.outbound.get());
        const outboundOperationParameterMap = buildOutboundOperationParameterMap(Jig.data.outbound.get());

        const buildCurrentDiagramContext = () => {
            const context = {methodMap, reverseCallerMap, inboundCallerIndex, outboundOperationSet, outboundOperationParameterMap};
            DIAGRAM_TOGGLES.forEach(({id, key}) => context[key] = document.getElementById(id).checked);
            return context;
        };

        const handlerFqns = state.handlerFqns;

        // カードにならないusecaseを先に除き、用語を持つ中間パッケージの見出しが孤児にならないようにする
        const visibleUsecases = usecases
            .map(usecase => ({usecase, visibleMethods: visibleUsecaseMethodsOf(usecase, handlerFqns)}))
            .filter(({visibleMethods}) => !handlerFqns || visibleMethods.length > 0);
        // パッケージごとに見出しを置き、ユースケースカードをまとめる。サイドバーのパッケージノードのリンク先になる
        // 用語（package-info）を持つパッケージはクラスを直接含まなくても見出しと説明を表示する
        const sections = Jig.util.flattenPackageTree(visibleUsecases, ({usecase}) => usecase.fqn, Jig.glossary.hasTerm);
        sections.forEach(({fqn: packageFqn, items: packageUsecases}) => {
            const heading = Jig.dom.createPackageHeading(fqnToPackageId(packageFqn), packageFqn);
            container.appendChild(heading);
            appendPackageDiagram(heading, packageUsecases.map(({usecase}) => usecase), buildCurrentDiagramContext);
            packageUsecases.forEach(({usecase, visibleMethods}) =>
                container.appendChild(renderUsecaseCard(usecase, visibleMethods, handlerFqns, buildCurrentDiagramContext)));
        });
    }

    /**
     * パッケージ見出しに、パッケージ内クラスをユースケースと見立てたユースケース図を追加する。
     * クラス間の関連がない場合は追加しない。
     * @param {HTMLElement} headingSection
     * @param {Usecase[]} packageUsecases
     * @param {function(): DiagramContext} buildCurrentDiagramContext
     */
    function appendPackageDiagram(headingSection, packageUsecases, buildCurrentDiagramContext) {
        if (packageUsecases.length === 0) return;
        // 図の有無は全トグルONの文脈で判定する（描画自体は現在のトグル状態に従う）
        const packageGraph = buildPackageGraph(packageUsecases, withAllDiagramToggles(buildCurrentDiagramContext()));
        if (packageGraph.edges.length === 0) return;
        const diagramContainer = Jig.dom.createElement("div", {className: "jig-card-section diagram-container package-diagram"});
        headingSection.appendChild(diagramContainer);
        Jig.mermaid.diagram.createAndRegister(diagramContainer, (mmdContainer) => {
            mmdContainer.innerHTML = "";
            Jig.mermaid.render.renderWithControls(mmdContainer, createPackageDiagramGenerator(packageUsecases, buildCurrentDiagramContext), {direction: 'LR', enableLabelToggle: true});
        });
    }

    /**
     * 表示オプションの初期化
     */
    function initControls() {
        const controls = [
            {id: 'show-members', class: 'hide-usecase-members'},
            {id: 'show-diagrams', class: 'hide-usecase-diagrams'},
            {id: 'show-details', class: 'hide-usecase-details'},
            {id: 'show-descriptions', class: 'hide-usecase-descriptions'},
            {id: 'show-declarations', class: 'hide-usecase-declarations'},
            ...DIAGRAM_TOGGLES.map(({id}) => ({id, reRender: true}))
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

        Jig.dom.sidebar.initCollapseBtn();
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
        // モジュールキャッシュを再ロードしなくても状態がリセットされるよう、毎回 init で state をクリア
        Object.assign(state, createInitialState());

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
        buildOutboundOperationParameterMap,
        buildArgumentsLabel,
        buildReverseCallerMap,
        buildUsecaseDiagram,
        buildClassGraph,
        buildPackageGraph,
        buildPackageMethodGraph,
        createUsecaseDiagramGenerator,
        createSequenceDiagramGenerator,
        createClassDiagramGenerator,
        createPackageDiagramGenerator,
        SequenceDiagram,
        render,
        renderSidebar,
        renderUsecaseList,
    };
})();

Jig.bootstrap.register("usecase-model", UsecaseApp.init);

if (typeof module !== "undefined" && module.exports) {
    module.exports = UsecaseApp;
}
