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
    const field = document.createElement("div");
    field.className = "outputs-item-field";

    const labelElement = document.createElement("dt");
    labelElement.textContent = label;
    const valueElement = document.createElement("dd");
    valueElement.textContent = value;

    field.appendChild(labelElement);
    field.appendChild(valueElement);
    return field;
}

function generateMermaidCode(link, mode = 'standard') {
    const portFqn = link.outputPort?.fqn || "Port";
    const portLabel = link.outputPort?.label || portFqn;
    const portOpName = link.outputPortOperation?.name || link.outputPortOperation?.signature || "Operation";

    const adapterFqn = link.outputAdapter?.fqn || "Adapter";
    const adapterLabel = link.outputAdapter?.label || adapterFqn;
    const executionName = link.outputAdapterExecution?.name || link.outputAdapterExecution?.signature || "Execution";

    let mermaidCode = `graph LR
  subgraph "${portLabel}"
    PortOp["${portOpName}"]
  end
`;

    let lastNode = "PortOp";

    if (mode !== 'simple') {
        mermaidCode += `  subgraph "${adapterLabel}"
    Execution["${executionName}"]
  end
  PortOp --> Execution
`;
        lastNode = "Execution";
    }

    const edgeSet = new Set();
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
                mermaidCode += `  subgraph "${groupLabel}"
    ${groupNodeId}["${op.id.split('.').pop()}"]
  end
  ${lastNode} --> ${groupNodeId}
`;
            }
            currentLastNode = groupNodes.get(groupId);
        }

        op.targets?.forEach((target) => {
            if (!targetNodes.has(target)) {
                const targetId = `Target_${targetNodes.size}`;
                targetNodes.set(target, targetId);
                mermaidCode += `  ${targetId}[(${target})]\n`;
            }
            const targetId = targetNodes.get(target);
            const edgeKey = `${currentLastNode}--${sqlType}-->${targetId}`;
            if (!edgeSet.has(edgeKey)) {
                mermaidCode += `  ${currentLastNode} -- "${sqlType}" --> ${targetId}\n`;
                edgeSet.add(edgeKey);
            }
        });
    });

    return mermaidCode;
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
    const portFqn = group.outputPort?.fqn || "Port";
    const portLabel = group.outputPort?.label || portFqn;

    let mermaidCode = `graph LR\n`;
    mermaidCode += `  subgraph "${portLabel}"\n`;
    group.links.forEach((link, index) => {
        const portOpName = link.outputPortOperation?.name || link.outputPortOperation?.signature || `Operation_${index}`;
        mermaidCode += `    PortOp_${index}["${portOpName}"]\n`;
    });
    mermaidCode += `  end\n`;

    const adapterNodes = new Map();
    const executionNodes = new Map();
    const groupNodes = new Map();
    const targetNodes = new Map();
    const edgeSet = new Set();

    group.links.forEach((link, linkIndex) => {
        const adapterFqn = link.outputAdapter?.fqn || `Adapter_${linkIndex}`;
        const adapterLabel = link.outputAdapter?.label || adapterFqn;
        const executionName = link.outputAdapterExecution?.name || link.outputAdapterExecution?.signature || `Execution_${linkIndex}`;
        const executionFqn = link.outputAdapterExecution?.fqn || `${adapterFqn}.${executionName}`;

        let lastNodeId = `PortOp_${linkIndex}`;

        if (mode !== 'simple') {
            if (!adapterNodes.has(adapterFqn)) {
                adapterNodes.set(adapterFqn, {
                    label: adapterLabel,
                    executions: new Map()
                });
            }
            const adapter = adapterNodes.get(adapterFqn);
            if (!adapter.executions.has(executionFqn)) {
                const executionId = `Execution_${executionNodes.size}`;
                adapter.executions.set(executionFqn, {
                    id: executionId,
                    name: executionName
                });
                executionNodes.set(executionFqn, executionId);
            }
            const executionId = executionNodes.get(executionFqn);
            mermaidCode += `  PortOp_${linkIndex} --> ${executionId}\n`;
            lastNodeId = executionId;
        }

        link.persistenceOperations?.forEach((op) => {
            const sqlType = op.sqlType || "";
            const groupId = op.group;
            const groupLabel = op.groupLabel;

            let currentLastNodeId = lastNodeId;

            if (mode === 'detailed' && groupId) {
                const groupKey = `${groupId}`;
                if (!groupNodes.has(groupKey)) {
                    const groupNodeId = `Group_${groupNodes.size}`;
                    groupNodes.set(groupKey, {
                        id: groupNodeId,
                        label: groupLabel,
                        operations: new Map()
                    });
                }
                const g = groupNodes.get(groupKey);
                if (!g.operations.has(op.id)) {
                    const opNodeId = `POp_${op.id.replace(/[^a-zA-Z0-9]/g, '_')}`;
                    g.operations.set(op.id, opNodeId);
                }
                const opNodeId = g.operations.get(op.id);
                const edgeKey = `${lastNodeId}-->${opNodeId}`;
                if (!edgeSet.has(edgeKey)) {
                    mermaidCode += `  ${lastNodeId} --> ${opNodeId}\n`;
                    edgeSet.add(edgeKey);
                }
                currentLastNodeId = opNodeId;
            }

            op.targets?.forEach((target) => {
                if (!targetNodes.has(target)) {
                    const targetId = `Target_${targetNodes.size}`;
                    targetNodes.set(target, targetId);
                }
                const targetId = targetNodes.get(target);
                const edgeKey = `${currentLastNodeId}--${sqlType}-->${targetId}`;
                if (!edgeSet.has(edgeKey)) {
                    edgeSet.add(edgeKey);
                }
            });
        });
    });

    adapterNodes.forEach((adapter, adapterFqn) => {
        mermaidCode += `  subgraph "${adapter.label}"\n`;
        adapter.executions.forEach((execution) => {
            mermaidCode += `    ${execution.id}["${execution.name}"]\n`;
        });
        mermaidCode += `  end\n`;
    });

    groupNodes.forEach((g) => {
        mermaidCode += `  subgraph "${g.label}"\n`;
        g.operations.forEach((nodeId, opId) => {
            mermaidCode += `    ${nodeId}["${opId.split('.').pop()}"]\n`;
        });
        mermaidCode += `  end\n`;
    });

    targetNodes.forEach((targetId, target) => {
        mermaidCode += `  ${targetId}[(${target})]\n`;
    });

    edgeSet.forEach((edgeKey) => {
        const match = edgeKey.match(/(.+)--(.+)-->(.+)/);
        if (match) {
            mermaidCode += `  ${match[1]} -- "${match[2]}" --> ${match[3]}\n`;
        }
    });

    return mermaidCode;
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
    const sidebar = document.getElementById("crud-sidebar");
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
            const title = document.createElement("p");
            title.className = "sidebar-title";
            title.textContent = "永続化操作対象";
            sidebarList.appendChild(title);
            
            allTargets.forEach(target => {
                const link = document.createElement("a");
                link.setAttribute("href", `#crud-target-${target}`);
                link.textContent = target;
                link.className = "sidebar-link";
                sidebarList.appendChild(link);
            });
        }

    const table = document.createElement("table");
    table.className = "zebra crud-table";
    const thead = document.createElement("thead");
    const headerRow = document.createElement("tr");

    const opHeader = document.createElement("th");
    opHeader.textContent = "出力ポート / 操作";
    headerRow.appendChild(opHeader);

    allTargets.forEach(target => {
        const th = document.createElement("th");
        th.id = `crud-target-${target}`;
        th.textContent = target;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement("tbody");
    const grouped = groupLinksByOutputPort(links);

    grouped.forEach(group => {
        const portId = "port-" + Math.random().toString(36).substr(2, 9);
        const portRow = document.createElement("tr");
        portRow.className = "port-group-row";
        portRow.style.cursor = "pointer";

        const portCell = document.createElement("td");
        portCell.textContent = group.outputPort.label || group.outputPort.fqn || "(unknown)";
        portCell.className = "port-group-cell";
        // 子要素の数（操作数）を表示
        const countSpan = document.createElement("span");
        countSpan.className = "weak";
        countSpan.style.marginLeft = "8px";
        countSpan.textContent = `(${group.links.length})`;
        portCell.appendChild(countSpan);

        portRow.appendChild(portCell);

        // ポート単位のCRUDを集計
        const portTargetCrudMap = new Map();
        group.links.forEach(link => {
            link.persistenceOperations?.forEach(op => {
                const crud = toCrudChar(op.sqlType);

                if (crud) {
                    op.targets?.forEach(target => {
                        const current = portTargetCrudMap.get(target) || new Set();
                        current.add(crud);
                        portTargetCrudMap.set(target, current);
                    });
                }
            });
        });

        allTargets.forEach(target => {
            const td = document.createElement("td");
            td.className = "crud-cell port-crud-cell";
            const cruds = portTargetCrudMap.get(target);
            if (cruds) {
                td.textContent = Array.from(cruds).sort().join("");
            }
            portRow.appendChild(td);
        });
        tbody.appendChild(portRow);

        // 操作行の作成
        const opRows = [];
        group.links.forEach(link => {
            const row = document.createElement("tr");
            row.className = `operation-row ${portId}`;
            row.style.display = "none"; // デフォルト非表示

            const opCell = document.createElement("td");
            opCell.className = "operation-cell";
            const opName = link.outputPortOperation?.name || link.outputPortOperation?.signature || "";
            opCell.textContent = opName;
            row.appendChild(opCell);

            const targetCrudMap = new Map();
            link.persistenceOperations?.forEach(op => {
                const crud = toCrudChar(op.sqlType);

                if (crud) {
                    op.targets?.forEach(target => {
                        const current = targetCrudMap.get(target) || new Set();
                        current.add(crud);
                        targetCrudMap.set(target, current);
                    });
                }
            });

            allTargets.forEach(target => {
                const td = document.createElement("td");
                td.className = "crud-cell";
                const cruds = targetCrudMap.get(target);
                if (cruds) {
                    td.textContent = Array.from(cruds).sort().join("");
                }
                row.appendChild(td);
            });
            tbody.appendChild(row);
            opRows.push(row);
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

    table.appendChild(tbody);
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
    const target = group.target;
    let mermaidCode = `graph RL\n`;
    mermaidCode += `  Target[("${target}")]\n`;

    const groupNodes = new Map();
    const opNodes = new Map();
    const adapterNodes = new Map();
    const portNodes = new Map();
    const edgeSet = new Set();

    group.links.forEach((link, linkIndex) => {
        // Find persistence operations that touch this target
        const relevantOps = link.persistenceOperations.filter(op => op.targets.includes(target));

        relevantOps.forEach(op => {
            const opNodeId = `POp_${op.id.replace(/[^a-zA-Z0-9]/g, '_')}`;
            if (!opNodes.has(op.id)) {
                opNodes.set(op.id, {
                    id: opNodeId,
                    label: op.id.split('.').pop(),
                    sqlType: op.sqlType,
                    group: op.group,
                    groupLabel: op.groupLabel
                });
            }

            const edgeKey1 = `${opNodeId}--${op.sqlType}-->Target`;
            if (!edgeSet.has(edgeKey1)) {
                mermaidCode += `  ${opNodeId} -- "${op.sqlType}" --> Target\n`;
                edgeSet.add(edgeKey1);
            }

            const adapterFqn = link.outputAdapter?.fqn || `Adapter_${linkIndex}`;
            const adapterLabel = link.outputAdapter?.label || adapterFqn;
            const executionName = link.outputAdapterExecution?.name || link.outputAdapterExecution?.signature || `Execution_${linkIndex}`;
            const executionFqn = link.outputAdapterExecution?.fqn || `${adapterFqn}.${executionName}`;
            const executionId = `Execution_${executionFqn.replace(/[^a-zA-Z0-9]/g, '_')}`;

            if (!adapterNodes.has(adapterFqn)) {
                adapterNodes.set(adapterFqn, {
                    label: adapterLabel,
                    executions: new Map()
                });
            }
            const adapter = adapterNodes.get(adapterFqn);
            if (!adapter.executions.has(executionFqn)) {
                adapter.executions.set(executionFqn, {
                    id: executionId,
                    name: executionName
                });
            }

            const edgeKey2 = `${executionId}-->${opNodeId}`;
            if (!edgeSet.has(edgeKey2)) {
                mermaidCode += `  ${executionId} --> ${opNodeId}\n`;
                edgeSet.add(edgeKey2);
            }

            const portFqn = link.outputPort?.fqn || `Port_${linkIndex}`;
            const portLabel = link.outputPort?.label || portFqn;
            const portId = `Port_${portFqn.replace(/[^a-zA-Z0-9]/g, '_')}`;
            const portOpName = link.outputPortOperation?.name || link.outputPortOperation?.signature || `PortOp_${linkIndex}`;
            const portOpFqn = link.outputPortOperation?.fqn || `${portFqn}.${portOpName}`;
            const portOpId = `PortOp_${portOpFqn.replace(/[^a-zA-Z0-9]/g, '_')}`;

            if (!portNodes.has(portFqn)) {
                portNodes.set(portFqn, {
                    id: portId,
                    label: portLabel,
                    operations: new Map()
                });
            }
            const port = portNodes.get(portFqn);
            if (!port.operations.has(portOpFqn)) {
                port.operations.set(portOpFqn, {
                    id: portOpId,
                    name: portOpName
                });
            }

            const edgeKey3 = `${portOpId}-->${executionId}`;
            if (!edgeSet.has(edgeKey3)) {
                mermaidCode += `  ${portOpId} --> ${executionId}\n`;
                edgeSet.add(edgeKey3);
            }
        });
    });

    // Group persistence operations by their group (class)
    const persistenceGroups = new Map();
    opNodes.forEach((op) => {
        const groupId = op.group || 'default';
        if (!persistenceGroups.has(groupId)) {
            persistenceGroups.set(groupId, {
                label: op.groupLabel || 'Persistence Operations',
                ops: []
            });
        }
        persistenceGroups.get(groupId).ops.push(op);
    });

    persistenceGroups.forEach((pg) => {
        mermaidCode += `  subgraph "${pg.label}"\n`;
        pg.ops.forEach((op) => {
            mermaidCode += `    ${op.id}["${op.label}"]\n`;
        });
        mermaidCode += `  end\n`;
    });

    adapterNodes.forEach((adapter) => {
        mermaidCode += `  subgraph "${adapter.label}"\n`;
        adapter.executions.forEach((execution) => {
            mermaidCode += `    ${execution.id}["${execution.name}"]\n`;
        });
        mermaidCode += `  end\n`;
    });

    portNodes.forEach((port) => {
        mermaidCode += `  subgraph "${port.label}"\n`;
        port.operations.forEach((op) => {
            mermaidCode += `    ${op.id}["${op.name}"]\n`;
        });
        mermaidCode += `  end\n`;
    });

    return mermaidCode;
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

    const sidebarList = document.createElement("ul");

    grouped.forEach(group => {
        const targetId = "persistence-" + group.target.replace(/[^a-zA-Z0-9]/g, '-');

        const groupCard = document.createElement("section");
        groupCard.className = "outputs-port-card";
        groupCard.id = targetId;

        const title = document.createElement("h3");
        title.textContent = group.target;
        groupCard.appendChild(title);

        if (sidebar) {
            const sidebarItem = document.createElement("li");
            const sidebarLink = document.createElement("a");
            sidebarLink.setAttribute("href", "#" + targetId);
            sidebarLink.textContent = group.target;
            sidebarItem.appendChild(sidebarLink);
            sidebarList.appendChild(sidebarItem);
        }

        const persistenceMermaidContainer = document.createElement("div");
        persistenceMermaidContainer.className = "mermaid-diagram port-diagram";
        groupCard.appendChild(persistenceMermaidContainer);
        lazyRender(persistenceMermaidContainer, () => renderPersistenceMermaid(group, persistenceMermaidContainer));

        container.appendChild(groupCard);
    });

    if (sidebar && grouped.length > 0) {
        sidebar.appendChild(sidebarList);
    }

    if (grouped.length === 0) {
        const noData = document.createElement("p");
        noData.className = "weak";
        noData.textContent = "データなし";
        container.appendChild(noData);
    }
}

function renderOutputsTable(grouped, mode = 'standard') {
    const container = document.getElementById("outputs-list");
    const sidebar = document.getElementById("outputs-sidebar-list");
    if (!container) return;
    container.innerHTML = "";
    if (sidebar) sidebar.innerHTML = "";

    const sidebarList = document.createElement("ul");

    grouped.forEach(group => {
        const portFqnValue = group.outputPort.fqn ?? "";
        const portId = "port-" + portFqnValue.replace(/[^a-zA-Z0-9]/g, '-');

        const groupCard = document.createElement("section");
        groupCard.className = "outputs-port-card";
        groupCard.id = portId;

        const portLabel = group.outputPort.label ?? group.outputPort.fqn ?? "(unknown)";
        const title = document.createElement("h3");
        title.textContent = portLabel;
        groupCard.appendChild(title);

        if (sidebar) {
            const sidebarItem = document.createElement("li");
            const sidebarLink = document.createElement("a");
            sidebarLink.setAttribute("href", "#" + portId);
            sidebarLink.textContent = portLabel;
            sidebarItem.appendChild(sidebarLink);
            sidebarList.appendChild(sidebarItem);
        }

        const portFqn = document.createElement("p");
        portFqn.className = "fully-qualified-name";
        portFqn.textContent = group.outputPort.fqn ?? "";
        groupCard.appendChild(portFqn);

        if (mode !== 'simple') {
            const adapterLabels = Array.from(new Set(group.links.map(link => {
                const label = link.outputAdapter?.label ?? link.outputAdapter?.fqn ?? "";
                const fqn = link.outputAdapter?.fqn ?? "";
                return label + (label !== fqn ? ` (${fqn})` : "");
            })));
            if (adapterLabels.length > 0) {
                const adapterInfo = document.createElement("p");
                adapterInfo.className = "weak";
                adapterInfo.textContent = "Implementation: " + adapterLabels.join(", ");
                groupCard.appendChild(adapterInfo);
            }
        }

        const count = document.createElement("p");
        count.className = "weak";
        count.textContent = `${group.links.length} operations`;
        groupCard.appendChild(count);

        const portMermaidContainer = document.createElement("div");
        portMermaidContainer.className = "mermaid-diagram port-diagram";
        groupCard.appendChild(portMermaidContainer);
        lazyRender(portMermaidContainer, () => renderPortMermaid(group, portMermaidContainer, mode));

        const list = document.createElement("div");
        list.className = "outputs-item-list";

        group.links.forEach(link => {
            const item = document.createElement("article");
            item.className = "outputs-item";

            const operation = document.createElement("h4");
            operation.textContent = link.outputPortOperation?.name ?? link.outputPortOperation?.signature ?? "";
            item.appendChild(operation);

            const mermaidContainer = document.createElement("div");
            mermaidContainer.className = "mermaid-diagram";
            item.appendChild(mermaidContainer);
            lazyRender(mermaidContainer, () => renderMermaid(link, mermaidContainer, mode));

            const persistenceTitle = document.createElement("p");
            persistenceTitle.className = "outputs-persistence-title";
            persistenceTitle.textContent = "永続化操作詳細";
            item.appendChild(persistenceTitle);

            const persistenceList = document.createElement("ul");
            persistenceList.className = "outputs-persistence-list";
            formatPersistenceOperations(link.persistenceOperations).forEach(text => {
                const line = document.createElement("li");
                line.textContent = text;
                persistenceList.appendChild(line);
            });
            item.appendChild(persistenceList);

            list.appendChild(item);
        });

        groupCard.appendChild(list);
        container.appendChild(groupCard);
    });

    if (sidebar && grouped.length > 0) {
        sidebar.appendChild(sidebarList);
    }

    if (grouped.length === 0) {
        const noData = document.createElement("p");
        noData.className = "weak";
        noData.textContent = "データなし";
        container.appendChild(noData);
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
        renderOutputsTable,
        renderPersistenceTable,
        renderCrudTable,
        toCrudChar,
        generateMermaidCode,
        generatePortMermaidCode,
        generatePersistenceMermaidCode,
    };
}
