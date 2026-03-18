// ===== データ取得・変換 =====

/**
 * Java側（OutputsSummaryAdapter）が生成する出力ポートデータのスキーマ。
 * outputPortOperation の fqn・label は必ず設定される（Java側保証）。
 * outputAdapter・outputAdapterExecution は対応する実装が見つからない場合 null になる。
 *
 * @typedef {{fqn: string, label: string, signature: string}} OutputPortOperation
 * @typedef {{fqn: string, label: string}} OutputAdapter
 * @typedef {{fqn: string, label: string}} OutputAdapterExecution
 */
function getOutputsData() {
    return globalThis.outputPortData || {
        outputPorts: [],
        outputAdapters: [],
        persistenceAccessors: [],
        externalAccessors: [],
        targets: [],
        links: {operationToExecution: [], executionToAccessor: [], executionToExternalAccessor: []}
    };
}

function groupOperationsByOutputPort(data) {
    // 結合に使うルックアップMapを事前構築
    // execution.fqn → {exec, adapter}
    const executionByFqn = new Map();
    (data.outputAdapters || []).forEach(adapter => {
        (adapter.executions || []).forEach(exec => {
            executionByFqn.set(exec.fqn, {exec, adapter});
        });
    });

    // method.id → method（所属accessor情報付き）
    const methodById = new Map();
    (data.persistenceAccessors || []).forEach(accessor => {
        (accessor.methods || []).forEach(method => {
            methodById.set(method.id, {...method, group: accessor.fqn, groupLabel: accessor.label});
        });
    });

    // outputPortOperation.fqn → execution.fqn
    const executionByOperation = new Map();
    (data.links?.operationToExecution || []).forEach(link => {
        executionByOperation.set(link.operation, link.execution);
    });

    // execution.fqn → [accessor.id]
    const accessorsByExecution = new Map();
    (data.links?.executionToAccessor || []).forEach(link => {
        if (!accessorsByExecution.has(link.execution)) {
            accessorsByExecution.set(link.execution, []);
        }
        accessorsByExecution.get(link.execution).push(link.accessor);
    });

    // externalAccessor.fqn → externalAccessor
    const externalAccessorByFqn = new Map();
    (data.externalAccessors || []).forEach(a => externalAccessorByFqn.set(a.fqn, a));

    // execution.fqn → [externalAccessor.fqn]
    const externalAccessorsByExecution = new Map();
    (data.links?.executionToExternalAccessor || []).forEach(link => {
        if (!externalAccessorsByExecution.has(link.execution))
            externalAccessorsByExecution.set(link.execution, []);
        externalAccessorsByExecution.get(link.execution).push(link.accessor);
    });

    return (data.outputPorts || []).map(port => {
        const operations = (port.operations || []).flatMap(op => {
            const execFqn = executionByOperation.get(op.fqn);
            if (!execFqn) return [];
            const execEntry = executionByFqn.get(execFqn);
            const accessorIds = accessorsByExecution.get(execFqn) || [];
            const persistenceAccessors = accessorIds.map(id => methodById.get(id)).filter(Boolean);
            const externalAccessorFqns = externalAccessorsByExecution.get(execFqn) || [];
            const externalAccessors = externalAccessorFqns.map(fqn => externalAccessorByFqn.get(fqn)).filter(Boolean);
            return [{
                outputPortOperation: op,
                outputAdapter: execEntry?.adapter ?? null,
                outputAdapterExecution: execEntry?.exec ?? null,
                persistenceAccessors,
                externalAccessors
            }];
        }).sort((a, b) => {
            const left = a.outputPortOperation.label;
            const right = b.outputPortOperation.label;
            return left.localeCompare(right, "ja");
        });
        return {outputPort: port, operations};
    }).filter(group => group.operations.length > 0)
      .sort((a, b) => {
        const left = a.outputPort.label;
        const right = b.outputPort.label;
        return left.localeCompare(right, "ja");
    });
}

function groupOperationsByPersistenceTarget(operations) {
    const map = new Map();
    operations.forEach(operation => {
        operation.persistenceAccessors?.forEach(op => {
            op.targets?.forEach(target => {
                if (!map.has(target)) {
                    map.set(target, {
                        target: target,
                        operations: [],
                    });
                }
                const group = map.get(target);
                if (!group.operations.includes(operation)) {
                    group.operations.push(operation);
                }
            });
        });
    });
    return Array.from(map.values()).map(group => {
        group.operations.sort((a, b) => {
            const left = a.outputPort.label;
            const right = b.outputPort.label;
            return left.localeCompare(right, "ja");
        });
        return group;
    }).sort((a, b) => {
        return a.target.localeCompare(b.target, "ja");
    });
}

