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
     *
     * 設計契約: コンテナの有無は現在のトグル状態に依存させず「いずれかのトグルで
     * 内容が生まれ得るか」で判定する。トグル変更は既存コンテナの再描画のみで
     * コンテナを再生成せず、また図ごとのメニューでグローバルOFFのトグルも
     * その図だけONにできるため。DIAGRAM_TOGGLESを走査するので新しいトグルの
     * 追加時に個別対応は不要。図の有無判定は必ずこのコンテキストで行うこと。
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
     * 実引数の値ではなく、呼び出し先メソッドの仮引数の型を示す。
     * @param {MethodParameter[]} parameters
     * @param {function(string): string} typeLabel
     * @returns {string}
     */
    function buildArgumentsLabel(parameters, typeLabel) {
        if (!parameters || parameters.length === 0) return '';
        return parameters.map(param => typeLabel(param.typeRef?.fqn)).join(', ');
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
                if (methodMap.has(relation.from)) return; // 呼び出し元がusecase内メソッドなら上のループで登録済み
                addEntry(relation.to, {fqn: relation.from, kind: "inbound-method"});
            });
        });
        return map;
    }

    /**
     * @param {string} kind
     * @param {DiagramContext} diagramContext
     * @returns {boolean}
     */
    function shouldIncludeMethodNode(kind, diagramContext) {
        return diagramContext.showDiagramInternalMethods || kind === "usecase";
    }

    /** @typedef {{tryEnter: function(string): (InliningGuard|null)}} InliningGuard インライン化の展開可否を判定し、展開する場合は再帰用ガードを返す */

    /** パス単位のガード。循環のみ防ぎ、別経路からは同じメソッドを再展開する（シーケンス図が経路ごとの呼び出しを描くため） */
    function pathInliningGuard(path = new Set()) {
        return {
            tryEnter(fqn) {
                if (path.has(fqn)) return null;
                return pathInliningGuard(new Set(path).add(fqn));
            }
        };
    }

    /** 走査全体で共有するガード。各メソッドを一度だけ展開する（分岐合流するDAGでも線形。#1152） */
    function sharedVisitedInliningGuard() {
        const visited = new Set();
        const guard = {
            tryEnter(fqn) {
                if (visited.has(fqn)) return null;
                visited.add(fqn);
                return guard;
            }
        };
        return guard;
    }

    /**
     * 呼び出し先を辿り、対象外の内部メソッドは読み飛ばして（インライン化して）
     * 対象への呼び出しを収集する共通骨格。対象判定・インライン化対象・展開ガードは呼び出し側が与える。
     * @param {string[]} callMethods
     * @param {{isTarget: function(string): boolean, inlinableMethodOf: function(string): (UsecaseMethod|undefined), onTargetCallee: function(string): void, onOtherCallee: function(string): void}} config
     * @param {InliningGuard} guard
     */
    function traverseWithInlining(callMethods, config, guard) {
        const {isTarget, inlinableMethodOf, onTargetCallee, onOtherCallee} = config;
        (callMethods || []).forEach(calleeFqn => {
            if (isTarget(calleeFqn)) {
                onTargetCallee(calleeFqn);
                return;
            }
            const inlinable = inlinableMethodOf(calleeFqn);
            if (inlinable) {
                const nextGuard = guard.tryEnter(calleeFqn);
                if (nextGuard) traverseWithInlining(inlinable.callMethods, config, nextGuard);
            } else {
                onOtherCallee(calleeFqn);
            }
        });
    }

    /**
     * 呼び出し先を辿り、非表示の内部メソッドは読み飛ばして（インライン化して）
     * 可視ノード／出力インタフェースへの呼び出しを収集する。可視ノードは新たな呼び出し元として
     * さらに辿る。usecase図（ノード・エッジ）とシーケンス図（参加者・呼び出し）の両方が同じ辿り方を使う。
     * @param {string} effectiveCallerFqn
     * @param {string[]} callMethods
     * @param {DiagramContext} diagramContext
     * @param {Set<string>} visited
     * @param {{onVisibleCallee: function(string, string, UsecaseMethod): void, onOutboundCallee: function(string, string): void}} handlers
     */
    function traverseCallGraph(effectiveCallerFqn, callMethods, diagramContext, visited, handlers) {
        const methodMap = diagramContext.methodMap;
        traverseWithInlining(callMethods, {
            isTarget: fqn => methodMap.has(fqn) && shouldIncludeMethodNode(methodMap.get(fqn).kind, diagramContext),
            inlinableMethodOf: fqn => methodMap.get(fqn),
            onTargetCallee: calleeFqn => {
                const m = methodMap.get(calleeFqn);
                handlers.onVisibleCallee(effectiveCallerFqn, calleeFqn, m);
                if (visited.has(calleeFqn)) return;
                visited.add(calleeFqn);
                traverseCallGraph(calleeFqn, m.callMethods, diagramContext, visited, handlers);
            },
            onOtherCallee: calleeFqn => {
                if (!diagramContext.outboundOperationSet.has(calleeFqn)) return;
                if (!diagramContext.showDiagramOutboundPorts) return;
                handlers.onOutboundCallee(effectiveCallerFqn, calleeFqn);
            }
        }, pathInliningGuard());
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
                    if (!shouldIncludeMethodNode(caller.kind, diagramContext)) return;
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
                addEdgeOnce(edgeSet, edges, callerFqn, rootMethod.fqn);
                if (!nodes.has(callerFqn)) {
                    nodes.set(callerFqn, {fqn: callerFqn, kind});
                }
            });
        }

        if (diagramContext.showDiagramCallees !== false) {
            traverseCallGraph(rootMethod.fqn, rootMethod.callMethods, diagramContext, visited, {
                onVisibleCallee: (callerFqn, calleeFqn, m) => {
                    addEdgeOnce(edgeSet, edges, callerFqn, calleeFqn);
                    if (!nodes.has(calleeFqn)) {
                        nodes.set(calleeFqn, {fqn: calleeFqn, kind: m.kind});
                    }
                },
                onOutboundCallee: (callerFqn, calleeFqn) => {
                    addEdgeOnce(edgeSet, edges, callerFqn, calleeFqn);
                    if (!nodes.has(calleeFqn)) {
                        nodes.set(calleeFqn, {fqn: calleeFqn, kind: "outbound-method"});
                    }
                }
            });
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
     * 呼び出し先を辿り、集計対象外の非公開メソッドは読み飛ばして（インライン化して）
     * 集計対象メソッドへの呼び出しを収集する。集計対象でも非公開でもない呼び出し先は
     * onOtherCallee に渡す（出力インタフェース判定などは呼び出し側で行う）。
     * 非公開メソッドは共有ガードで一度だけ展開する（展開済みメソッドの呼び出し先は
     * 初回展開時にコールバック済みのため、スキップしても収集結果は変わらない）。
     * @param {string[]} callMethods
     * @param {{targetMethodFqns: Set<string>, internalMethodMap: Map<string, UsecaseMethod>, onTargetCallee: function(string): void, onOtherCallee: function(string): void}} config
     */
    function collectInlinedCallees(callMethods, config) {
        const {targetMethodFqns, internalMethodMap, onTargetCallee, onOtherCallee} = config;
        traverseWithInlining(callMethods, {
            isTarget: fqn => targetMethodFqns.has(fqn),
            inlinableMethodOf: fqn => internalMethodMap.get(fqn),
            onTargetCallee,
            onOtherCallee
        }, sharedVisitedInliningGuard());
    }

    /**
     * クラス単位・パッケージ単位のグラフ構築で共通の骨格（ノード・エッジの集約と、
     * 出力インタフェース・ドメインモデル・入力インタフェースの収集）をまとめたビルダー。
     * ノード粒度（クラスFQN/メソッドFQN）による違いは各buildXxxGraphが指定する。
     * @param {DiagramContext} diagramContext
     */
    function createClassLevelGraphBuilder(diagramContext) {
        /** @type {DiagramNode[]} */
        const nodes = [];
        /** @type {DiagramEdge[]} */
        const edges = [];
        const edgeSet = new Set();
        const domainNodeSet = new Set();
        const outboundNodeSet = new Set();
        const inboundNodeSet = new Set();
        const domainFqnSet = Jig.data.domain.getDomainFqnSet();
        const {outboundOperationSet, inboundCallerIndex, showDiagramOutboundPorts, showDiagramDomainTypes, showDiagramInboundClasses}
            = resolveClassLevelDiagramContext(diagramContext);

        return {
            addNode(node) {
                nodes.push(node);
            },
            addEdge(from, to) {
                addEdgeOnce(edgeSet, edges, from, to);
            },
            /** 呼び出し先が出力インタフェースならoutbound-classノードとエッジを追加する */
            addOutboundEdgeIfVisible(fromFqn, calleeFqn) {
                if (!showDiagramOutboundPorts || !outboundOperationSet.has(calleeFqn)) return;
                const outboundClassFqn = getClassFqnFromMethodFqn(calleeFqn);
                addNodeOnce(outboundNodeSet, nodes, outboundClassFqn, "outbound-class");
                addEdgeOnce(edgeSet, edges, fromFqn, outboundClassFqn);
            },
            /** メソッドのパラメータ・戻り値からanchorFqnに紐づくドメイン型ノード・エッジを追加する */
            collectDomainEdges(method, anchorFqn) {
                if (!showDiagramDomainTypes) return;
                collectDomainTypeNodesAndEdges(method, anchorFqn, domainFqnSet, edgeSet, edges,
                    domainFqn => addNodeOnce(domainNodeSet, nodes, domainFqn, "domain-type"));
            },
            /**
             * inboundクラスからの呼び出しエッジを追加する。エッジの向き先は
             * resolveTargetで決め、nullを返した呼び出しはスキップする。
             * @param {Set<string>} classFqns
             * @param {function({classFqn: string, callerClassFqn: string, calleeMethodFqn: string}): string|null} resolveTarget
             */
            collectInboundEdges(classFqns, resolveTarget) {
                if (!showDiagramInboundClasses) return;
                classFqns.forEach(classFqn => {
                    (inboundCallerIndex.get(classFqn) || []).forEach(({callerClassFqn, calleeMethodFqn}) => {
                        const targetFqn = resolveTarget({classFqn, callerClassFqn, calleeMethodFqn});
                        if (!targetFqn) return;
                        addNodeOnce(inboundNodeSet, nodes, callerClassFqn, "inbound-class");
                        addEdgeOnce(edgeSet, edges, callerClassFqn, targetFqn);
                    });
                });
            },
            build() {
                return {nodes, edges};
            },
        };
    }

    /**
     * 公開メソッド（ユースケースとstaticメソッド）をノードとするメソッド単位のグラフを組み立てる。
     * buildClassGraph（1クラス）とbuildPackageMethodGraph（パッケージ全体）の共通実装。
     * 非公開メソッド経由の呼び出しはインライン化して集計対象メソッドへのエッジにする。
     * @param {Usecase[]} usecases
     * @param {DiagramContext} diagramContext
     * @param {{includeUsecaseMethod: function(UsecaseMethod): boolean, nodeOf: function(UsecaseMethod, Usecase, boolean): DiagramNode, skipInboundCallersInScope: boolean}} spec
     * @returns {{nodes: DiagramNode[], edges: DiagramEdge[]}}
     */
    function buildMethodLevelGraph(usecases, diagramContext, spec) {
        const builder = createClassLevelGraphBuilder(diagramContext);
        const classFqns = new Set(usecases.map(usecase => usecase.fqn));
        const methodFqns = new Set();
        const internalMethodMap = new Map();
        /** @type {{method: UsecaseMethod, usecase: Usecase, isStatic: boolean}[]} */
        const targets = [];
        usecases.forEach(usecase => {
            usecase.methods.forEach(method => {
                if (!isUsecase(method)) {
                    internalMethodMap.set(method.fqn, method);
                } else if (spec.includeUsecaseMethod(method)) {
                    methodFqns.add(method.fqn);
                    targets.push({method, usecase, isStatic: false});
                }
            });
            usecase.staticMethods.forEach(method => {
                methodFqns.add(method.fqn);
                targets.push({method, usecase, isStatic: true});
            });
        });

        targets.forEach(({method, usecase, isStatic}) => {
            builder.addNode(spec.nodeOf(method, usecase, isStatic));
            collectInlinedCallees(method.callMethods, {
                targetMethodFqns: methodFqns,
                internalMethodMap,
                onTargetCallee: calleeFqn => builder.addEdge(method.fqn, calleeFqn),
                onOtherCallee: calleeFqn => builder.addOutboundEdgeIfVisible(method.fqn, calleeFqn)
            });
            builder.collectDomainEdges(method, method.fqn);
        });

        builder.collectInboundEdges(classFqns, ({callerClassFqn, calleeMethodFqn}) => {
            if (spec.skipInboundCallersInScope && classFqns.has(callerClassFqn)) return null;
            return methodFqns.has(calleeMethodFqn) ? calleeMethodFqn : null;
        });

        return builder.build();
    }

    /**
     * @param {Usecase} usecase
     * @param {Set<string>|null} handlerFqns ハンドラのみ表示時のFQN集合、nullはすべて表示
     * @param {DiagramContext} [diagramContext]
     * @returns {{nodes: DiagramNode[], edges: DiagramEdge[]}}
     */
    function buildClassGraph(usecase, handlerFqns = null, diagramContext = {}) {
        return buildMethodLevelGraph([usecase], diagramContext, {
            includeUsecaseMethod: method => !handlerFqns || handlerFqns.has(method.fqn),
            nodeOf: (method, _usecase, isStatic) => ({
                fqn: method.fqn,
                kind: isUsecase(method) ? "usecase" : (isStatic ? "static-method" : "method")
            }),
            skipInboundCallersInScope: false
        });
    }

    /**
     * パッケージに含まれるクラスをユースケースと見立てたグラフを組み立てる。
     * クラス間の呼び出しは全メソッド由来、ドメインモデルの入出力は公開インタフェース（ユースケースとstaticメソッド）由来とする。
     * @param {Usecase[]} packageUsecases
     * @param {DiagramContext} [diagramContext]
     * @returns {{nodes: DiagramNode[], edges: DiagramEdge[]}}
     */
    function buildPackageGraph(packageUsecases, diagramContext = {}) {
        const builder = createClassLevelGraphBuilder(diagramContext);
        const classFqns = new Set(packageUsecases.map(usecase => usecase.fqn));

        packageUsecases.forEach(usecase => {
            builder.addNode({fqn: usecase.fqn, kind: "usecase"});

            const collectCallEdges = (method) => {
                (method.callMethods || []).forEach(calleeFqn => {
                    const calleeClassFqn = getClassFqnFromMethodFqn(calleeFqn);
                    if (calleeClassFqn === usecase.fqn) return;
                    if (classFqns.has(calleeClassFqn)) {
                        builder.addEdge(usecase.fqn, calleeClassFqn);
                    } else {
                        builder.addOutboundEdgeIfVisible(usecase.fqn, calleeFqn);
                    }
                });
            };

            usecase.methods.forEach(method => {
                collectCallEdges(method);
                if (isUsecase(method)) builder.collectDomainEdges(method, usecase.fqn);
            });
            usecase.staticMethods.forEach(method => {
                collectCallEdges(method);
                builder.collectDomainEdges(method, usecase.fqn);
            });
        });

        builder.collectInboundEdges(classFqns, ({classFqn, callerClassFqn}) =>
            classFqns.has(callerClassFqn) ? null : classFqn);

        return builder.build();
    }

    /**
     * パッケージに含まれるクラスの公開メソッド（ユースケースとstaticメソッド）をユースケースと見立てたグラフを組み立てる。
     * クラスはノードをまとめるための subgraph 情報（classFqn）としてのみ扱う。
     * @param {Usecase[]} packageUsecases
     * @param {DiagramContext} [diagramContext]
     * @returns {{nodes: DiagramNode[], edges: DiagramEdge[]}}
     */
    function buildPackageMethodGraph(packageUsecases, diagramContext = {}) {
        return buildMethodLevelGraph(packageUsecases, diagramContext, {
            includeUsecaseMethod: () => true,
            nodeOf: (method, usecase) => ({fqn: method.fqn, kind: "usecase", classFqn: usecase.fqn}),
            skipInboundCallersInScope: true
        });
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

            ensureUsecaseParticipant(rootMethod.fqn);
            visited.add(rootMethod.fqn);
            traverseCallGraph(rootMethod.fqn, rootMethod.callMethods, diagramContext, visited, {
                onVisibleCallee: (callerFqn, calleeFqn, m) => {
                    const caller = participants.get(callerFqn);
                    const callee = ensureUsecaseParticipant(calleeFqn);
                    const label = diagramContext.showDiagramArguments
                        ? buildArgumentsLabel(m.parameters, typeLabel)
                        : '';
                    calls.push({from: caller.id, to: callee.id, label});
                },
                onOutboundCallee: (callerFqn, calleeFqn) => {
                    const caller = participants.get(callerFqn);
                    const classFqn = getClassFqnFromMethodFqn(calleeFqn);
                    const methodName = Jig.glossary.methodSimpleName(calleeFqn);
                    const callee = ensureParticipant(classFqn, Jig.glossary.getTypeTerm(classFqn).title, "outbound");
                    const argumentsLabel = diagramContext.showDiagramArguments
                        ? buildArgumentsLabel(resolveCalleeParameters(calleeFqn, diagramContext), typeLabel)
                        : '';
                    const label = argumentsLabel ? `${methodName}(${argumentsLabel})` : methodName;
                    calls.push({from: caller.id, to: callee.id, label});
                }
            });

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

        // テキストフィルタ: クラス名/所属パッケージ名一致→全メソッド表示、メソッド名一致→該当メソッドのみ表示
        const filteredItems = usecases.flatMap(usecase => {
            const visibleMethods = visibleUsecaseMethodsOf(usecase, handlerFqns);
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
                if (addLinkedClassNode(builder, node, typeLabel)) return;
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
        // 破棄するDOMに紐づくダイアグラム登録を解除してから作り直す
        Jig.mermaid.diagram.unregisterWithin(container);
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
                if (!state.data) return; // 前回initでデータなしになっていた場合、古いリスナーが残っていても何もしない
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
