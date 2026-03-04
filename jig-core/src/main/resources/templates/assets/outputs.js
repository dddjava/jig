function getOutputsData() {
    const jsonText = document.getElementById("outputs-data")?.textContent || "{}";
    const data = JSON.parse(jsonText);

    const ports = data.ports || {};
    const operations = data.operations || {};
    const adapters = data.adapters || {};
    const executions = data.executions || {};
    const persistenceOperationsMaster = data.persistenceOperations || {};

    const links = (data.links || []).map(link => {
        const pOps = (link.persistenceOperations || []).map(id => {
            const op = persistenceOperationsMaster[id];
            return {
                ...op,
                groupLabel: op.group?.split('.').pop() || op.group
            };
        });
        return {
            outputPort: ports[link.port],
            outputPortOperation: operations[link.operation],
            outputAdapter: adapters[link.adapter],
            outputAdapterExecution: executions[link.execution],
            persistenceOperations: pOps
        };
    });

    return {
        links: links,
    };
}

function groupLinksByOutputPort(links) {
    const map = new Map();
    links.forEach(link => {
        const key = link.outputPort?.fqn ?? "";
        if (!map.has(key)) {
            map.set(key, {
                outputPort: link.outputPort ?? {},
                links: [],
            });
        }
        map.get(key).links.push(link);
    });
    return Array.from(map.values()).map(group => {
        group.links.sort((a, b) => {
            const left = a.outputPortOperation?.name ?? a.outputPortOperation?.signature ?? "";
            const right = b.outputPortOperation?.name ?? b.outputPortOperation?.signature ?? "";
            return left.localeCompare(right, "ja");
        });
        return group;
    }).sort((a, b) => {
        const left = a.outputPort.label ?? a.outputPort.fqn ?? "";
        const right = b.outputPort.label ?? b.outputPort.fqn ?? "";
        return left.localeCompare(right, "ja");
    });
}