function groupOperationsByExternalType(operations) {
    const map = new Map();
    operations.forEach(operation => {
        operation.externalAccessors?.forEach(accessor => {
            (accessor.externals || []).forEach(ext => {
                if (!map.has(ext.fqn)) map.set(ext.fqn, {externalType: ext, operations: []});
                const group = map.get(ext.fqn);
                if (!group.operations.includes(operation)) group.operations.push(operation);
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
            operation.persistenceAccessors?.forEach(op => {
                op.targets?.forEach(target => targetsSet.add(target));
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
            const id = operation.id ?? "";
            const sqlType = operation.sqlType ?? "";
            const targets = Array.isArray(operation.targets) ? operation.targets.join(", ") : "";
            return `${sqlType} ${id} [${targets}]`.trim();
        });
}

function toCrudChar(sqlType) {
    const type = (sqlType || "").toUpperCase();
    if (type === "SELECT") return "R";
    if (type === "INSERT") return "C";
    if (type === "UPDATE") return "U";
    if (type === "DELETE") return "D";
    return "";
}

function isCrudVisible(sqlType, visibility) {
    switch ((sqlType || "").toUpperCase()) {
        case 'INSERT': return visibility.crudCreate !== false;
        case 'SELECT': return visibility.crudRead !== false;
        case 'UPDATE': return visibility.crudUpdate !== false;
        case 'DELETE': return visibility.crudDelete !== false;
        default: return true;
    }
}

// ===== DOM ユーティリティ =====

function createElement(tag, options = {}) {
    const element = document.createElement(tag);
    if (options.className) element.className = options.className;
    if (options.id) element.id = options.id;
    if (options.textContent) element.textContent = options.textContent;
    if (options.attributes) {
        for (const [key, value] of Object.entries(options.attributes)) {
            element.setAttribute(key, value);
        }
    }
    if (options.style) {
        for (const [key, value] of Object.entries(options.style)) {
            element.style[key] = value;
        }
    }
    if (options.children) {
        options.children.forEach(child => {
            if (child != null) element.appendChild(child);
        });
    }
    return element;
}

function renderNoData(container) {
    container.appendChild(createElement("p", {
        className: "weak",
        textContent: "データなし"
    }));
}

function createSidebarSection(title, items) {
    if (!items || items.length === 0) return null;

    return createElement("section", {
        className: "in-page-sidebar__section",
        children: [
            createElement("p", {
                className: "in-page-sidebar__title",
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

    addNodeToSubgraph(subgraph, id, label, shape = '["$LABEL"]') {
        const nodeLine = `    ${id}${shape.replace('$LABEL', label)}`;
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

const DEFAULT_VISIBILITY = {port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: true, externalAccessor: false, externalType: true, direction: 'LR', crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true};

function renderMermaid(generateCodeFn, data, container, visibility = DEFAULT_VISIBILITY) {
    if (typeof mermaid === "undefined") return;
    const mermaidCode = generateCodeFn(data, visibility);
    if (!mermaidCode) return;
    const id = "mermaid-" + Math.random().toString(36).substring(2, 11);
    mermaid.render(id, mermaidCode).then(({svg}) => {
        container.innerHTML = svg;
    });
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

function addTargetEdges(builder, sourceNodeId, op, targetNodes) {
    op.targets?.forEach(target => {
        if (!targetNodes.has(target)) {
            targetNodes.set(target, `Target_${targetNodes.size}`);
            builder.addNode(targetNodes.get(target), target, '[($LABEL)]');
        }
        const sqlType = op.targetSqlTypes?.[target] || op.sqlType || "";
        if (sourceNodeId) builder.addEdge(sourceNodeId, targetNodes.get(target), sqlType);
    });
}

function addExternalAccessorNode(builder, sourceNodeId, accessor, visibility, extAccessorNodes, extTypeNodes) {
    if (!visibility.externalAccessor) return sourceNodeId;
    const nodeId = `ExtAcc_${builder.sanitize(accessor.fqn)}`;
    if (!extAccessorNodes.has(accessor.fqn)) {
        extAccessorNodes.set(accessor.fqn, nodeId);
        builder.addNode(nodeId, accessor.label, '[/$LABEL\\]');
    }
    if (sourceNodeId) builder.addEdge(sourceNodeId, extAccessorNodes.get(accessor.fqn));

    if (visibility.externalType) {
        (accessor.externals || []).forEach(ext => {
            if (!extTypeNodes.has(ext.fqn)) {
                extTypeNodes.set(ext.fqn, `ExtType_${extTypeNodes.size}`);
                builder.addNode(extTypeNodes.get(ext.fqn), ext.label, '{{$LABEL}}');
            }
            builder.addEdge(extAccessorNodes.get(accessor.fqn), extTypeNodes.get(ext.fqn));
        });
    }
    return extAccessorNodes.get(accessor.fqn);
}

function generatePortMermaidCode(group, visibility = DEFAULT_VISIBILITY) {
    const builder = new MermaidBuilder();
    const portFqn = group.outputPort.fqn;
    const portLabel = group.outputPort.label;

    const portSubgraphs = new Map();
    const adapterSubgraphs = new Map();
    const accessorSubgraphs = new Map();
    const accessorNodes = new Map();
    const targetNodes = new Map();
    const extAccessorNodes = new Map();
    const extTypeNodes = new Map();

    group.operations.forEach((operation, operationIndex) => {
        const portOpName = operation.outputPortOperation.label;
        const portOpFqn = operation.outputPortOperation.fqn;

        const adapterFqn = operation.outputAdapter?.fqn;
        const adapterLabel = operation.outputAdapter?.label;
        const executionName = operation.outputAdapterExecution?.label;
        const executionFqn = operation.outputAdapterExecution?.fqn;

        let lastNodeId = addPortNode(builder, portSubgraphs, portFqn, portLabel, portOpFqn, portOpName, visibility);

        lastNodeId = addAdapterNode(builder, lastNodeId, adapterFqn, adapterLabel, executionFqn, executionName, visibility, adapterSubgraphs);

        operation.persistenceAccessors?.forEach((op) => {
            if (!isCrudVisible(op.sqlType, visibility)) return;

            const currentNode = addAccessorNode(builder, lastNodeId, op, visibility, accessorSubgraphs, accessorNodes);

            if (visibility.target) {
                addTargetEdges(builder, currentNode, op, targetNodes);
            }
        });

        operation.externalAccessors?.forEach(accessor => {
            addExternalAccessorNode(builder, lastNodeId, accessor, visibility, extAccessorNodes, extTypeNodes);
        });
    });

    if (builder.isEmpty()) return null;
    return builder.build(visibility.direction);
}

function generateOperationMermaidCode(operation, visibility = DEFAULT_VISIBILITY) {
    return generatePortMermaidCode(
        {outputPort: operation.outputPort, operations: [operation]},
        visibility
    );
}

function generatePersistenceMermaidCode(group, visibility = DEFAULT_VISIBILITY) {
    const builder = new MermaidBuilder();
    const target = group.target;

    const portSubgraphs = new Map();
    const adapterSubgraphs = new Map();
    const accessorSubgraphs = new Map();
    const accessorNodes = new Map();
    const targetNodes = new Map();

    group.operations.forEach((operation, operationIndex) => {
        const relevantOps = operation.persistenceAccessors.filter(op =>
            op.targets.includes(target) && isCrudVisible(op.sqlType, visibility));

        const portFqn = operation.outputPort.fqn;
        const portLabel = operation.outputPort.label;
        const portOpName = operation.outputPortOperation.label;
        const portOpFqn = operation.outputPortOperation.fqn;

        const adapterFqn = operation.outputAdapter?.fqn;
        const adapterLabel = operation.outputAdapter?.label;
        const executionName = operation.outputAdapterExecution?.label;
        const executionFqn = operation.outputAdapterExecution?.fqn;

        relevantOps.forEach(op => {
            let currentNode = addPortNode(builder, portSubgraphs, portFqn, portLabel, portOpFqn, portOpName, visibility);

            currentNode = addAdapterNode(builder, currentNode, adapterFqn, adapterLabel, executionFqn, executionName, visibility, adapterSubgraphs);

            currentNode = addAccessorNode(builder, currentNode, op, visibility, accessorSubgraphs, accessorNodes);

            if (visibility.target) {
                const targetSqlType = op.targetSqlTypes?.[target] || op.sqlType || "";
                addTargetEdges(builder, currentNode, {targets: [target], sqlType: targetSqlType, targetSqlTypes: {[target]: targetSqlType}}, targetNodes);
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
    const extTypeNodes = new Map();

    group.operations.forEach(operation => {
        const relevantAccessors = (operation.externalAccessors || []).filter(accessor =>
            (accessor.externals || []).some(ext => ext.fqn === externalType.fqn));

        const portFqn = operation.outputPort.fqn;
        const portLabel = operation.outputPort.label;
        const portOpName = operation.outputPortOperation.label;
        const portOpFqn = operation.outputPortOperation.fqn;

        const adapterFqn = operation.outputAdapter?.fqn;
        const adapterLabel = operation.outputAdapter?.label;
        const executionName = operation.outputAdapterExecution?.label;
        const executionFqn = operation.outputAdapterExecution?.fqn;

        relevantAccessors.forEach(accessor => {
            let currentNode = addPortNode(builder, portSubgraphs, portFqn, portLabel, portOpFqn, portOpName, visibility);

            currentNode = addAdapterNode(builder, currentNode, adapterFqn, adapterLabel, executionFqn, executionName, visibility, adapterSubgraphs);

            const accessorNodeId = addExternalAccessorNode(builder, currentNode, accessor, {...visibility, externalAccessor: true}, extAccessorNodes, extTypeNodes);

            if (visibility.externalType) {
                const extFqn = externalType.fqn;
                if (!extTypeNodes.has(extFqn)) {
                    extTypeNodes.set(extFqn, `ExtType_${extTypeNodes.size}`);
                    builder.addNode(extTypeNodes.get(extFqn), externalType.label, '{{$LABEL}}');
                }
                builder.addEdge(extAccessorNodes.get(accessor.fqn), extTypeNodes.get(extFqn));
            }
        });
    });

    if (builder.isEmpty()) return null;
    return builder.build(visibility.direction);
}

// ===== CRUD テーブル描画 =====

function createPortGroupRow(group, allTargets, visibility) {
    return createElement("tr", {
        className: "port-group-row",
        style: {cursor: "pointer"},
        children: [
            createElement("td", {
                className: "port-group-cell",
                children: [
                    document.createTextNode(group.outputPort.label),
                    createElement("span", {
                        className: "weak",
                        style: {marginLeft: "8px"},
                        textContent: `(${group.operations.length})`
                    })
                ]
            }),
            ...allTargets.map(target => {
                const cell = createElement("td", {className: "crud-cell port-crud-cell"});
                const cruds = new Set();
                group.operations.forEach(operation => {
                    operation.persistenceAccessors?.forEach(op => {
                        if (op.targets?.includes(target)) {
                            const targetSqlType = op.targetSqlTypes?.[target] || op.sqlType;
                            const crud = toCrudChar(targetSqlType);
                            if (crud && isCrudVisible(targetSqlType, visibility)) {
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

function createOperationRow(operation, allTargets, portId, visibility) {
    return createElement("tr", {
        className: `operation-row ${portId}`,
        style: {display: "none"},
        children: [
            createElement("td", {
                className: "operation-cell",
                textContent: operation.outputPortOperation.label
            }),
            ...allTargets.map(target => {
                const cell = createElement("td", {className: "crud-cell"});
                const cruds = new Set();
                operation.persistenceAccessors?.forEach(op => {
                    if (op.targets?.includes(target)) {
                        const targetSqlType = op.targetSqlTypes?.[target] || op.sqlType;
                        const crud = toCrudChar(targetSqlType);
                        if (crud && isCrudVisible(targetSqlType, visibility)) {
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

function appendGroupToTable(tbody, group, allTargets, visibility) {
    const portId = "port-" + group.outputPort.fqn.replace(/[^a-zA-Z0-9]/g, '-');
    const portRow = createPortGroupRow(group, allTargets, visibility);
    tbody.appendChild(portRow);

    const opRows = group.operations.map(operation => {
        const row = createOperationRow(operation, allTargets, portId, visibility);
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

function renderCrudTable(grouped, visibility = DEFAULT_VISIBILITY) {
    const container = document.getElementById("outputs-crud");
    if (!container) return;

    container.innerHTML = "";

    const allTargets = collectAllTargets(grouped);

    if (allTargets.length === 0) {
        container.textContent = "永続化操作なし";
        return;
    }

    const headerRow = createElement("tr", {
        children: [
            createElement("th", {textContent: "出力ポート / 操作"}),
            ...allTargets.map(target => createElement("th", {
                id: `crud-target-${target}`,
                textContent: target
            }))
        ]
    });

    const tbody = createElement("tbody");
    grouped.forEach(group => appendGroupToTable(tbody, group, allTargets, visibility));

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

function renderOutputsList(grouped, visibility = DEFAULT_VISIBILITY) {
    const container = document.getElementById("outputs-list");
    const sidebar = document.getElementById("outputs-sidebar-list");
    if (!container) return;
    container.innerHTML = "";
    if (sidebar) sidebar.innerHTML = "";

    grouped.forEach(group => {
        if (!generatePortMermaidCode(group, visibility)) return;
        const portFqnValue = group.outputPort.fqn;
        const portId = "port-" + portFqnValue.replace(/[^a-zA-Z0-9]/g, '-');
        const portLabel = group.outputPort.label;

        const cardChildren = [
            createElement("h3", {textContent: portLabel}),
            createElement("p", {
                className: "fully-qualified-name",
                textContent: portFqnValue
            })
        ];

        if (visibility.adapter) {
            const adapterLabels = Array.from(new Set(group.operations.map(operation => {
                const label = operation.outputAdapter?.label ?? "";
                const fqn = operation.outputAdapter?.fqn ?? "";
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
        lazyRender(portMermaidContainer, () => renderMermaid(generatePortMermaidCode, group, portMermaidContainer, visibility));
        cardChildren.push(portMermaidContainer);

        const itemList = createElement("div", {className: "outputs-item-list"});
        group.operations.forEach(operation => {
            const mermaidContainer = createElement("div", {className: "mermaid-diagram"});
            const operationWithPort = {...operation, outputPort: group.outputPort};
            lazyRender(mermaidContainer, () => renderMermaid(generateOperationMermaidCode, operationWithPort, mermaidContainer, visibility));

            itemList.appendChild(createElement("article", {
                className: "outputs-item",
                children: [
                    createElement("h4", {textContent: operation.outputPortOperation.label}),
                    mermaidContainer,
                    createElement("p", {
                        className: "outputs-persistence-title",
                        textContent: "永続化操作詳細"
                    }),
                    createElement("ul", {
                        className: "outputs-persistence-list",
                        children: formatPersistenceAccessors(operation.persistenceAccessors).map(text => createElement("li", {textContent: text}))
                    })
                ]
            }));
        });
        const itemListDetails = createElement("details", {});
        const itemListSummary = createElement("summary", {
            className: "outputs-item-list-summary",
            textContent: `操作一覧 (${group.operations.length}件)`
        });
        itemListDetails.appendChild(itemListSummary);
        itemListDetails.appendChild(itemList);
        cardChildren.push(itemListDetails);

        container.appendChild(createElement("section", {
            className: "outputs-port-card",
            id: portId,
            children: cardChildren
        }));
    });

    renderSidebarSection(sidebar, "出力ポート", grouped.map(group => {
        return {
            id: "port-" + group.outputPort.fqn.replace(/[^a-zA-Z0-9]/g, '-'),
            label: group.outputPort.label
        };
    }));

    if (grouped.length === 0) {
        renderNoData(container);
    }
}

function renderPersistenceList(grouped, visibility = DEFAULT_VISIBILITY) {
    const container = document.getElementById("persistence-list");
    const sidebar = document.getElementById("persistence-sidebar-list");
    if (!container) return;
    container.innerHTML = "";
    if (sidebar) sidebar.innerHTML = "";

    grouped.forEach(group => {
        if (!generatePersistenceMermaidCode(group, visibility)) return;
        const targetId = "persistence-" + group.target.replace(/[^a-zA-Z0-9]/g, '-');

        const persistenceMermaidContainer = createElement("div", {className: "mermaid-diagram port-diagram"});
        lazyRender(persistenceMermaidContainer, () => renderMermaid(generatePersistenceMermaidCode, group, persistenceMermaidContainer, visibility));

        container.appendChild(createElement("section", {
            className: "outputs-port-card",
            id: targetId,
            children: [
                createElement("h3", {textContent: group.target}),
                persistenceMermaidContainer
            ]
        }));
    });

    renderSidebarSection(sidebar, "永続化操作対象", grouped.map(group => ({
        id: "persistence-" + group.target.replace(/[^a-zA-Z0-9]/g, '-'),
        label: group.target
    })));

    if (grouped.length === 0) {
        renderNoData(container);
    }
}

function renderExternalList(grouped, visibility = DEFAULT_VISIBILITY) {
    const container = document.getElementById("external-list");
    const sidebar = document.getElementById("external-sidebar-list");
    if (!container) return;
    container.innerHTML = "";
    if (sidebar) sidebar.innerHTML = "";

    grouped.forEach(group => {
        if (!generateExternalTypeMermaidCode(group, visibility)) return;
        const externalFqn = group.externalType.fqn;
        const externalId = "external-" + externalFqn.replace(/[^a-zA-Z0-9]/g, '-');

        const externalMermaidContainer = createElement("div", {className: "mermaid-diagram port-diagram"});
        lazyRender(externalMermaidContainer, () => renderMermaid(generateExternalTypeMermaidCode, group, externalMermaidContainer, visibility));

        container.appendChild(createElement("section", {
            className: "outputs-port-card",
            id: externalId,
            children: [
                createElement("h3", {textContent: group.externalType.label}),
                createElement("p", {className: "fully-qualified-name", textContent: externalFqn}),
                externalMermaidContainer
            ]
        }));
    });

    renderSidebarSection(sidebar, "外部型", grouped.map(group => ({
        id: "external-" + group.externalType.fqn.replace(/[^a-zA-Z0-9]/g, '-'),
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
        accessor,
        accessorMethod: checked("show-accessor-method"),
        target: checked("show-target"),
        externalAccessor: checked("show-external-accessor"),
        externalType: checked("show-external-type"),
        direction,
        crudCreate: checked("show-crud-c"),
        crudRead: checked("show-crud-r"),
        crudUpdate: checked("show-crud-u"),
        crudDelete: checked("show-crud-d"),
    };
}

const OutputsApp = {
    state: {
        visibility: {...DEFAULT_VISIBILITY},
        activeTab: 'outputs',
        data: null,
        grouped: null,
        persistenceGrouped: null,
        externalGrouped: null
    },

    init() {
        const data = getOutputsData();
        this.state.data = data;

        const grouped = groupOperationsByOutputPort(data);
        this.state.grouped = grouped;

        const allOperations = grouped.flatMap(group =>
            group.operations.map(operation => ({...operation, outputPort: group.outputPort})));
        this.state.persistenceGrouped = groupOperationsByPersistenceTarget(allOperations);
        this.state.externalGrouped = groupOperationsByExternalType(allOperations);

        if (typeof mermaid !== "undefined") {
            mermaid.initialize({startOnLoad: false});
        }

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
        const parentRules = {
            "show-operation": "show-port",
            "show-execution": "show-adapter",
            "show-accessor-method": "show-accessor",
            "show-external-type": "show-external-accessor",
        };

        document.querySelectorAll('input[name^="show-"]').forEach(input => {
            input.addEventListener('change', () => {
                const name = input.getAttribute("name");
                if (input.checked && parentRules[name]) {
                    const parentEl = document.querySelector(`input[name="${parentRules[name]}"]`);
                    if (parentEl) parentEl.checked = true;
                }
                this.setState({visibility: readVisibility()});
            });
        });

        document.querySelectorAll('input[name="diagram-direction"]').forEach(input => {
            input.addEventListener('change', () => {
                this.setState({visibility: readVisibility()});
            });
        });

        document.querySelectorAll('.outputs-tabs .tab-button').forEach(button => {
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
        document.querySelectorAll('.outputs-tabs .tab-button').forEach(btn => {
            btn.classList.toggle('is-active', btn.getAttribute('data-tab') === activeTab);
        });
        document.querySelectorAll('.outputs-tab-panel').forEach(panel => {
            panel.classList.toggle('is-active', panel.id === `${activeTab}-tab-panel`);
        });
    },

    renderPanels() {
        const {visibility, data, grouped, persistenceGrouped, externalGrouped} = this.state;
        if (!data) return;
        renderPersistenceList(persistenceGrouped, visibility);
        renderExternalList(externalGrouped, visibility);
        renderOutputsList(grouped, visibility);
        renderCrudTable(grouped, visibility);
    }
};

if (typeof window !== "undefined" && typeof document !== "undefined") {
    window.addEventListener("DOMContentLoaded", () => {
        OutputsApp.init();
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        OutputsApp,
        getOutputsData,
        groupOperationsByOutputPort,
        groupOperationsByPersistenceTarget,
        groupOperationsByExternalType,
        formatPersistenceAccessors,
        renderOutputsList,
        renderPersistenceList,
        renderExternalList,
        renderCrudTable,
        toCrudChar,
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
