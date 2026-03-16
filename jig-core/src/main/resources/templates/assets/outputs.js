function getOutputsData() {
    return globalThis.outputPortData || {
        outputPorts: [],
        outputAdapters: [],
        persistenceAccessors: [],
        targets: [],
        links: {operationToExecution: [], executionToAccessor: []}
    };
}

function groupLinksByOutputPort(data) {
    const executionByFqn = new Map();
    (data.outputAdapters || []).forEach(adapter => {
        (adapter.executions || []).forEach(exec => {
            executionByFqn.set(exec.fqn, {exec, adapter});
        });
    });

    const methodById = new Map();
    (data.persistenceAccessors || []).forEach(accessor => {
        (accessor.methods || []).forEach(method => {
            methodById.set(method.id, {...method, group: accessor.fqn, groupLabel: accessor.label});
        });
    });

    const executionByOperation = new Map();
    (data.links?.operationToExecution || []).forEach(link => {
        executionByOperation.set(link.operation, link.execution);
    });

    const accessorsByExecution = new Map();
    (data.links?.executionToAccessor || []).forEach(link => {
        if (!accessorsByExecution.has(link.execution)) {
            accessorsByExecution.set(link.execution, []);
        }
        accessorsByExecution.get(link.execution).push(link.accessor);
    });

    return (data.outputPorts || []).map(port => {
        const links = (port.operations || []).flatMap(op => {
            const execFqn = executionByOperation.get(op.fqn);
            if (!execFqn) return [];
            const execEntry = executionByFqn.get(execFqn);
            const accessorIds = accessorsByExecution.get(execFqn) || [];
            const persistenceAccessors = accessorIds.map(id => methodById.get(id)).filter(Boolean);
            return [{
                outputPortOperation: op,
                outputAdapter: execEntry?.adapter ?? null,
                outputAdapterExecution: execEntry?.exec ?? null,
                persistenceAccessors
            }];
        }).sort((a, b) => {
            const left = a.outputPortOperation?.label ?? a.outputPortOperation?.signature ?? "";
            const right = b.outputPortOperation?.label ?? b.outputPortOperation?.signature ?? "";
            return left.localeCompare(right, "ja");
        });
        return {outputPort: port, links};
    }).filter(group => group.links.length > 0)
      .sort((a, b) => {
        const left = a.outputPort.label ?? a.outputPort.fqn ?? "";
        const right = b.outputPort.label ?? b.outputPort.fqn ?? "";
        return left.localeCompare(right, "ja");
    });
}

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

function createField(label, value) {
    return createElement("div", {
        className: "outputs-item-field",
        children: [
            createElement("dt", {textContent: label}),
            createElement("dd", {textContent: value})
        ]
    });
}

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
            if (child) element.appendChild(child);
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

function MermaidBuilder() {
    this.nodes = [];
    this.edges = [];
    this.subgraphs = [];
    this.edgeSet = new Set();
}

MermaidBuilder.prototype.sanitize = function (id) {
    return (id || "").replace(/[^a-zA-Z0-9]/g, '_');
};

MermaidBuilder.prototype.addNode = function (id, label, shape = '["$LABEL"]') {
    const nodeLine = `${id}${shape.replace('$LABEL', label)}`;
    if (!this.nodes.includes(nodeLine)) {
        this.nodes.push(nodeLine);
    }
    return id;
};

MermaidBuilder.prototype.addEdge = function (from, to, label = "") {
    const edgeKey = `${from}--${label}-->${to}`;
    if (!this.edgeSet.has(edgeKey)) {
        this.edgeSet.add(edgeKey);
        const edgeLine = label ? `  ${from} -- "${label}" --> ${to}` : `  ${from} --> ${to}`;
        this.edges.push(edgeLine);
    }
};

MermaidBuilder.prototype.startSubgraph = function (label) {
    const subgraph = {label, lines: []};
    this.subgraphs.push(subgraph);
    return subgraph;
};

MermaidBuilder.prototype.ensureSubgraph = function (map, key, label) {
    if (!map.has(key)) {
        map.set(key, this.startSubgraph(label));
    }
    return map.get(key);
};

MermaidBuilder.prototype.addNodeToSubgraph = function (subgraph, id, label, shape = '["$LABEL"]') {
    const nodeLine = `    ${id}${shape.replace('$LABEL', label)}`;
    if (!subgraph.lines.includes(nodeLine)) {
        subgraph.lines.push(nodeLine);
    }
    return id;
};

