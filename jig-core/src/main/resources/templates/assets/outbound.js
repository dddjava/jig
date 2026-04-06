const OutboundApp = (() => {
    const Jig = globalThis.Jig;

    const state = {
        visibility: null, // Will be initialized from DEFAULT_VISIBILITY
        activeTab: 'outbound',
        data: null,
        grouped: null,
        persistenceGrouped: null,
        externalGrouped: null
    };

    const DEFAULT_VISIBILITY = {
        port: true, operation: true,
        adapter: true, execution: true,
        accessor: false, accessorMethod: false,
        target: true,
        externalAccessor: false, externalAccessorMethod: false,
        externalType: true, externalTypeMethod: true,
        direction: 'LR',
        crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true
    };

    // ===== データ取得・変換 =====

    /**
     * Java側（OutboundInterfaceAdapter）が生成する出力ポートデータのスキーマ。
     * outboundPortOperation の fqn は必ず設定される（Java側保証）。
     * outboundAdapter・outboundAdapterExecution は対応する実装が見つからない場合 null になる。
     */
    function getOutboundData() {
        return globalThis.outboundData || {
            outboundPorts: [],
            outboundAdapters: [],
            persistenceAccessors: [],
            otherExternalAccessors: [],
            targets: [],
            links: {operationToExecution: [], executionToPersistenceAccessor: [], executionToOtherExternalAccessor: []}
        };
    }

    function groupOperationsByOutboundPort(data) {
        // 結合に使うルックアップMapを事前構築
        // execution.fqn → {exec, adapter}
        const executionByFqn = new Map();
        data.outboundAdapters.forEach(adapter => {
            adapter.executions.forEach(exec => {
                executionByFqn.set(exec.fqn, {exec, adapter});
            });
        });

        // method.id → method（所属accessor情報付き）
        const methodById = new Map();
        data.persistenceAccessors.forEach(accessor => {
            accessor.methods.forEach(method => {
                methodById.set(method.id, {...method, group: accessor.fqn});
            });
        });

        // outboundPortOperation.fqn → execution.fqn
        const executionByOperation = new Map();
        data.links.operationToExecution.forEach(link => {
            executionByOperation.set(link.operation, link.execution);
        });

        // execution.fqn → [accessor.id]
        const accessorsByExecution = new Map();
        data.links.executionToPersistenceAccessor.forEach(link => {
            if (!accessorsByExecution.has(link.execution)) {
                accessorsByExecution.set(link.execution, []);
            }
            accessorsByExecution.get(link.execution).push(link.accessor);
        });

        // externalAccessor.fqn → externalAccessor
        const externalAccessorByFqn = new Map();
        data.otherExternalAccessors.forEach(a => externalAccessorByFqn.set(a.fqn, a));

        // execution.fqn → Map(accessorFqn → Set(methodName))
        const externalAccessorsByExecution = new Map();
        data.links.executionToOtherExternalAccessor.forEach(link => {
            if (!externalAccessorsByExecution.has(link.execution))
                externalAccessorsByExecution.set(link.execution, new Map());
            const accessorMethods = externalAccessorsByExecution.get(link.execution);
            if (!accessorMethods.has(link.accessor))
                accessorMethods.set(link.accessor, new Set());
            accessorMethods.get(link.accessor).add(link.method);
        });

        return data.outboundPorts.map(port => {
            const operations = port.operations.flatMap(op => {
                const execFqn = executionByOperation.get(op.fqn);
                if (!execFqn) return [];
                const execEntry = executionByFqn.get(execFqn);
                const accessorIds = accessorsByExecution.get(execFqn) || [];
                const persistenceAccessors = accessorIds.map(id => methodById.get(id)).filter(Boolean);
                const executionAccessorMethods = externalAccessorsByExecution.get(execFqn) || new Map();
                const externalAccessors = Array.from(executionAccessorMethods.entries()).flatMap(([fqn, methodNames]) => {
                    const accessor = externalAccessorByFqn.get(fqn);
                    if (!accessor) return [];
                    return [{...accessor, methods: accessor.methods.filter(m => methodNames.has(m.name))}];
                });
                return [{
                    outboundPortOperation: op,
                    outboundAdapter: execEntry?.adapter ?? null,
                    outboundAdapterExecution: execEntry?.exec ?? null,
                    persistenceAccessors,
                    externalAccessors
                }];
            }).sort((a, b) => {
                const left = Jig.glossary.getMethodTerm(a.outboundPortOperation.fqn).title;
                const right = Jig.glossary.getMethodTerm(b.outboundPortOperation.fqn).title;
                return left.localeCompare(right, "ja");
            });
            return {outboundPort: port, operations};
        }).filter(group => group.operations.length > 0)
            .sort((a, b) => {
                const left = Jig.glossary.getTypeTerm(a.outboundPort.fqn).title;
                const right = Jig.glossary.getTypeTerm(b.outboundPort.fqn).title;
                return left.localeCompare(right, "ja");
            });
    }

    function groupOperationsByPersistenceTarget(operations) {
        const map = new Map();
        operations.forEach(operation => {
            operation.persistenceAccessors.forEach(op => {
                Object.keys(op.targetOperationTypes).forEach(persistenceTarget => {
                    if (!map.has(persistenceTarget)) {
                        map.set(persistenceTarget, {
                            persistenceTarget: persistenceTarget,
                            operations: [],
                        });
                    }
                    const group = map.get(persistenceTarget);
                    if (!group.operations.includes(operation)) {
                        group.operations.push(operation);
                    }
                });
            });
        });
        return Array.from(map.values()).map(group => {
            group.operations.sort((a, b) => {
                const left = Jig.glossary.getTypeTerm(a.outboundPort.fqn).title;
                const right = Jig.glossary.getTypeTerm(b.outboundPort.fqn).title;
                return left.localeCompare(right, "ja");
            });
            return group;
        }).sort((a, b) => {
            return a.persistenceTarget.localeCompare(b.persistenceTarget, "ja");
        });
    }

    function groupOperationsByExternalType(operations) {
        const map = new Map();
        operations.forEach(operation => {
            operation.externalAccessors.forEach(accessor => {
                accessor.methods.forEach(accMethod => {
                    accMethod.externals.forEach(ext => {
                        if (!map.has(ext.fqn)) map.set(ext.fqn, {externalType: {fqn: ext.fqn}, operations: []});
                        const group = map.get(ext.fqn);
                        if (!group.operations.includes(operation)) group.operations.push(operation);
                    });
                });
            });
        });
        return Array.from(map.values())
            .sort((a, b) => Jig.glossary.getTypeTerm(a.externalType.fqn).title.localeCompare(Jig.glossary.getTypeTerm(b.externalType.fqn).title, "ja"));
    }

    function collectAllTargets(grouped) {
        const targetsSet = new Set();
        grouped.forEach(group => {
            group.operations.forEach(operation => {
                operation.persistenceAccessors.forEach(op => {
                    Object.keys(op.targetOperationTypes).forEach(persistenceTarget => targetsSet.add(persistenceTarget));
                });
            });
        });
        return Array.from(targetsSet).sort();
    }

    // ===== 変換ユーティリティ =====

    function formatPersistenceAccessors(persistenceAccessors) {
        if (!Array.isArray(persistenceAccessors) || persistenceAccessors.length === 0) {
            return ["なし"];
        }
        return persistenceAccessors
            .map(operation => {
                const operationTypes = Object.entries(operation.targetOperationTypes)
                    .map(([persistenceTarget, operationType]) => `${operationType}:${persistenceTarget}`)
                    .join(", ")
                return `${operation.id} [${operationTypes}]`.trim();
            });
    }

    function toCrudChar(operationType) {
        const type = (operationType || "").toUpperCase();
        if (type === "SELECT") return "R";
        if (type === "INSERT") return "C";
        if (type === "UPDATE") return "U";
        if (type === "DELETE") return "D";
        return "";
    }

    function isCrudVisible(operationType, visibility) {
        switch ((operationType || "").toUpperCase()) {
            case 'INSERT':
                return visibility.crudCreate !== false;
            case 'SELECT':
                return visibility.crudRead !== false;
            case 'UPDATE':
                return visibility.crudUpdate !== false;
            case 'DELETE':
                return visibility.crudDelete !== false;
            default:
                return true;
        }
    }

    // ===== DOM ユーティリティ =====

    function renderNoData(container) {
        container.appendChild(Jig.dom.createElement("p", {
            className: "weak",
            textContent: "データなし"
        }));
    }


    // ===== Mermaid ダイアグラム生成 =====

    function renderMermaid(diagramFn, container, options = {}) {
        if (!diagramFn) return;
        if (!container) return;
        container.innerHTML = "";
        Jig.mermaid.render.renderWithControls(container, diagramFn, options);
    }

    function addPortNode(builder, portSubgraphs, portFqn, portLabel, portOpFqn, portOpName, visibility) {
        if (!visibility.port) return null;
        if (visibility.operation) {
            const portOpId = Jig.util.fqnToId("portOp", portOpFqn);
            builder.addNodeToSubgraph(
                builder.ensureSubgraph(portSubgraphs, portFqn, portLabel),
                portOpId, portOpName, 'method'
            );
            builder.addClass(portOpId, "outbound");
            return portOpId;
        } else {
            const portNodeId = Jig.util.fqnToId("port", portFqn);
            builder.addNode(portNodeId, portLabel, 'class');
            builder.addClass(portNodeId, "outbound");
            return portNodeId;
        }
    }

    function addAdapterNode(builder, sourceNodeId, adapterFqn, adapterLabel, executionFqn, executionName, visibility, adapterSubgraphs) {
        if (!visibility.adapter) return sourceNodeId;

        if (visibility.execution) {
            const sg = builder.ensureSubgraph(adapterSubgraphs, adapterFqn, adapterLabel);
            const executionId = Jig.util.fqnToId("exec", executionFqn);
            builder.addNodeToSubgraph(sg, executionId, executionName, 'method');
            if (sourceNodeId) builder.addEdge(sourceNodeId, executionId);
            return executionId;
        } else {
            const adapterNodeId = Jig.util.fqnToId("adapter", adapterFqn);
            builder.addNode(adapterNodeId, adapterLabel, 'class');
            if (sourceNodeId) builder.addEdge(sourceNodeId, adapterNodeId);
            return adapterNodeId;
        }
    }

    function addAccessorNode(builder, sourceNodeId, op, visibility, accessorSubgraphs, accessorNodes) {
        const groupId = op.group;
        if (!visibility.accessor || !groupId) return sourceNodeId;

        const groupLabel = Jig.glossary.getTypeTerm(groupId).title;
        if (visibility.accessorMethod) {
            const opNodeId = Jig.util.fqnToId("op", op.id);
            builder.addNodeToSubgraph(builder.ensureSubgraph(accessorSubgraphs, groupId, groupLabel), opNodeId, op.id.split('.').pop(), 'method');
            if (sourceNodeId) builder.addEdge(sourceNodeId, opNodeId);
            return opNodeId;
        } else {
            const accessorNodeId = Jig.util.fqnToId("accessor", groupId);
            if (!accessorNodes.has(groupId)) {
                accessorNodes.set(groupId, accessorNodeId);
                builder.addNode(accessorNodeId, groupLabel, 'class');
            }
            if (sourceNodeId) builder.addEdge(sourceNodeId, accessorNodes.get(groupId));
            return accessorNodes.get(groupId);
        }
    }

    function addPersistenceTargetEdges(builder, sourceNodeId, op, persistenceTargetNodes, visibility) {
        Object.entries(op.targetOperationTypes).forEach(([persistenceTarget, operationType]) => {
            if (!isCrudVisible(operationType, visibility)) return;
            if (!persistenceTargetNodes.has(persistenceTarget)) {
                persistenceTargetNodes.set(persistenceTarget, `Target_${persistenceTargetNodes.size}`);
                builder.addNode(persistenceTargetNodes.get(persistenceTarget), persistenceTarget, 'database');
            }
            const edgeLabel = visibility.externalTypeMethod ? operationType : undefined;
            if (sourceNodeId) builder.addEdge(sourceNodeId, persistenceTargetNodes.get(persistenceTarget), edgeLabel);
        });
    }

    function addExternalAccessorNode(builder, sourceNodeId, accessor, visibility, extAccessorNodes, extAccessorSubgraphs, extTypeNodes) {
        // 外部型ノードの追加ヘルパー（externalTypeMethod でエッジラベルにメソッド名を付与）
        const addExternal = (fromNodeId, ext) => {
            if (!visibility.externalType) return;
            if (!extTypeNodes.has(ext.fqn)) {
                extTypeNodes.set(ext.fqn, `ExtType_${extTypeNodes.size}`);
                builder.addNode(extTypeNodes.get(ext.fqn), Jig.glossary.getTypeTerm(ext.fqn).title, 'external');
            }
            const edgeLabel = visibility.externalTypeMethod ? ext.method : undefined;
            if (fromNodeId) builder.addEdge(fromNodeId, extTypeNodes.get(ext.fqn), edgeLabel);
        };

        if (!visibility.externalAccessor) {
            // アクセッサ非表示時は外部型をアダプターから直接接続
            if (visibility.externalType) {
                const uniqueExternals = new Map();
                accessor.methods.forEach(accMethod => {
                    accMethod.externals.forEach(ext => uniqueExternals.set(ext.fqn, ext));
                });
                uniqueExternals.forEach(ext => addExternal(sourceNodeId, ext));
            }
            return sourceNodeId;
        }

        const accessorLabel = Jig.glossary.getTypeTerm(accessor.fqn).title;
        if (visibility.externalAccessorMethod) {
            // 外部アクセッサをsubgraphにして各メソッドをノードに
            const sg = builder.ensureSubgraph(extAccessorSubgraphs, accessor.fqn, accessorLabel);
            accessor.methods.forEach(accMethod => {
                const accMethodNodeId = Jig.util.fqnToId("accMethod", accessor.fqn + '#' + accMethod.name);
                builder.addNodeToSubgraph(sg, accMethodNodeId, accMethod.name, 'method');
                if (sourceNodeId) builder.addEdge(sourceNodeId, accMethodNodeId);
                accMethod.externals.forEach(ext => addExternal(accMethodNodeId, ext));
            });
            return null;
        } else {
            // クラス単位の単一ノード
            const nodeId = Jig.util.fqnToId("extAcc", accessor.fqn);
            if (!extAccessorNodes.has(accessor.fqn)) {
                extAccessorNodes.set(accessor.fqn, nodeId);
                builder.addNode(nodeId, accessorLabel, 'class');
            }
            if (sourceNodeId) builder.addEdge(sourceNodeId, extAccessorNodes.get(accessor.fqn));

            if (visibility.externalType) {
                if (visibility.externalTypeMethod) {
                    // メソッドごとのエッジ（メソッド名をラベルとして）
                    accessor.methods.forEach(accMethod => {
                        accMethod.externals.forEach(ext => addExternal(extAccessorNodes.get(accessor.fqn), ext));
                    });
                } else {
                    // 外部型を単一ノードで表示（重複排除）
                    const uniqueExternals = new Map();
                    accessor.methods.forEach(accMethod => {
                        accMethod.externals.forEach(ext => uniqueExternals.set(ext.fqn, ext));
                    });
                    uniqueExternals.forEach(ext => addExternal(extAccessorNodes.get(accessor.fqn), ext));
                }
            }
            return extAccessorNodes.get(accessor.fqn);
        }
    }

    function generatePortMermaidCode(group, visibility = state.visibility || DEFAULT_VISIBILITY) {
        const builder = new Jig.mermaid.Builder();
        builder.applyThemeClassDefs();
        const portFqn = group.outboundPort.fqn;
        const portLabel = Jig.glossary.getTypeTerm(portFqn).title;

        const portSubgraphs = new Map();
        const adapterSubgraphs = new Map();
        const accessorSubgraphs = new Map();
        const accessorNodes = new Map();
        const persistenceTargetNodes = new Map();
        const extAccessorNodes = new Map();
        const extAccessorSubgraphs = new Map();
        const extTypeNodes = new Map();

        group.operations.forEach((operation) => {
            const portOpName = Jig.glossary.getMethodTerm(operation.outboundPortOperation.fqn).title;
            const portOpFqn = operation.outboundPortOperation.fqn;

            const adapterFqn = operation.outboundAdapter?.fqn;
            const adapterLabel = Jig.glossary.getTypeTerm(operation.outboundAdapter?.fqn).title;
            const executionName = Jig.glossary.getMethodTerm(operation.outboundAdapterExecution?.fqn).title;
            const executionFqn = operation.outboundAdapterExecution?.fqn;

            let lastNodeId = addPortNode(builder, portSubgraphs, portFqn, portLabel, portOpFqn, portOpName, visibility);

            lastNodeId = addAdapterNode(builder, lastNodeId, adapterFqn, adapterLabel, executionFqn, executionName, visibility, adapterSubgraphs);

            operation.persistenceAccessors.forEach(op => {
                const currentNode = addAccessorNode(builder, lastNodeId, op, visibility, accessorSubgraphs, accessorNodes);

                if (visibility.target) {
                    addPersistenceTargetEdges(builder, currentNode, op, persistenceTargetNodes, visibility);
                }
            });

            operation.externalAccessors.forEach(accessor => {
                addExternalAccessorNode(builder, lastNodeId, accessor, visibility, extAccessorNodes, extAccessorSubgraphs, extTypeNodes);
            });
        });

        if (builder.isEmpty()) return null;
        return builder.build(visibility.direction);
    }

    function generateOperationMermaidCode(operation, visibility = state.visibility || DEFAULT_VISIBILITY) {
        return generatePortMermaidCode(
            {outboundPort: operation.outboundPort, operations: [operation]},
            visibility
        );
    }

    function extractOperationProps(operation) {
        return {
            portFqn: operation.outboundPort.fqn,
            portLabel: Jig.glossary.getTypeTerm(operation.outboundPort.fqn).title,
            portOpName: Jig.glossary.getMethodTerm(operation.outboundPortOperation.fqn).title,
            portOpFqn: operation.outboundPortOperation.fqn,
            adapterFqn: operation.outboundAdapter?.fqn,
            adapterLabel: Jig.glossary.getTypeTerm(operation.outboundAdapter?.fqn).title,
            executionName: Jig.glossary.getMethodTerm(operation.outboundAdapterExecution?.fqn).title,
            executionFqn: operation.outboundAdapterExecution?.fqn,
        };
    }

    function generatePersistenceMermaidCode(group, visibility = state.visibility || DEFAULT_VISIBILITY) {
        const builder = new Jig.mermaid.Builder();
        builder.applyThemeClassDefs();
        const persistenceTarget = group.persistenceTarget;

        const portSubgraphs = new Map();
        const adapterSubgraphs = new Map();
        const accessorSubgraphs = new Map();
        const accessorNodes = new Map();
        const persistenceTargetNodes = new Map();

        group.operations.forEach((operation) => {
            const {
                portFqn, portLabel, portOpName, portOpFqn,
                adapterFqn, adapterLabel, executionName, executionFqn
            } = extractOperationProps(operation);

            operation.persistenceAccessors
                .filter(op => persistenceTarget in op.targetOperationTypes)
                .filter(op => isCrudVisible(op.targetOperationTypes[persistenceTarget], visibility))
                .forEach(op => {
                    let currentNode = addPortNode(builder, portSubgraphs, portFqn, portLabel, portOpFqn, portOpName, visibility);

                    currentNode = addAdapterNode(builder, currentNode, adapterFqn, adapterLabel, executionFqn, executionName, visibility, adapterSubgraphs);

                    currentNode = addAccessorNode(builder, currentNode, op, visibility, accessorSubgraphs, accessorNodes);

                    if (visibility.target) {
                        addPersistenceTargetEdges(builder, currentNode, {
                            targetOperationTypes: {[persistenceTarget]: op.targetOperationTypes[persistenceTarget]}
                        }, persistenceTargetNodes, visibility);
                    }
                });
        });

        if (builder.isEmpty()) return null;
        return builder.build(visibility.direction);
    }

    function generateExternalTypeMermaidCode(group, visibility = state.visibility || DEFAULT_VISIBILITY) {
        const builder = new Jig.mermaid.Builder();
        builder.applyThemeClassDefs();
        const externalType = group.externalType;

        const portSubgraphs = new Map();
        const adapterSubgraphs = new Map();
        const extAccessorNodes = new Map();
        const extAccessorSubgraphs = new Map();
        const extTypeNodes = new Map();

        group.operations.forEach(operation => {
            const relevantAccessors = operation.externalAccessors.filter(accessor =>
                accessor.methods.some(accMethod =>
                    accMethod.externals.some(ext => ext.fqn === externalType.fqn)));

            const {
                portFqn, portLabel, portOpName, portOpFqn,
                adapterFqn, adapterLabel, executionName, executionFqn
            } = extractOperationProps(operation);

            relevantAccessors.forEach(accessor => {
                let currentNode = addPortNode(builder, portSubgraphs, portFqn, portLabel, portOpFqn, portOpName, visibility);

                currentNode = addAdapterNode(builder, currentNode, adapterFqn, adapterLabel, executionFqn, executionName, visibility, adapterSubgraphs);

                // このカードの外部型のみに絞ったアクセッサを渡す
                const filteredAccessor = {
                    ...accessor,
                    methods: accessor.methods
                        .map(m => ({...m, externals: m.externals.filter(ext => ext.fqn === externalType.fqn)}))
                        .filter(m => m.externals.length > 0)
                };
                addExternalAccessorNode(builder, currentNode, filteredAccessor, visibility, extAccessorNodes, extAccessorSubgraphs, extTypeNodes);
            });
        });

        if (builder.isEmpty()) return null;
        return builder.build(visibility.direction);
    }

    // ===== CRUD テーブル描画 =====

    function createPortGroupRow(group, allPersistenceTargets) {
        return Jig.dom.createElement("tr", {
            className: "port-group-row",
            style: {cursor: "pointer"},
            children: [
                Jig.dom.createElement("td", {
                    className: "port-group-cell",
                    children: [
                        document.createTextNode(Jig.glossary.getTypeTerm(group.outboundPort.fqn).title),
                        Jig.dom.createElement("span", {
                            className: "weak",
                            style: {marginLeft: "8px"},
                            textContent: `(${group.operations.length})`
                        })
                    ]
                }),
                ...allPersistenceTargets.map(persistenceTarget => {
                    const cell = Jig.dom.createElement("td", {className: "crud-cell port-crud-cell"});
                    const cruds = new Set();
                    group.operations.forEach(operation => {
                        operation.persistenceAccessors.forEach(op => {
                            if (persistenceTarget in op.targetOperationTypes) {
                                const operationType = op.targetOperationTypes[persistenceTarget];
                                const crud = toCrudChar(operationType);
                                if (crud) {
                                    cruds.add(crud);
                                }
                            }
                        });
                    });
                    if (cruds.size > 0) {
                        cell.textContent = Array.from(cruds).sort().join("");
                    }
                    return cell;
                })
            ]
        });
    }

    function createOperationRow(operation, allPersistenceTargets, portId) {
        return Jig.dom.createElement("tr", {
            className: `operation-row ${portId}`,
            style: {display: "none"},
            children: [
                Jig.dom.createElement("td", {
                    className: "operation-cell",
                    textContent: Jig.glossary.getMethodTerm(operation.outboundPortOperation.fqn).title
                }),
                ...allPersistenceTargets.map(persistenceTarget => {
                    const cell = Jig.dom.createElement("td", {className: "crud-cell"});
                    const cruds = new Set();
                    operation.persistenceAccessors.forEach(op => {
                        if (persistenceTarget in op.targetOperationTypes) {
                            const operationType = op.targetOperationTypes[persistenceTarget];
                            const crud = toCrudChar(operationType);
                            if (crud) {
                                cruds.add(crud);
                            }
                        }
                    });
                    if (cruds.size > 0) {
                        cell.textContent = Array.from(cruds).sort().join("");
                    }
                    return cell;
                })
            ]
        });
    }

    function appendGroupToTable(tbody, group, allPersistenceTargets) {
        const portId = Jig.util.fqnToId("port", group.outboundPort.fqn);
        const portRow = createPortGroupRow(group, allPersistenceTargets);
        tbody.appendChild(portRow);

        const opRows = group.operations.map(operation => {
            const row = createOperationRow(operation, allPersistenceTargets, portId);
            tbody.appendChild(row);
            return row;
        });

        portRow.addEventListener("click", () => {
            const isHidden = opRows[0].style.display === "none";
            opRows.forEach(row => {
                row.style.display = isHidden ? "table-row" : "none";
            });
            portRow.classList.toggle("is-expanded", isHidden);
        });
    }

    function renderCrudTable(grouped) {
        const container = document.getElementById("outbound-crud-panel");
        if (!container) return;

        container.innerHTML = "";

        const allPersistenceTargets = collectAllTargets(grouped);

        if (allPersistenceTargets.length === 0) {
            container.textContent = "永続化操作なし";
            return;
        }

        const headerRow = Jig.dom.createElement("tr", {
            children: [
                Jig.dom.createElement("th", {textContent: "出力ポート / 操作"}),
                ...allPersistenceTargets.map(persistenceTarget => Jig.dom.createElement("th", {
                    id: `crud-target-${persistenceTarget}`,
                    textContent: persistenceTarget
                }))
            ]
        });

        const tbody = Jig.dom.createElement("tbody");
        grouped.forEach(group => appendGroupToTable(tbody, group, allPersistenceTargets));

        const table = Jig.dom.createElement("table", {
            className: "zebra crud-table",
            children: [
                Jig.dom.createElement("thead", {children: [headerRow]}),
                tbody
            ]
        });

        container.appendChild(table);
    }

    // ===== コンテンツ描画 =====

    function renderOutboundList(grouped, visibility = state.visibility || DEFAULT_VISIBILITY) {
        const container = document.getElementById("outbound-port-list");
        const sidebar = document.getElementById("outbound-sidebar-list");
        if (!container) return;
        container.innerHTML = "";
        if (sidebar) sidebar.innerHTML = "";

        grouped.forEach(group => {
            const portMermaidCode = generatePortMermaidCode(group, visibility);
            if (!portMermaidCode) return;
            const portFqnValue = group.outboundPort.fqn;
            const portId = Jig.util.fqnToId("port", portFqnValue);
            const portLabel = Jig.glossary.getTypeTerm(portFqnValue).title;

            const cardChildren = [
                Jig.dom.createElement("h3", {textContent: portLabel}),
                Jig.dom.createElement("p", {
                    className: "fully-qualified-name",
                    textContent: portFqnValue
                })
            ];

            if (visibility.adapter) {
                const adapterLabels = Array.from(new Set(group.operations.map(operation => {
                    const fqn = operation.outboundAdapter?.fqn ?? "";
                    const label = Jig.glossary.getTypeTerm(fqn).title;
                    return label + (label !== fqn ? ` (${fqn})` : "");
                })));
                if (adapterLabels.length > 0) {
                    cardChildren.push(Jig.dom.createElement("p", {
                        className: "weak",
                        textContent: "Implementation: " + adapterLabels.join(", ")
                    }));
                }
            }

            cardChildren.push(Jig.dom.createElement("p", {
                className: "weak",
                textContent: `${group.operations.length} operations`
            }));

            Jig.mermaid.diagram.createAndRegister(cardChildren, (container) => {
                const currentVisibility = readVisibility();
                const generator = (dir) => generatePortMermaidCode(group, {...currentVisibility, direction: dir});
                if (generator(currentVisibility.direction)) {
                    renderMermaid(generator, container, {direction: currentVisibility.direction});
                }
            }, {className: "mermaid-diagram port-diagram"});

            const itemList = Jig.dom.createElement("div", {className: "outbound-operation-list"});
            group.operations.forEach(operation => {
                const mermaidContainer = Jig.dom.createElement("div", {className: "mermaid-diagram"});
                const operationWithPort = {...operation, outboundPort: group.outboundPort};
                Jig.mermaid.diagram.register(mermaidContainer, () => {
                    const currentVisibility = readVisibility();
                    const generator = (dir) => generateOperationMermaidCode(operationWithPort, {
                        ...currentVisibility,
                        direction: dir
                    });
                    if (generator(currentVisibility.direction)) {
                        renderMermaid(generator, mermaidContainer, {direction: currentVisibility.direction});
                    }
                });

                itemList.appendChild(Jig.dom.createElement("article", {
                    className: "outbound-operation-item jig-card jig-card--item",
                    children: [
                        Jig.dom.createElement("h4", {textContent: Jig.glossary.getMethodTerm(operation.outboundPortOperation.fqn).title}),
                        mermaidContainer,
                        Jig.dom.createElement("p", {
                            className: "outbound-persistence-detail-title",
                            textContent: "永続化操作"
                        }),
                        Jig.dom.createElement("ul", {
                            className: "outbound-persistence-detail-list",
                            children: formatPersistenceAccessors(operation.persistenceAccessors).map(text => Jig.dom.createElement("li", {textContent: text}))
                        })
                    ]
                }));
            });
            const itemListDetails = Jig.dom.createElement("details", {});
            const itemListSummary = Jig.dom.createElement("summary", {
                className: "outbound-operation-list-summary",
                textContent: `操作別詳細 (${group.operations.length}件)`
            });
            itemListDetails.appendChild(itemListSummary);
            itemListDetails.appendChild(itemList);
            cardChildren.push(itemListDetails);

            container.appendChild(Jig.dom.createElement("section", {
                className: "outbound-group-card jig-card jig-card--type",
                id: portId,
                children: cardChildren
            }));
        });

        Jig.dom.sidebar.renderSection(sidebar, "出力ポート", grouped.map(group => {
            return {
                id: Jig.util.fqnToId("port", group.outboundPort.fqn),
                label: Jig.glossary.getTypeTerm(group.outboundPort.fqn).title
            };
        }));

        if (grouped.length === 0) {
            renderNoData(container);
        }
    }

    function renderPersistenceList(grouped, visibility = state.visibility || DEFAULT_VISIBILITY) {
        const container = document.getElementById("outbound-persistence-list");
        const sidebar = document.getElementById("persistence-sidebar-list");
        if (!container) return;
        container.innerHTML = "";
        if (sidebar) sidebar.innerHTML = "";

        grouped.forEach(group => {
            const persistenceMermaidCode = generatePersistenceMermaidCode(group, visibility);
            if (!persistenceMermaidCode) return;
            const targetId = Jig.util.fqnToId("persistence", group.persistenceTarget);

            const persistenceMermaidContainer = Jig.dom.createElement("div", {className: "mermaid-diagram port-diagram"});
            Jig.mermaid.diagram.register(persistenceMermaidContainer, () => {
                const currentVisibility = readVisibility();
                const generator = (dir) => generatePersistenceMermaidCode(group, {...currentVisibility, direction: dir});
                if (generator(currentVisibility.direction)) {
                    renderMermaid(generator, persistenceMermaidContainer, {direction: currentVisibility.direction});
                }
            });

            container.appendChild(Jig.dom.createElement("section", {
                className: "outbound-group-card jig-card jig-card--type",
                id: targetId,
                children: [
                    Jig.dom.createElement("h3", {textContent: group.persistenceTarget}),
                    persistenceMermaidContainer
                ]
            }));
        });

        Jig.dom.sidebar.renderSection(sidebar, "永続化操作対象", grouped.map(group => ({
            id: Jig.util.fqnToId("persistence", group.persistenceTarget),
            label: group.persistenceTarget
        })));

        if (grouped.length === 0) {
            renderNoData(container);
        }
    }

    function renderExternalList(grouped, visibility = state.visibility || DEFAULT_VISIBILITY) {
        const container = document.getElementById("outbound-external-list");
        const sidebar = document.getElementById("external-sidebar-list");
        if (!container) return;
        container.innerHTML = "";
        if (sidebar) sidebar.innerHTML = "";

        grouped.forEach(group => {
            const externalMermaidCode = generateExternalTypeMermaidCode(group, visibility);
            if (!externalMermaidCode) return;
            const externalFqn = group.externalType.fqn;
            const externalId = Jig.util.fqnToId("external", externalFqn);
            const externalLabel = Jig.glossary.getTypeTerm(externalFqn).title;

            const externalMermaidContainer = Jig.dom.createElement("div", {className: "mermaid-diagram port-diagram"});
            Jig.mermaid.diagram.register(externalMermaidContainer, () => {
                const currentVisibility = readVisibility();
                const generator = (dir) => generateExternalTypeMermaidCode(group, {...currentVisibility, direction: dir});
                if (generator(currentVisibility.direction)) {
                    renderMermaid(generator, externalMermaidContainer, {direction: currentVisibility.direction});
                }
            });

            container.appendChild(Jig.dom.createElement("section", {
                className: "outbound-group-card jig-card jig-card--type",
                id: externalId,
                children: [
                    Jig.dom.createElement("h3", {textContent: externalLabel}),
                    Jig.dom.createElement("p", {className: "fully-qualified-name", textContent: externalFqn}),
                    externalMermaidContainer
                ]
            }));
        });

        Jig.dom.sidebar.renderSection(sidebar, "外部型", grouped.map(group => ({
            id: Jig.util.fqnToId("external", group.externalType.fqn),
            label: Jig.glossary.getTypeTerm(group.externalType.fqn).title
        })));

        if (grouped.length === 0) {
            renderNoData(container);
        }
    }

    // ===== 設定・アプリケーション本体 =====

    function readVisibility() {
        const checked = (name) => {
            const el = document.querySelector(`input[name="${name}"]`);
            return el ? el.checked : false;
        };
        const port = checked("show-port");
        const adapter = checked("show-adapter");
        const accessor = checked("show-accessor");
        const directionEl = document.querySelector('input[name="diagram-direction"]:checked');
        const direction = directionEl ? directionEl.value : 'LR';
        return {
            port,
            operation: checked("show-operation"),
            adapter,
            execution: checked("show-execution"),
            // 永続化アクセッサの表示制御
            accessor,
            accessorMethod: checked("show-accessor-method"),
            target: checked("show-target"),
            // 外部アクセッサの表示制御（UIでは永続化アクセッサと同一チェックボックスで制御）
            externalAccessor: checked("show-accessor"),
            externalAccessorMethod: checked("show-accessor-method"),
            externalType: checked("show-target"),
            externalTypeMethod: checked("show-external-type-method"),
            direction,
            crudCreate: checked("show-crud-c"),
            crudRead: checked("show-crud-r"),
            crudUpdate: checked("show-crud-u"),
            crudDelete: checked("show-crud-d"),
        };
    }

    function setState(newState) {
        state.visibility = {...(state.visibility || DEFAULT_VISIBILITY), ...(newState.visibility || {})};
        if ('activeTab' in newState) state.activeTab = newState.activeTab;
        if ('data' in newState) state.data = newState.data;
        if ('grouped' in newState) state.grouped = newState.grouped;
        if ('persistenceGrouped' in newState) state.persistenceGrouped = newState.persistenceGrouped;
        if ('externalGrouped' in newState) state.externalGrouped = newState.externalGrouped;

        renderTabs();
        if ('visibility' in newState) {
            renderPanels();
            Jig.mermaid.diagram.rerenderVisible();
        }
    }

    function bindEvents() {
        const childRules = {
            "show-port": "show-operation",
            "show-adapter": "show-execution",
            "show-accessor": "show-accessor-method",
            "show-target": "show-external-type-method",
        };

        const updateChildDisabled = () => {
            Object.entries(childRules).forEach(([parentName, childName]) => {
                const parentEl = document.querySelector(`input[name="${parentName}"]`);
                const childEl = document.querySelector(`input[name="${childName}"]`);
                if (parentEl && childEl) {
                    childEl.disabled = !parentEl.checked;
                }
            });
        };

        document.querySelectorAll('input[name^="show-"]').forEach(input => {
            input.addEventListener('change', () => {
                updateChildDisabled();
                setState({visibility: readVisibility()});
            });
        });

        updateChildDisabled();

        document.querySelectorAll('input[name="diagram-direction"]').forEach(input => {
            input.addEventListener('change', () => {
                setState({visibility: readVisibility()});
            });
        });

        document.querySelectorAll('.outbound-tab-list .tab-button').forEach(button => {
            button.addEventListener('click', () => {
                const tabName = button.getAttribute('data-tab');
                setState({activeTab: tabName});
            });
        });
    }

    function renderTabs() {
        const {activeTab} = state;
        document.querySelectorAll('.outbound-tab-list .tab-button').forEach(btn => {
            btn.classList.toggle('is-active', btn.getAttribute('data-tab') === activeTab);
        });
        document.querySelectorAll('.outbound-tab-panel').forEach(panel => {
            panel.classList.toggle('is-active', panel.id === `${activeTab}-tab-panel`);
        });
    }

    function renderPanels() {
        const {visibility, data, grouped, persistenceGrouped, externalGrouped} = state;
        if (!data) return;
        renderPersistenceList(persistenceGrouped, visibility);
        renderExternalList(externalGrouped, visibility);
        renderOutboundList(grouped, visibility);
        renderCrudTable(grouped);
    }

    function render() {
        renderTabs();
        renderPanels();
    }

    function init() {
        if (typeof document === "undefined") return;
        if (!document.body.classList.contains("outbound-interface")) return;

        state.visibility = {...DEFAULT_VISIBILITY};
        const data = getOutboundData();
        state.data = data;

        const grouped = groupOperationsByOutboundPort(data);
        state.grouped = grouped;

        const allOperations = grouped.flatMap(group =>
            group.operations.map(operation => ({...operation, outboundPort: group.outboundPort})));
        state.persistenceGrouped = groupOperationsByPersistenceTarget(allOperations);
        state.externalGrouped = groupOperationsByExternalType(allOperations);

        bindEvents();
        render();
    }

    return {
        init,
        state,
        getOutboundData,
        groupOperationsByOutboundPort,
        groupOperationsByPersistenceTarget,
        groupOperationsByExternalType,
        formatPersistenceAccessors,
        renderOutboundList,
        renderPersistenceList,
        renderExternalList,
        renderCrudTable,
        toCrudChar,
        renderNoData,
        generateOperationMermaidCode,
        generatePortMermaidCode,
        generatePersistenceMermaidCode,
        generateExternalTypeMermaidCode,
        addExternalAccessorNode,
    };
})();

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", () => {
        OutboundApp.init();
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = OutboundApp;
}
