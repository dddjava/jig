function getOutputsData() {
    const jsonText = document.getElementById("outputs-data")?.textContent || "{}";
    const data = JSON.parse(jsonText);

    const ports = data.ports || {};
    const operations = data.operations || {};
    const adapters = data.adapters || {};
    const executions = data.executions || {};
    const persistenceOperationsMaster = data.persistenceOperations || {};

    const links = (data.links || []).map(link => {
        const pOps = (link.persistenceOperations || []).map(id => persistenceOperationsMaster[id]);
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

function renderMermaid(link, container) {
    if (typeof mermaid === "undefined") return;

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
  subgraph "${adapterLabel}"
    Execution["${executionName}"]
  end
  PortOp --> Execution
`;

    const edgeSet = new Set();
    const targetNodes = new Map();
    link.persistenceOperations?.forEach((op) => {
        const sqlType = op.sqlType || "";
        op.targets?.forEach((target) => {
            if (!targetNodes.has(target)) {
                const targetId = `Target_${targetNodes.size}`;
                targetNodes.set(target, targetId);
                mermaidCode += `  ${targetId}[(${target})]\n`;
            }
            const targetId = targetNodes.get(target);
            const edgeKey = `${sqlType}->${targetId}`;
            if (!edgeSet.has(edgeKey)) {
                mermaidCode += `  Execution -- "${sqlType}" --> ${targetId}\n`;
                edgeSet.add(edgeKey);
            }
        });
    });

    const id = "mermaid-" + Math.random().toString(36).substr(2, 9);
    mermaid.render(id, mermaidCode).then(({svg}) => {
        container.innerHTML = svg;
    });
}

function renderPortMermaid(group, container) {
    if (typeof mermaid === "undefined") return;

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
    const targetNodes = new Map();
    const edgeSet = new Set();

    group.links.forEach((link, linkIndex) => {
        const adapterFqn = link.outputAdapter?.fqn || `Adapter_${linkIndex}`;
        const adapterLabel = link.outputAdapter?.label || adapterFqn;
        const executionName = link.outputAdapterExecution?.name || link.outputAdapterExecution?.signature || `Execution_${linkIndex}`;
        const executionFqn = link.outputAdapterExecution?.fqn || `${adapterFqn}.${executionName}`;

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

        link.persistenceOperations?.forEach((op) => {
            const sqlType = op.sqlType || "";
            op.targets?.forEach((target) => {
                if (!targetNodes.has(target)) {
                    const targetId = `Target_${targetNodes.size}`;
                    targetNodes.set(target, targetId);
                }
                const targetId = targetNodes.get(target);
                const edgeKey = `${executionId}--${sqlType}-->${targetId}`;
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

    targetNodes.forEach((targetId, target) => {
        mermaidCode += `  ${targetId}[(${target})]\n`;
    });

    edgeSet.forEach((edgeKey) => {
        const match = edgeKey.match(/(.+)--(.+)-->(.+)/);
        if (match) {
            mermaidCode += `  ${match[1]} -- "${match[2]}" --> ${match[3]}\n`;
        }
    });

    const id = "mermaid-port-" + Math.random().toString(36).substr(2, 9);
    mermaid.render(id, mermaidCode).then(({svg}) => {
        container.innerHTML = svg;
    });
}

function renderCrudTable(links) {
    const container = document.getElementById("outputs-crud");
    if (!container) return;

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

    const table = document.createElement("table");
    table.className = "zebra crud-table";
    const thead = document.createElement("thead");
    const headerRow = document.createElement("tr");

    const opHeader = document.createElement("th");
    opHeader.textContent = "出力ポート / 操作";
    headerRow.appendChild(opHeader);

    allTargets.forEach(target => {
        const th = document.createElement("th");
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
                const sqlType = (op.sqlType || "").toUpperCase();
                let crud = "";
                if (sqlType === "SELECT") crud = "R";
                else if (sqlType === "INSERT") crud = "C";
                else if (sqlType === "UPDATE") crud = "U";
                else if (sqlType === "DELETE") crud = "D";

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
                const sqlType = (op.sqlType || "").toUpperCase();
                let crud = "";
                if (sqlType === "SELECT") crud = "R";
                else if (sqlType === "INSERT") crud = "C";
                else if (sqlType === "UPDATE") crud = "U";
                else if (sqlType === "DELETE") crud = "D";

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

function renderOutputsTable(grouped) {
    const container = document.getElementById("outputs-list");
    if (!container) return;
    container.innerHTML = "";

    grouped.forEach(group => {
        const groupCard = document.createElement("section");
        groupCard.className = "outputs-port-card";

        const title = document.createElement("h3");
        title.textContent = group.outputPort.label ?? group.outputPort.fqn ?? "(unknown)";
        groupCard.appendChild(title);

        const portFqn = document.createElement("p");
        portFqn.className = "fully-qualified-name";
        portFqn.textContent = group.outputPort.fqn ?? "";
        groupCard.appendChild(portFqn);

        const count = document.createElement("p");
        count.className = "weak";
        count.textContent = `${group.links.length} operations`;
        groupCard.appendChild(count);

        const portMermaidContainer = document.createElement("div");
        portMermaidContainer.className = "mermaid-diagram port-diagram";
        groupCard.appendChild(portMermaidContainer);
        lazyRender(portMermaidContainer, () => renderPortMermaid(group, portMermaidContainer));

        const list = document.createElement("div");
        list.className = "outputs-item-list";

        group.links.forEach(link => {
            const item = document.createElement("article");
            item.className = "outputs-item";

            const operation = document.createElement("h4");
            operation.textContent = link.outputPortOperation?.name ?? link.outputPortOperation?.signature ?? "";
            item.appendChild(operation);

            const meta = document.createElement("dl");
            meta.className = "outputs-item-meta";
            meta.appendChild(createField("OutputAdapter", link.outputAdapter?.label ?? link.outputAdapter?.fqn ?? ""));
            meta.appendChild(createField("Execution", link.outputAdapterExecution?.name ?? link.outputAdapterExecution?.signature ?? ""));
            item.appendChild(meta);

            const persistenceTitle = document.createElement("p");
            persistenceTitle.className = "outputs-persistence-title";
            persistenceTitle.textContent = "PersistenceOperation";
            item.appendChild(persistenceTitle);

            const persistenceList = document.createElement("ul");
            persistenceList.className = "outputs-persistence-list";
            formatPersistenceOperations(link.persistenceOperations).forEach(text => {
                const line = document.createElement("li");
                line.textContent = text;
                persistenceList.appendChild(line);
            });
            item.appendChild(persistenceList);

            const mermaidContainer = document.createElement("div");
            mermaidContainer.className = "mermaid-diagram";
            item.appendChild(mermaidContainer);
            lazyRender(mermaidContainer, () => renderMermaid(link, mermaidContainer));

            list.appendChild(item);
        });

        groupCard.appendChild(list);
        container.appendChild(groupCard);
    });

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
        renderCrudTable(data.links);
        const grouped = groupLinksByOutputPort(data.links);
        renderOutputsTable(grouped);
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        getOutputsData,
        groupLinksByOutputPort,
        formatPersistenceOperations,
        renderOutputsTable,
    };
}
