// ===== データ取得・変換 =====

/**
 * Java側（OutboundOutSummaryAdapter）が生成する出力ポートデータのスキーマ。
 * outboundPortOperation の fqn・label は必ず設定される（Java側保証）。
 * outboundAdapter・outboundAdapterExecution は対応する実装が見つからない場合 null になる。
 *
 * @typedef {{fqn: string, label: string, signature: string}} OutboundPortOperation
 * @typedef {{fqn: string, label: string}} OutboundAdapter
 * @typedef {{fqn: string, label: string}} OutboundAdapterExecution
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
            methodById.set(method.id, {...method, group: accessor.fqn, groupLabel: accessor.label});
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
            const left = a.outboundPortOperation.label;
            const right = b.outboundPortOperation.label;
            return left.localeCompare(right, "ja");
        });
        return {outboundPort: port, operations};
    }).filter(group => group.operations.length > 0)
      .sort((a, b) => {
        const left = a.outboundPort.label;
        const right = b.outboundPort.label;
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
            const left = a.outboundPort.label;
            const right = b.outboundPort.label;
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
                    if (!map.has(ext.fqn)) map.set(ext.fqn, {externalType: {fqn: ext.fqn, label: ext.label}, operations: []});
                    const group = map.get(ext.fqn);
                    if (!group.operations.includes(operation)) group.operations.push(operation);
                });
            });
        });
    });
    return Array.from(map.values())
        .sort((a, b) => a.externalType.label.localeCompare(b.externalType.label, "ja"));
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
        case 'INSERT': return visibility.crudCreate !== false;
        case 'SELECT': return visibility.crudRead !== false;
        case 'UPDATE': return visibility.crudUpdate !== false;
        case 'DELETE': return visibility.crudDelete !== false;
        default: return true;
    }
}

// ===== DOM ユーティリティ =====
const createElement = globalThis.Jig.dom.createElement;

function renderNoData(container) {
    container.appendChild(createElement("p", {
        className: "weak",
        textContent: "データなし"
    }));
}

function createSidebarSection(title, items) {
    return globalThis.Jig.sidebar.createSection(title, items);
}

function renderSidebarSection(container, title, items) {
    globalThis.Jig.sidebar.renderSection(container, title, items);
}

function fqnToId(prefix, fqn) {
    return prefix + "-" + fqn.replace(/[^a-zA-Z0-9]/g, '-');
}

function lazyRender(container, renderFn) {
    globalThis.Jig.observe.lazyRender(container, renderFn);
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
        const nodeLine = `${id}${shape.replace('$LABEL', label)}`;
        if (!this.nodes.includes(nodeLine)) {
            this.nodes.push(nodeLine);
        }
        return id;
    }

    addEdge(from, to, label = "") {
        const edgeKey = `${from}--${label}-->${to}`;
        if (!this.edgeSet.has(edgeKey)) {
            this.edgeSet.add(edgeKey);
            const edgeLine = label ? `  ${from} -- "${label}" --> ${to}` : `  ${from} --> ${to}`;
            this.edges.push(edgeLine);
        }
    }

    startSubgraph(label) {
        const id = `sg_${this.sanitize(label)}_${this.subgraphs.length}`;
        const subgraph = {id, label, lines: []};
        this.subgraphs.push(subgraph);
        return subgraph;
    }

    ensureSubgraph(map, key, label) {
        if (!map.has(key)) {
            map.set(key, this.startSubgraph(label));
        }
        return map.get(key);
    }

    addNodeToSubgraph(subgraph, id, label) {
        const nodeLine = `    ${id}["${label}"]`;
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

function renderMermaid(mermaidCode, container) {
    if (!mermaidCode) return;
    if (!container) return;
    if (!globalThis.Jig || !globalThis.Jig.mermaid || typeof globalThis.Jig.mermaid.renderWithControls !== "function") return;
    container.innerHTML = "";
    globalThis.Jig.mermaid.renderWithControls(container, mermaidCode);
}

function addPortNode(builder, portSubgraphs, portFqn, portLabel, portOpFqn, portOpName, visibility) {
    if (!visibility.port) return null;
    if (visibility.operation) {
        const portOpId = `PortOp_${builder.sanitize(portOpFqn)}`;
        builder.addNodeToSubgraph(
            builder.ensureSubgraph(portSubgraphs, portFqn, portLabel),
            portOpId, portOpName
        );
        return portOpId;
    } else {
        const portNodeId = `Port_${builder.sanitize(portFqn)}`;
        builder.addNode(portNodeId, portLabel);
        return portNodeId;
    }
}

function addAdapterNode(builder, sourceNodeId, adapterFqn, adapterLabel, executionFqn, executionName, visibility, adapterSubgraphs) {
    if (!visibility.adapter) return sourceNodeId;

    if (visibility.execution) {
        const sg = builder.ensureSubgraph(adapterSubgraphs, adapterFqn, adapterLabel);
        const executionId = `Exec_${builder.sanitize(executionFqn)}`;
        builder.addNodeToSubgraph(sg, executionId, executionName);
        if (sourceNodeId) builder.addEdge(sourceNodeId, executionId);
        return executionId;
    } else {
        const adapterNodeId = `Adapter_${builder.sanitize(adapterFqn)}`;
        builder.addNode(adapterNodeId, adapterLabel);
        if (sourceNodeId) builder.addEdge(sourceNodeId, adapterNodeId);
        return adapterNodeId;
    }
}

function addAccessorNode(builder, sourceNodeId, op, visibility, accessorSubgraphs, accessorNodes) {
    const groupId = op.group;
    const groupLabel = op.groupLabel;
    if (!visibility.accessor || !groupId) return sourceNodeId;

    if (visibility.accessorMethod) {
        const opNodeId = `POp_${builder.sanitize(op.id)}`;
        builder.addNodeToSubgraph(builder.ensureSubgraph(accessorSubgraphs, groupId, groupLabel), opNodeId, op.id.split('.').pop());
        if (sourceNodeId) builder.addEdge(sourceNodeId, opNodeId);
        return opNodeId;
    } else {
        const accessorNodeId = `Accessor_${builder.sanitize(groupId)}`;
        if (!accessorNodes.has(groupId)) {
            accessorNodes.set(groupId, accessorNodeId);
            builder.addNode(accessorNodeId, groupLabel);
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
            builder.addNode(persistenceTargetNodes.get(persistenceTarget), persistenceTarget, '[("$LABEL")]');
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
            builder.addNode(extTypeNodes.get(ext.fqn), ext.label, '(("$LABEL"))');
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

    if (visibility.externalAccessorMethod) {
        // 外部アクセッサをsubgraphにして各メソッドをノードに
        const sg = builder.ensureSubgraph(extAccessorSubgraphs, accessor.fqn, accessor.label);
        accessor.methods.forEach(accMethod => {
            const accMethodNodeId = `AccMethod_${builder.sanitize(accessor.fqn + '_' + accMethod.name)}`;
            builder.addNodeToSubgraph(sg, accMethodNodeId, accMethod.name);
            if (sourceNodeId) builder.addEdge(sourceNodeId, accMethodNodeId);
            accMethod.externals.forEach(ext => addExternal(accMethodNodeId, ext));
        });
        return null;
    } else {
        // クラス単位の単一ノード
        const nodeId = `ExtAcc_${builder.sanitize(accessor.fqn)}`;
        if (!extAccessorNodes.has(accessor.fqn)) {
            extAccessorNodes.set(accessor.fqn, nodeId);
            builder.addNode(nodeId, accessor.label);
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

function generatePortMermaidCode(group, visibility = DEFAULT_VISIBILITY) {
    const builder = new MermaidBuilder();
    const portFqn = group.outboundPort.fqn;
    const portLabel = group.outboundPort.label;

    const portSubgraphs = new Map();
    const adapterSubgraphs = new Map();
    const accessorSubgraphs = new Map();
    const accessorNodes = new Map();
    const persistenceTargetNodes = new Map();
    const extAccessorNodes = new Map();
    const extAccessorSubgraphs = new Map();
    const extTypeNodes = new Map();

    group.operations.forEach((operation) => {
        const portOpName = operation.outboundPortOperation.label;
        const portOpFqn = operation.outboundPortOperation.fqn;

        const adapterFqn = operation.outboundAdapter?.fqn;
        const adapterLabel = operation.outboundAdapter?.label;
        const executionName = operation.outboundAdapterExecution?.label;
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

function generateOperationMermaidCode(operation, visibility = DEFAULT_VISIBILITY) {
    return generatePortMermaidCode(
        {outboundPort: operation.outboundPort, operations: [operation]},
        visibility
    );
}

function extractOperationProps(operation) {
    return {
        portFqn:       operation.outboundPort.fqn,
        portLabel:     operation.outboundPort.label,
        portOpName:    operation.outboundPortOperation.label,
        portOpFqn:     operation.outboundPortOperation.fqn,
        adapterFqn:    operation.outboundAdapter?.fqn,
        adapterLabel:  operation.outboundAdapter?.label,
        executionName: operation.outboundAdapterExecution?.label,
        executionFqn:  operation.outboundAdapterExecution?.fqn,
    };
}

function generatePersistenceMermaidCode(group, visibility = DEFAULT_VISIBILITY) {
    const builder = new MermaidBuilder();
    const persistenceTarget = group.persistenceTarget;

    const portSubgraphs = new Map();
    const adapterSubgraphs = new Map();
    const accessorSubgraphs = new Map();
    const accessorNodes = new Map();
    const persistenceTargetNodes = new Map();

    group.operations.forEach((operation) => {
        const { portFqn, portLabel, portOpName, portOpFqn,
                adapterFqn, adapterLabel, executionName, executionFqn } = extractOperationProps(operation);

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

function generateExternalTypeMermaidCode(group, visibility = DEFAULT_VISIBILITY) {
    const builder = new MermaidBuilder();
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

        const { portFqn, portLabel, portOpName, portOpFqn,
                adapterFqn, adapterLabel, executionName, executionFqn } = extractOperationProps(operation);

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
    return createElement("tr", {
        className: "port-group-row",
        style: {cursor: "pointer"},
        children: [
            createElement("td", {
                className: "port-group-cell",
                children: [
                    document.createTextNode(group.outboundPort.label),
                    createElement("span", {
                        className: "weak",
                        style: {marginLeft: "8px"},
                        textContent: `(${group.operations.length})`
                    })
                ]
            }),
            ...allPersistenceTargets.map(persistenceTarget => {
                const cell = createElement("td", {className: "crud-cell port-crud-cell"});
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
    return createElement("tr", {
        className: `operation-row ${portId}`,
        style: {display: "none"},
        children: [
            createElement("td", {
                className: "operation-cell",
                textContent: operation.outboundPortOperation.label
            }),
            ...allPersistenceTargets.map(persistenceTarget => {
                const cell = createElement("td", {className: "crud-cell"});
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
    const portId = fqnToId("port", group.outboundPort.fqn);
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

    const headerRow = createElement("tr", {
        children: [
            createElement("th", {textContent: "出力ポート / 操作"}),
            ...allPersistenceTargets.map(persistenceTarget => createElement("th", {
                id: `crud-target-${persistenceTarget}`,
                textContent: persistenceTarget
            }))
        ]
    });

    const tbody = createElement("tbody");
    grouped.forEach(group => appendGroupToTable(tbody, group, allPersistenceTargets));

    const table = createElement("table", {
        className: "zebra crud-table",
        children: [
            createElement("thead", {children: [headerRow]}),
            tbody
        ]
    });

    container.appendChild(table);
}

// ===== コンテンツ描画 =====

function renderOutboundList(grouped, visibility = DEFAULT_VISIBILITY) {
    const container = document.getElementById("outbound-port-list");
    const sidebar = document.getElementById("outbound-sidebar-list");
    if (!container) return;
    container.innerHTML = "";
    if (sidebar) sidebar.innerHTML = "";

    grouped.forEach(group => {
        const portMermaidCode = generatePortMermaidCode(group, visibility);
        if (!portMermaidCode) return;
        const portFqnValue = group.outboundPort.fqn;
        const portId = fqnToId("port", portFqnValue);
        const portLabel = group.outboundPort.label;

        const cardChildren = [
            createElement("h3", {textContent: portLabel}),
            createElement("p", {
                className: "fully-qualified-name",
                textContent: portFqnValue
            })
        ];

        if (visibility.adapter) {
            const adapterLabels = Array.from(new Set(group.operations.map(operation => {
                const label = operation.outboundAdapter?.label ?? "";
                const fqn = operation.outboundAdapter?.fqn ?? "";
                return label + (label !== fqn ? ` (${fqn})` : "");
            })));
            if (adapterLabels.length > 0) {
                cardChildren.push(createElement("p", {
                    className: "weak",
                    textContent: "Implementation: " + adapterLabels.join(", ")
                }));
            }
        }

        cardChildren.push(createElement("p", {
            className: "weak",
            textContent: `${group.operations.length} operations`
        }));

        const portMermaidContainer = createElement("div", {className: "mermaid-diagram port-diagram"});
        lazyRender(portMermaidContainer, () => renderMermaid(portMermaidCode, portMermaidContainer));
        cardChildren.push(portMermaidContainer);

        const itemList = createElement("div", {className: "outbound-operation-list"});
        group.operations.forEach(operation => {
            const mermaidContainer = createElement("div", {className: "mermaid-diagram"});
            const operationWithPort = {...operation, outboundPort: group.outboundPort};
            const operationMermaidCode = generateOperationMermaidCode(operationWithPort, visibility);
            lazyRender(mermaidContainer, () => renderMermaid(operationMermaidCode, mermaidContainer));

            itemList.appendChild(createElement("article", {
                className: "outbound-operation-item jig-card jig-card--item",
                children: [
                    createElement("h4", {textContent: operation.outboundPortOperation.label}),
                    mermaidContainer,
                    createElement("p", {
                        className: "outbound-persistence-detail-title",
                        textContent: "永続化操作詳細"
                    }),
                    createElement("ul", {
                        className: "outbound-persistence-detail-list",
                        children: formatPersistenceAccessors(operation.persistenceAccessors).map(text => createElement("li", {textContent: text}))
                    })
                ]
            }));
        });
        const itemListDetails = createElement("details", {});
        const itemListSummary = createElement("summary", {
            className: "outbound-operation-list-summary",
            textContent: `操作一覧 (${group.operations.length}件)`
        });
        itemListDetails.appendChild(itemListSummary);
        itemListDetails.appendChild(itemList);
        cardChildren.push(itemListDetails);

        container.appendChild(createElement("section", {
            className: "outbound-group-card jig-card jig-card--type",
            id: portId,
            children: cardChildren
        }));
    });

    renderSidebarSection(sidebar, "出力ポート", grouped.map(group => {
        return {
            id: fqnToId("port", group.outboundPort.fqn),
            label: group.outboundPort.label
        };
    }));

    if (grouped.length === 0) {
        renderNoData(container);
    }
}

function renderPersistenceList(grouped, visibility = DEFAULT_VISIBILITY) {
    const container = document.getElementById("outbound-persistence-list");
    const sidebar = document.getElementById("persistence-sidebar-list");
    if (!container) return;
    container.innerHTML = "";
    if (sidebar) sidebar.innerHTML = "";

    grouped.forEach(group => {
        const persistenceMermaidCode = generatePersistenceMermaidCode(group, visibility);
        if (!persistenceMermaidCode) return;
        const targetId = fqnToId("persistence", group.persistenceTarget);

        const persistenceMermaidContainer = createElement("div", {className: "mermaid-diagram port-diagram"});
        lazyRender(persistenceMermaidContainer, () => renderMermaid(persistenceMermaidCode, persistenceMermaidContainer));

        container.appendChild(createElement("section", {
            className: "outbound-group-card jig-card jig-card--type",
            id: targetId,
            children: [
                createElement("h3", {textContent: group.persistenceTarget}),
                persistenceMermaidContainer
            ]
        }));
    });

    renderSidebarSection(sidebar, "永続化操作対象", grouped.map(group => ({
        id: fqnToId("persistence", group.persistenceTarget),
        label: group.persistenceTarget
    })));

    if (grouped.length === 0) {
        renderNoData(container);
    }
}

function renderExternalList(grouped, visibility = DEFAULT_VISIBILITY) {
    const container = document.getElementById("outbound-external-list");
    const sidebar = document.getElementById("external-sidebar-list");
    if (!container) return;
    container.innerHTML = "";
    if (sidebar) sidebar.innerHTML = "";

    grouped.forEach(group => {
        const externalMermaidCode = generateExternalTypeMermaidCode(group, visibility);
        if (!externalMermaidCode) return;
        const externalFqn = group.externalType.fqn;
        const externalId = fqnToId("external", externalFqn);

        const externalMermaidContainer = createElement("div", {className: "mermaid-diagram port-diagram"});
        lazyRender(externalMermaidContainer, () => renderMermaid(externalMermaidCode, externalMermaidContainer));

        container.appendChild(createElement("section", {
            className: "outbound-group-card jig-card jig-card--type",
            id: externalId,
            children: [
                createElement("h3", {textContent: group.externalType.label}),
                createElement("p", {className: "fully-qualified-name", textContent: externalFqn}),
                externalMermaidContainer
            ]
        }));
    });

    renderSidebarSection(sidebar, "外部型", grouped.map(group => ({
        id: fqnToId("external", group.externalType.fqn),
        label: group.externalType.label
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

const OutboundApp = {
    state: {
        visibility: {...DEFAULT_VISIBILITY},
        activeTab: 'outbound',
        data: null,
        grouped: null,
        persistenceGrouped: null,
        externalGrouped: null
    },

    init() {
        const data = getOutboundData();
        this.state.data = data;

        const grouped = groupOperationsByOutboundPort(data);
        this.state.grouped = grouped;

        const allOperations = grouped.flatMap(group =>
            group.operations.map(operation => ({...operation, outboundPort: group.outboundPort})));
        this.state.persistenceGrouped = groupOperationsByPersistenceTarget(allOperations);
        this.state.externalGrouped = groupOperationsByExternalType(allOperations);

        this.bindEvents();
        this.render();
    },

    setState(newState) {
        this.state = {...this.state, ...newState};
        this.renderTabs();
        if ('visibility' in newState) {
            this.renderPanels();
        }
    },

    bindEvents() {
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
                this.setState({visibility: readVisibility()});
            });
        });

        updateChildDisabled();

        document.querySelectorAll('input[name="diagram-direction"]').forEach(input => {
            input.addEventListener('change', () => {
                this.setState({visibility: readVisibility()});
            });
        });

        document.querySelectorAll('.outbound-tab-list .tab-button').forEach(button => {
            button.addEventListener('click', () => {
                const tabName = button.getAttribute('data-tab');
                this.setState({activeTab: tabName});
            });
        });
    },

    render() {
        this.renderTabs();
        this.renderPanels();
    },

    renderTabs() {
        const {activeTab} = this.state;
        document.querySelectorAll('.outbound-tab-list .tab-button').forEach(btn => {
            btn.classList.toggle('is-active', btn.getAttribute('data-tab') === activeTab);
        });
        document.querySelectorAll('.outbound-tab-panel').forEach(panel => {
            panel.classList.toggle('is-active', panel.id === `${activeTab}-tab-panel`);
        });
    },

    renderPanels() {
        const {visibility, data, grouped, persistenceGrouped, externalGrouped} = this.state;
        if (!data) return;
        renderPersistenceList(persistenceGrouped, visibility);
        renderExternalList(externalGrouped, visibility);
        renderOutboundList(grouped, visibility);
        renderCrudTable(grouped);
    }
};

if (typeof window !== "undefined" && typeof document !== "undefined") {
    window.addEventListener("DOMContentLoaded", () => {
        OutboundApp.init();
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        OutboundApp,
        getOutboundData: getOutboundData,
        groupOperationsByOutboundPort,
        groupOperationsByPersistenceTarget,
        groupOperationsByExternalType,
        formatPersistenceAccessors,
        renderOutboundList,
        renderPersistenceList,
        renderExternalList,
        renderCrudTable,
        toCrudChar,
        fqnToId,
        createElement,
        createSidebarSection,
        renderSidebarSection,
        renderNoData,
        generateOperationMermaidCode,
        generatePortMermaidCode,
        generatePersistenceMermaidCode,
        generateExternalTypeMermaidCode,
        addExternalAccessorNode,
        MermaidBuilder,
    };
}