function formatPersistenceOperations(persistenceOperations) {
    if (!Array.isArray(persistenceOperations) || persistenceOperations.length === 0) {
        return ["なし"];
    }
    return persistenceOperations
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
            createElement("dt", { textContent: label }),
            createElement("dd", { textContent: value })
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

function addSidebarItem(sidebarList, id, label) {
    if (!sidebarList) return;
    sidebarList.appendChild(createElement("li", {
        children: [
            createElement("a", {
                attributes: { href: "#" + id },
                textContent: label
            })
        ]
    }));
}

function MermaidBuilder() {
    this.nodes = [];
    this.edges = [];
    this.subgraphs = [];
    this.edgeSet = new Set();
}

MermaidBuilder.prototype.sanitize = function(id) {
    return (id || "").replace(/[^a-zA-Z0-9]/g, '_');
};

MermaidBuilder.prototype.addNode = function(id, label, shape = '["$LABEL"]') {
    const nodeLine = `${id}${shape.replace('$LABEL', label)}`;
    if (!this.nodes.includes(nodeLine)) {
        this.nodes.push(nodeLine);
    }
    return id;
};

MermaidBuilder.prototype.addEdge = function(from, to, label = "") {
    const edgeKey = `${from}--${label}-->${to}`;
    if (!this.edgeSet.has(edgeKey)) {
        this.edgeSet.add(edgeKey);
        const edgeLine = label ? `  ${from} -- "${label}" --> ${to}` : `  ${from} --> ${to}`;
        this.edges.push(edgeLine);
    }
};

MermaidBuilder.prototype.startSubgraph = function(label) {
    const subgraph = { label, lines: [] };
    this.subgraphs.push(subgraph);
    return subgraph;
};

MermaidBuilder.prototype.addNodeToSubgraph = function(subgraph, id, label, shape = '["$LABEL"]') {
    const nodeLine = `    ${id}${shape.replace('$LABEL', label)}`;
    subgraph.lines.push(nodeLine);
    return id;
};

MermaidBuilder.prototype.build = function() {
    let code = "graph LR\n";
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

function generateMermaidCode(link, mode = 'standard') {
    const builder = new MermaidBuilder();

    const portLabel = link.outputPort?.label || link.outputPort?.fqn || "Port";
    const portOpName = link.outputPortOperation?.name || link.outputPortOperation?.signature || "Operation";
    const portSubgraph = builder.startSubgraph(portLabel);
    builder.addNodeToSubgraph(portSubgraph, "PortOp", portOpName);

    let lastNode = "PortOp";

    if (mode !== 'simple') {
        const adapterLabel = link.outputAdapter?.label || link.outputAdapter?.fqn || "Adapter";
        const executionName = link.outputAdapterExecution?.name || link.outputAdapterExecution?.signature || "Execution";
        const adapterSubgraph = builder.startSubgraph(adapterLabel);
        builder.addNodeToSubgraph(adapterSubgraph, "Execution", executionName);
        builder.addEdge("PortOp", "Execution");
        lastNode = "Execution";
    }

    const targetNodes = new Map();
    const groupNodes = new Map();

    link.persistenceOperations?.forEach((op) => {
        const sqlType = op.sqlType || "";
        const groupId = op.group;
        const groupLabel = op.groupLabel;

        let currentLastNode = lastNode;

        if (mode === 'detailed' && groupId) {
            if (!groupNodes.has(groupId)) {
                const groupNodeId = `Group_${groupNodes.size}`;
                groupNodes.set(groupId, groupNodeId);
                const groupSubgraph = builder.startSubgraph(groupLabel);
                builder.addNodeToSubgraph(groupSubgraph, groupNodeId, op.id.split('.').pop());
                builder.addEdge(lastNode, groupNodeId);
            }
            currentLastNode = groupNodes.get(groupId);
        }

        op.targets?.forEach((target) => {
            if (!targetNodes.has(target)) {
                const targetId = `Target_${targetNodes.size}`;
                targetNodes.set(target, targetId);
                builder.addNode(targetId, target, '[($LABEL)]');
            }
            const targetId = targetNodes.get(target);
            builder.addEdge(currentLastNode, targetId, sqlType);
        });
    });

    return builder.build();
}

function renderMermaid(link, container, mode = 'standard') {
    if (typeof mermaid === "undefined") return;

    const mermaidCode = generateMermaidCode(link, mode);
    const id = "mermaid-" + Math.random().toString(36).substr(2, 9);
    mermaid.render(id, mermaidCode).then(({svg}) => {
        container.innerHTML = svg;
    });
}

function generatePortMermaidCode(group, mode = 'standard') {
    const builder = new MermaidBuilder();
    const portLabel = group.outputPort?.label || group.outputPort?.fqn || "Port";

    const portSubgraph = builder.startSubgraph(portLabel);
    group.links.forEach((link, index) => {
        const portOpName = link.outputPortOperation?.name || link.outputPortOperation?.signature || `Operation_${index}`;
        builder.addNodeToSubgraph(portSubgraph, `PortOp_${index}`, portOpName);
    });

    const adapterSubgraphs = new Map();
    const executionNodes = new Map();
    const groupSubgraphs = new Map();
    const targetNodes = new Map();

    group.links.forEach((link, linkIndex) => {
        const adapterFqn = link.outputAdapter?.fqn || `Adapter_${linkIndex}`;
        const adapterLabel = link.outputAdapter?.label || adapterFqn;
        const executionName = link.outputAdapterExecution?.name || link.outputAdapterExecution?.signature || `Execution_${linkIndex}`;
        const executionFqn = link.outputAdapterExecution?.fqn || `${adapterFqn}.${executionName}`;

        let lastNodeId = `PortOp_${linkIndex}`;

        if (mode !== 'simple') {
            if (!adapterSubgraphs.has(adapterFqn)) {
                adapterSubgraphs.set(adapterFqn, builder.startSubgraph(adapterLabel));
            }
            if (!executionNodes.has(executionFqn)) {
                const executionId = `Exec_${builder.sanitize(executionFqn)}`;
                executionNodes.set(executionFqn, executionId);
                builder.addNodeToSubgraph(adapterSubgraphs.get(adapterFqn), executionId, executionName);
            }
            const executionId = executionNodes.get(executionFqn);
            builder.addEdge(lastNodeId, executionId);
            lastNodeId = executionId;
        }

        link.persistenceOperations?.forEach((op) => {
            const sqlType = op.sqlType || "";
            const groupId = op.group;
            const groupLabel = op.groupLabel;

            let currentLastNodeId = lastNodeId;

            if (mode === 'detailed' && groupId) {
                if (!groupSubgraphs.has(groupId)) {
                    groupSubgraphs.set(groupId, builder.startSubgraph(groupLabel));
                }
                const opNodeId = `POp_${builder.sanitize(op.id)}`;
                builder.addNodeToSubgraph(groupSubgraphs.get(groupId), opNodeId, op.id.split('.').pop());
                builder.addEdge(lastNodeId, opNodeId);
                currentLastNodeId = opNodeId;
            }

            op.targets?.forEach((target) => {
                if (!targetNodes.has(target)) {
                    const targetId = `Target_${targetNodes.size}`;
                    targetNodes.set(target, targetId);
                    builder.addNode(targetId, target, '[($LABEL)]');
                }
                const targetId = targetNodes.get(target);
                builder.addEdge(currentLastNodeId, targetId, sqlType);
            });
        });
    });

    return builder.build();
}

function renderPortMermaid(group, container, mode = 'standard') {
    if (typeof mermaid === "undefined") return;

    const mermaidCode = generatePortMermaidCode(group, mode);
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

function renderCrudTable(links) {
    const container = document.getElementById("outputs-crud");
    const sidebarList = document.getElementById("crud-sidebar-list");
    if (!container) return;

    container.innerHTML = "";
    if (sidebarList) sidebarList.innerHTML = "";

    const targetsSet = new Set();
    links.forEach(link => {
        link.persistenceOperations?.forEach(op => {
            op.targets?.forEach(target => targetsSet.add(target));
        });
    });
    const allTargets = Array.from(targetsSet).sort();

    if (allTargets.length === 0) {
        container.textContent = "永続化操作なし";
        return;
    }

    if (sidebarList) {
        sidebarList.appendChild(createElement("p", {
            className: "sidebar-title",
            textContent: "永続化操作対象"
        }));

        allTargets.forEach(target => {
            sidebarList.appendChild(createElement("a", {
                attributes: { href: `#crud-target-${target}` },
                textContent: target,
                className: "sidebar-link"
            }));
        });
    }

    const headerRow = createElement("tr", {
        children: [
            createElement("th", { textContent: "出力ポート / 操作" }),
            ...allTargets.map(target => createElement("th", {
                id: `crud-target-${target}`,
                textContent: target
            }))
        ]
    });

    const table = createElement("table", {
        className: "zebra crud-table",
        children: [
            createElement("thead", { children: [headerRow] })
        ]
    });

    const tbody = createElement("tbody");
    table.appendChild(tbody);
    const grouped = groupLinksByOutputPort(links);

    grouped.forEach(group => {
        const portId = "port-" + Math.random().toString(36).substr(2, 9);
        const portRow = createElement("tr", {
            className: "port-group-row",
            style: { cursor: "pointer" },
            children: [
                createElement("td", {
                    className: "port-group-cell",
                    children: [
                        document.createTextNode(group.outputPort.label || group.outputPort.fqn || "(unknown)"),
                        createElement("span", {
                            className: "weak",
                            style: { marginLeft: "8px" },
                            textContent: `(${group.links.length})`
                        })
                    ]
                }),
                ...allTargets.map(target => {
                    const cell = createElement("td", { className: "crud-cell port-crud-cell" });
                    const portTargetCrudMap = new Map();
                    group.links.forEach(link => {
                        link.persistenceOperations?.forEach(op => {
                            const crud = toCrudChar(op.sqlType);
                            if (crud && op.targets?.includes(target)) {
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
                style: { display: "none" },
                children: [
                    createElement("td", {
                        className: "operation-cell",
                        textContent: link.outputPortOperation?.name || link.outputPortOperation?.signature || ""
                    }),
                    ...allTargets.map(target => {
                        const cell = createElement("td", { className: "crud-cell" });
                        const targetCrudMap = new Set();
                        link.persistenceOperations?.forEach(op => {
                            const crud = toCrudChar(op.sqlType);
                            if (crud && op.targets?.includes(target)) {
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

function initMermaid() {
    if (typeof mermaid !== "undefined") {
        mermaid.initialize({ startOnLoad: false });
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
    }, { rootMargin: "200px" });
    observer.observe(container);
}

function groupLinksByPersistenceTarget(links) {
    const map = new Map();
    links.forEach(link => {
        link.persistenceOperations?.forEach(op => {
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

function generatePersistenceMermaidCode(group) {
    const builder = new MermaidBuilder();
    const target = group.target;
    builder.addNode("Target", target, "[($LABEL)]");

    const opNodes = new Map();
    const adapterSubgraphs = new Map();
    const portSubgraphs = new Map();
    const persistenceGroupSubgraphs = new Map();

    group.links.forEach((link, linkIndex) => {
        const relevantOps = link.persistenceOperations.filter(op => op.targets.includes(target));

        relevantOps.forEach(op => {
            const opNodeId = `POp_${builder.sanitize(op.id)}`;
            if (!opNodes.has(op.id)) {
                const groupId = op.group || 'default';
                const groupLabel = op.groupLabel || 'Persistence Operations';
                if (!persistenceGroupSubgraphs.has(groupId)) {
                    persistenceGroupSubgraphs.set(groupId, builder.startSubgraph(groupLabel));
                }
                builder.addNodeToSubgraph(persistenceGroupSubgraphs.get(groupId), opNodeId, op.id.split('.').pop());
                opNodes.set(op.id, opNodeId);
            }
            builder.addEdge(opNodeId, "Target", op.sqlType);

            const adapterFqn = link.outputAdapter?.fqn || `Adapter_${linkIndex}`;
            const adapterLabel = link.outputAdapter?.label || adapterFqn;
            const executionName = link.outputAdapterExecution?.name || link.outputAdapterExecution?.signature || `Execution_${linkIndex}`;
            const executionFqn = link.outputAdapterExecution?.fqn || `${adapterFqn}.${executionName}`;
            const executionId = `Execution_${builder.sanitize(executionFqn)}`;

            if (!adapterSubgraphs.has(adapterFqn)) {
                adapterSubgraphs.set(adapterFqn, builder.startSubgraph(adapterLabel));
            }
            builder.addNodeToSubgraph(adapterSubgraphs.get(adapterFqn), executionId, executionName);
            builder.addEdge(executionId, opNodeId);

            const portFqn = link.outputPort?.fqn || `Port_${linkIndex}`;
            const portLabel = link.outputPort?.label || portFqn;
            const portOpName = link.outputPortOperation?.name || link.outputPortOperation?.signature || `PortOp_${linkIndex}`;
            const portOpFqn = link.outputPortOperation?.fqn || `${portFqn}.${portOpName}`;
            const portOpId = `PortOp_${builder.sanitize(portOpFqn)}`;

            if (!portSubgraphs.has(portFqn)) {
                portSubgraphs.set(portFqn, builder.startSubgraph(portLabel));
            }
            builder.addNodeToSubgraph(portSubgraphs.get(portFqn), portOpId, portOpName);
            builder.addEdge(portOpId, executionId);
        });
    });

    return builder.build();
}

function renderPersistenceMermaid(group, container) {
    if (typeof mermaid === "undefined") return;

    const mermaidCode = generatePersistenceMermaidCode(group);
    const id = "mermaid-persistence-" + Math.random().toString(36).substr(2, 9);
    mermaid.render(id, mermaidCode).then(({svg}) => {
        container.innerHTML = svg;
    });
}

function renderPersistenceTable(grouped) {
    const container = document.getElementById("persistence-list");
    const sidebar = document.getElementById("persistence-sidebar-list");
    if (!container) return;
    container.innerHTML = "";
    if (sidebar) sidebar.innerHTML = "";

    const sidebarList = sidebar ? createElement("ul") : null;

    grouped.forEach(group => {
        const targetId = "persistence-" + group.target.replace(/[^a-zA-Z0-9]/g, '-');
        addSidebarItem(sidebarList, targetId, group.target);

        const persistenceMermaidContainer = createElement("div", { className: "mermaid-diagram port-diagram" });
        lazyRender(persistenceMermaidContainer, () => renderPersistenceMermaid(group, persistenceMermaidContainer));

        container.appendChild(createElement("section", {
            className: "outputs-port-card",
            id: targetId,
            children: [
                createElement("h3", { textContent: group.target }),
                persistenceMermaidContainer
            ]
        }));
    });

    if (sidebarList && grouped.length > 0) {
        sidebar.appendChild(sidebarList);
    }

    if (grouped.length === 0) {
        renderNoData(container);
    }
}

function renderOutputsTable(grouped, mode = 'standard') {
    const container = document.getElementById("outputs-list");
    const sidebar = document.getElementById("outputs-sidebar-list");
    if (!container) return;
    container.innerHTML = "";
    if (sidebar) sidebar.innerHTML = "";

    const sidebarList = sidebar ? createElement("ul") : null;

    grouped.forEach(group => {
        const portFqnValue = group.outputPort.fqn ?? "";
        const portId = "port-" + portFqnValue.replace(/[^a-zA-Z0-9]/g, '-');
        const portLabel = group.outputPort.label ?? group.outputPort.fqn ?? "(unknown)";
        addSidebarItem(sidebarList, portId, portLabel);

        const cardChildren = [
            createElement("h3", { textContent: portLabel }),
            createElement("p", {
                className: "fully-qualified-name",
                textContent: portFqnValue
            })
        ];

        if (mode !== 'simple') {
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

        const portMermaidContainer = createElement("div", { className: "mermaid-diagram port-diagram" });
        lazyRender(portMermaidContainer, () => renderPortMermaid(group, portMermaidContainer, mode));
        cardChildren.push(portMermaidContainer);

        const itemList = createElement("div", { className: "outputs-item-list" });
        group.links.forEach(link => {
            const mermaidContainer = createElement("div", { className: "mermaid-diagram" });
            lazyRender(mermaidContainer, () => renderMermaid(link, mermaidContainer, mode));

            itemList.appendChild(createElement("article", {
                className: "outputs-item",
                children: [
                    createElement("h4", { textContent: link.outputPortOperation?.name ?? link.outputPortOperation?.signature ?? "" }),
                    mermaidContainer,
                    createElement("p", {
                        className: "outputs-persistence-title",
                        textContent: "永続化操作詳細"
                    }),
                    createElement("ul", {
                        className: "outputs-persistence-list",
                        children: formatPersistenceOperations(link.persistenceOperations).map(text => createElement("li", { textContent: text }))
                    })
                ]
            }));
        });
        cardChildren.push(itemList);

        container.appendChild(createElement("section", {
            className: "outputs-port-card",
            id: portId,
            children: cardChildren
        }));
    });

    if (sidebarList && grouped.length > 0) {
        sidebar.appendChild(sidebarList);
    }

    if (grouped.length === 0) {
        renderNoData(container);
    }
}

if (typeof window !== "undefined" && typeof document !== "undefined") {
    window.addEventListener("DOMContentLoaded", () => {
        initMermaid();
        const data = getOutputsData();
        const grouped = groupLinksByOutputPort(data.links);
        const persistenceGrouped = groupLinksByPersistenceTarget(data.links);

        const render = () => {
            const mode = document.querySelector('input[name="display-mode"]:checked')?.value || 'standard';
            renderPersistenceTable(persistenceGrouped);
            renderOutputsTable(grouped, mode);
            renderCrudTable(data.links);
        };

        document.querySelectorAll('input[name="display-mode"]').forEach(input => {
            input.addEventListener('change', render);
        });

        document.querySelectorAll('.outputs-tabs .tab-button').forEach(button => {
            button.addEventListener('click', () => {
                const tabName = button.getAttribute('data-tab');
                document.querySelectorAll('.outputs-tabs .tab-button').forEach(btn => btn.classList.remove('is-active'));
                document.querySelectorAll('.outputs-tab-panel').forEach(panel => panel.classList.remove('is-active'));
                
                button.classList.add('is-active');
                document.getElementById(`${tabName}-tab-panel`).classList.add('is-active');
            });
        });

        render();
    });
}

    if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        getOutputsData,
        groupLinksByOutputPort,
        groupLinksByPersistenceTarget,
        formatPersistenceOperations,
        createField,
        renderOutputsTable,
        renderPersistenceTable,
        renderCrudTable,
        toCrudChar,
        createElement,
        renderNoData,
        generateMermaidCode,
        generatePortMermaidCode,
        generatePersistenceMermaidCode,
        MermaidBuilder,
    };
}