MermaidBuilder.prototype.build = function (direction = 'LR') {
    let code = `graph ${direction}\n`;
    this.subgraphs.forEach(sg => {
        code += `  subgraph "${sg.label}"\n`;
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
};

const DEFAULT_VISIBILITY = {port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: true, direction: 'LR', crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true};

function generateMermaidCode(link, visibility = DEFAULT_VISIBILITY) {
    const builder = new MermaidBuilder();
    const accessorSubgraphs = new Map();
    const accessorNodes = new Map();
    const targetNodes = new Map();

    let lastNode = null;

    if (visibility.port) {
        const portLabel = link.outputPort?.label || link.outputPort?.fqn || "Port";
        const portOpName = link.outputPortOperation?.label || link.outputPortOperation?.signature || "Operation";
        if (visibility.operation) {
            const portSubgraph = builder.startSubgraph(portLabel);
            builder.addNodeToSubgraph(portSubgraph, "PortOp", portOpName);
            lastNode = "PortOp";
        } else {
            builder.addNode("Port", portLabel);
            lastNode = "Port";
        }
    }

    if (visibility.adapter) {
        const adapterLabel = link.outputAdapter?.label || link.outputAdapter?.fqn || "Adapter";
        const executionName = link.outputAdapterExecution?.label || link.outputAdapterExecution?.signature || "Execution";
        if (visibility.execution) {
            const adapterSubgraph = builder.startSubgraph(adapterLabel);
            builder.addNodeToSubgraph(adapterSubgraph, "Execution", executionName);
            if (lastNode) builder.addEdge(lastNode, "Execution");
            lastNode = "Execution";
        } else {
            builder.addNode("Adapter", adapterLabel);
            if (lastNode) builder.addEdge(lastNode, "Adapter");
            lastNode = "Adapter";
        }
    }

    link.persistenceAccessors?.forEach((op) => {
        if (!isCrudVisible(op.sqlType, visibility)) return;
        const sqlType = op.sqlType || "";

        const currentNode = addAccessorNode(builder, lastNode, op, visibility, accessorSubgraphs, accessorNodes);

        if (visibility.target) {
            addTargetEdges(builder, currentNode, op.targets, targetNodes, sqlType);
        }
    });

    return builder.build(visibility.direction);
}

function renderMermaid(link, container, visibility = DEFAULT_VISIBILITY) {
    if (typeof mermaid === "undefined") return;

    const mermaidCode = generateMermaidCode(link, visibility);
    const id = "mermaid-" + Math.random().toString(36).substr(2, 9);
    mermaid.render(id, mermaidCode).then(({svg}) => {
        container.innerHTML = svg;
    });
}

function generatePortMermaidCode(group, visibility = DEFAULT_VISIBILITY) {
    const builder = new MermaidBuilder();
    const portLabel = group.outputPort?.label || group.outputPort?.fqn || "Port";

    if (visibility.port) {
        if (visibility.operation) {
            const portSubgraph = builder.startSubgraph(portLabel);
            group.links.forEach((link, index) => {
                const portOpName = link.outputPortOperation?.label || link.outputPortOperation?.signature || `Operation_${index}`;
                builder.addNodeToSubgraph(portSubgraph, `PortOp_${index}`, portOpName);
            });
        } else {
            builder.addNode("Port", portLabel);
        }
    }

    const adapterSubgraphs = new Map();
    const accessorSubgraphs = new Map();
    const accessorNodes = new Map();
    const targetNodes = new Map();

    group.links.forEach((link, linkIndex) => {
        const adapterFqn = link.outputAdapter?.fqn || `Adapter_${linkIndex}`;
        const adapterLabel = link.outputAdapter?.label || adapterFqn;
        const executionName = link.outputAdapterExecution?.label || link.outputAdapterExecution?.signature || `Execution_${linkIndex}`;
        const executionFqn = link.outputAdapterExecution?.fqn || `${adapterFqn}.${executionName}`;

        let lastNodeId = visibility.port
            ? (visibility.operation ? `PortOp_${linkIndex}` : "Port")
            : null;

        lastNodeId = addAdapterNode(builder, lastNodeId, adapterFqn, adapterLabel, executionFqn, executionName, visibility, adapterSubgraphs);

        link.persistenceAccessors?.forEach((op) => {
            if (!isCrudVisible(op.sqlType, visibility)) return;
            const sqlType = op.sqlType || "";

            const currentNode = addAccessorNode(builder, lastNodeId, op, visibility, accessorSubgraphs, accessorNodes);

            if (visibility.target) {
                addTargetEdges(builder, currentNode, op.targets, targetNodes, sqlType);
            }
        });
    });

    return builder.build(visibility.direction);
}

function renderPortMermaid(group, container, visibility = DEFAULT_VISIBILITY) {
    if (typeof mermaid === "undefined") return;

    const mermaidCode = generatePortMermaidCode(group, visibility);
    const id = "mermaid-port-" + Math.random().toString(36).substr(2, 9);
    mermaid.render(id, mermaidCode).then(({svg}) => {
        container.innerHTML = svg;
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

function addTargetEdges(builder, sourceNodeId, targets, targetNodes, sqlType) {
    targets?.forEach(target => {
        if (!targetNodes.has(target)) {
            targetNodes.set(target, `Target_${targetNodes.size}`);
            builder.addNode(targetNodes.get(target), target, '[($LABEL)]');
        }
        if (sourceNodeId) builder.addEdge(sourceNodeId, targetNodes.get(target), sqlType);
    });
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

function renderCrudTable(grouped, visibility = DEFAULT_VISIBILITY) {
    const container = document.getElementById("outputs-crud");
    if (!container) return;

    container.innerHTML = "";

    const targetsSet = new Set();
    grouped.forEach(group => {
        group.links.forEach(link => {
            link.persistenceAccessors?.forEach(op => {
                op.targets?.forEach(target => targetsSet.add(target));
            });
        });
    });
    const allTargets = Array.from(targetsSet).sort();

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

    const table = createElement("table", {
        className: "zebra crud-table",
        children: [
            createElement("thead", {children: [headerRow]})
        ]
    });

    const tbody = createElement("tbody");
    table.appendChild(tbody);

    grouped.forEach(group => {
        const portId = "port-" + Math.random().toString(36).substr(2, 9);
        const portRow = createElement("tr", {
            className: "port-group-row",
            style: {cursor: "pointer"},
            children: [
                createElement("td", {
                    className: "port-group-cell",
                    children: [
                        document.createTextNode(group.outputPort.label || group.outputPort.fqn || "(unknown)"),
                        createElement("span", {
                            className: "weak",
                            style: {marginLeft: "8px"},
                            textContent: `(${group.links.length})`
                        })
                    ]
                }),
                ...allTargets.map(target => {
                    const cell = createElement("td", {className: "crud-cell port-crud-cell"});
                    const portTargetCrudMap = new Map();
                    group.links.forEach(link => {
                        link.persistenceAccessors?.forEach(op => {
                            const crud = toCrudChar(op.sqlType);
                            if (crud && op.targets?.includes(target) && isCrudVisible(op.sqlType, visibility)) {
                                const current = portTargetCrudMap.get(target) || new Set();
                                current.add(crud);
                                portTargetCrudMap.set(target, current);
                            }
                        });
                    });
                    const cruds = portTargetCrudMap.get(target);
                    if (cruds) {
                        cell.textContent = Array.from(cruds).sort().join("");
                    }
                    return cell;
                })
            ]
        });

        tbody.appendChild(portRow);

        // 操作行の作成
        const opRows = group.links.map(link => {
            const row = createElement("tr", {
                className: `operation-row ${portId}`,
                style: {display: "none"},
                children: [
                    createElement("td", {
                        className: "operation-cell",
                        textContent: link.outputPortOperation?.label || link.outputPortOperation?.signature || ""
                    }),
                    ...allTargets.map(target => {
                        const cell = createElement("td", {className: "crud-cell"});
                        const targetCrudMap = new Set();
                        link.persistenceAccessors?.forEach(op => {
                            const crud = toCrudChar(op.sqlType);
                            if (crud && op.targets?.includes(target) && isCrudVisible(op.sqlType, visibility)) {
                                targetCrudMap.add(crud);
                            }
                        });
                        if (targetCrudMap.size > 0) {
                            cell.textContent = Array.from(targetCrudMap).sort().join("");
                        }
                        return cell;
                    })
                ]
            });
            tbody.appendChild(row);
            return row;
        });

        // トグル動作の設定
        portRow.addEventListener("click", () => {
            const isHidden = opRows[0].style.display === "none";
            opRows.forEach(row => {
                row.style.display = isHidden ? "table-row" : "none";
            });
            portRow.classList.toggle("is-expanded", isHidden);
        });
    });

    container.appendChild(table);
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

function groupLinksByPersistenceTarget(links) {
    const map = new Map();
    links.forEach(link => {
        link.persistenceAccessors?.forEach(op => {
            op.targets?.forEach(target => {
                if (!map.has(target)) {
                    map.set(target, {
                        target: target,
                        links: [],
                    });
                }
                const group = map.get(target);
                if (!group.links.includes(link)) {
                    group.links.push(link);
                }
            });
        });
    });
    return Array.from(map.values()).map(group => {
        group.links.sort((a, b) => {
            const left = a.outputPort.label ?? a.outputPort.fqn ?? "";
            const right = b.outputPort.label ?? b.outputPort.fqn ?? "";
            return left.localeCompare(right, "ja");
        });
        return group;
    }).sort((a, b) => {
        return a.target.localeCompare(b.target, "ja");
    });
}

function generatePersistenceMermaidCode(group, visibility = DEFAULT_VISIBILITY) {
    const builder = new MermaidBuilder();
    const target = group.target;

    const portSubgraphs = new Map();
    const adapterSubgraphs = new Map();
    const accessorSubgraphs = new Map();
    const accessorNodes = new Map();

    group.links.forEach((link, linkIndex) => {
        const relevantOps = link.persistenceAccessors.filter(op =>
            op.targets.includes(target) && isCrudVisible(op.sqlType, visibility));

        const portFqn = link.outputPort?.fqn || `Port_${linkIndex}`;
        const portLabel = link.outputPort?.label || portFqn;
        const portOpName = link.outputPortOperation?.label || link.outputPortOperation?.signature || `PortOp_${linkIndex}`;
        const portOpFqn = link.outputPortOperation?.fqn || `${portFqn}.${portOpName}`;
        const portOpId = `PortOp_${builder.sanitize(portOpFqn)}`;

        const adapterFqn = link.outputAdapter?.fqn || `Adapter_${linkIndex}`;
        const adapterLabel = link.outputAdapter?.label || adapterFqn;
        const executionName = link.outputAdapterExecution?.label || link.outputAdapterExecution?.signature || `Execution_${linkIndex}`;
        const executionFqn = link.outputAdapterExecution?.fqn || `${adapterFqn}.${executionName}`;

        relevantOps.forEach(op => {
            let currentNode = null;

            if (visibility.port) {
                if (visibility.operation) {
                    builder.addNodeToSubgraph(
                        builder.ensureSubgraph(portSubgraphs, portFqn, portLabel),
                        portOpId, portOpName
                    );
                    currentNode = portOpId;
                } else {
                    builder.addNode(`Port_${builder.sanitize(portFqn)}`, portLabel);
                    currentNode = `Port_${builder.sanitize(portFqn)}`;
                }
            }

            currentNode = addAdapterNode(builder, currentNode, adapterFqn, adapterLabel, executionFqn, executionName, visibility, adapterSubgraphs);

            currentNode = addAccessorNode(builder, currentNode, op, visibility, accessorSubgraphs, accessorNodes);

            if (visibility.target) {
                builder.addNode("Target", target, "[($LABEL)]");
                if (currentNode) builder.addEdge(currentNode, "Target", op.sqlType);
            }
        });
    });

    return builder.build(visibility.direction);
}

function renderPersistenceMermaid(group, container, visibility = DEFAULT_VISIBILITY) {
    if (typeof mermaid === "undefined") return;

    const mermaidCode = generatePersistenceMermaidCode(group, visibility);
    const id = "mermaid-persistence-" + Math.random().toString(36).substr(2, 9);
    mermaid.render(id, mermaidCode).then(({svg}) => {
        container.innerHTML = svg;
    });
}

function renderPersistenceTable(grouped, visibility = DEFAULT_VISIBILITY) {
    const container = document.getElementById("persistence-list");
    const sidebar = document.getElementById("persistence-sidebar-list");
    if (!container) return;
    container.innerHTML = "";
    if (sidebar) sidebar.innerHTML = "";

    grouped.forEach(group => {
        const targetId = "persistence-" + group.target.replace(/[^a-zA-Z0-9]/g, '-');

        const persistenceMermaidContainer = createElement("div", {className: "mermaid-diagram port-diagram"});
        lazyRender(persistenceMermaidContainer, () => renderPersistenceMermaid(group, persistenceMermaidContainer, visibility));

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

function renderOutputsTable(grouped, visibility = DEFAULT_VISIBILITY) {
    const container = document.getElementById("outputs-list");
    const sidebar = document.getElementById("outputs-sidebar-list");
    if (!container) return;
    container.innerHTML = "";
    if (sidebar) sidebar.innerHTML = "";

    grouped.forEach(group => {
        const portFqnValue = group.outputPort.fqn ?? "";
        const portId = "port-" + portFqnValue.replace(/[^a-zA-Z0-9]/g, '-');
        const portLabel = group.outputPort.label ?? group.outputPort.fqn ?? "(unknown)";

        const cardChildren = [
            createElement("h3", {textContent: portLabel}),
            createElement("p", {
                className: "fully-qualified-name",
                textContent: portFqnValue
            })
        ];

        if (visibility.adapter) {
            const adapterLabels = Array.from(new Set(group.links.map(link => {
                const label = link.outputAdapter?.label ?? link.outputAdapter?.fqn ?? "";
                const fqn = link.outputAdapter?.fqn ?? "";
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
            textContent: `${group.links.length} operations`
        }));

        const portMermaidContainer = createElement("div", {className: "mermaid-diagram port-diagram"});
        lazyRender(portMermaidContainer, () => renderPortMermaid(group, portMermaidContainer, visibility));
        cardChildren.push(portMermaidContainer);

        const itemList = createElement("div", {className: "outputs-item-list"});
        group.links.forEach(link => {
            const mermaidContainer = createElement("div", {className: "mermaid-diagram"});
            lazyRender(mermaidContainer, () => renderMermaid(link, mermaidContainer, visibility));

            itemList.appendChild(createElement("article", {
                className: "outputs-item",
                children: [
                    createElement("h4", {textContent: link.outputPortOperation?.label ?? link.outputPortOperation?.signature ?? ""}),
                    mermaidContainer,
                    createElement("p", {
                        className: "outputs-persistence-title",
                        textContent: "永続化操作詳細"
                    }),
                    createElement("ul", {
                        className: "outputs-persistence-list",
                        children: formatPersistenceAccessors(link.persistenceAccessors).map(text => createElement("li", {textContent: text}))
                    })
                ]
            }));
        });
        const itemListDetails = createElement("details", {});
        const itemListSummary = createElement("summary", {
            className: "outputs-item-list-summary",
            textContent: `操作一覧 (${group.links.length}件)`
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
        const portFqnValue = group.outputPort.fqn ?? "";
        return {
            id: "port-" + portFqnValue.replace(/[^a-zA-Z0-9]/g, '-'),
            label: group.outputPort.label ?? group.outputPort.fqn ?? "(unknown)"
        };
    }));

    if (grouped.length === 0) {
        renderNoData(container);
    }
}

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
        operation: port && checked("show-operation"),
        adapter,
        execution: adapter && checked("show-execution"),
        accessor,
        accessorMethod: accessor && checked("show-accessor-method"),
        target: checked("show-target"),
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
        persistenceGrouped: null
    },

    init() {
        const data = getOutputsData();
        this.state.data = data;

        const grouped = groupLinksByOutputPort(data);
        this.state.grouped = grouped;

        const allLinks = grouped.flatMap(group =>
            group.links.map(link => ({...link, outputPort: group.outputPort})));
        this.state.persistenceGrouped = groupLinksByPersistenceTarget(allLinks);

        if (typeof mermaid !== "undefined") {
            mermaid.initialize({startOnLoad: false});
        }

        this.bindEvents();
        this.render();
    },

    setState(newState) {
        this.state = {...this.state, ...newState};
        this.render();
    },

    bindEvents() {
        const cascadeRules = {
            "show-port": ["show-operation"],
            "show-adapter": ["show-execution"],
            "show-accessor": ["show-accessor-method"],
        };

        document.querySelectorAll('input[name^="show-"]').forEach(input => {
            input.addEventListener('change', () => {
                const name = input.getAttribute("name");
                const children = cascadeRules[name] || [];
                children.forEach(childName => {
                    const childEl = document.querySelector(`input[name="${childName}"]`);
                    if (childEl) {
                        if (!input.checked) childEl.checked = false;
                        childEl.disabled = !input.checked;
                    }
                });
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
        const {visibility, activeTab, data, grouped, persistenceGrouped} = this.state;
        if (!data) return;

        // タブの表示切り替え
        document.querySelectorAll('.outputs-tabs .tab-button').forEach(btn => {
            btn.classList.toggle('is-active', btn.getAttribute('data-tab') === activeTab);
        });
        document.querySelectorAll('.outputs-tab-panel').forEach(panel => {
            panel.classList.toggle('is-active', panel.id === `${activeTab}-tab-panel`);
        });

        // 各パネルの描画
        renderPersistenceTable(persistenceGrouped, visibility);
        renderOutputsTable(grouped, visibility);
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
        groupLinksByOutputPort,
        groupLinksByPersistenceTarget,
        formatPersistenceAccessors,
        createField,
        renderOutputsTable,
        renderPersistenceTable,
        renderCrudTable,
        toCrudChar,
        createElement,
        createSidebarSection,
        renderSidebarSection,
        renderNoData,
        generateMermaidCode,
        generatePortMermaidCode,
        generatePersistenceMermaidCode,
        MermaidBuilder,
    };
}
