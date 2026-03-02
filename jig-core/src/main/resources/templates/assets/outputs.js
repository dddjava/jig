function getOutputsData() {
    const jsonText = document.getElementById("outputs-data")?.textContent || "{}";
    /** @type {{links?: Array<{
     * outputPort?: {fqn?: string, label?: string},
     * outputPortOperation?: {fqn?: string, name?: string, signature?: string},
     * outputAdapter?: {fqn?: string, label?: string},
     * outputAdapterExecution?: {fqn?: string, name?: string, signature?: string},
     * persistenceOperations?: Array<{id?: string, sqlType?: string, targets?: string[]}>
     * }>} } */
    const data = JSON.parse(jsonText);
    return {
        links: Array.isArray(data.links) ? data.links : [],
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
    return Array.from(map.values()).sort((a, b) => {
        const left = a.outputPort.label ?? a.outputPort.fqn ?? "";
        const right = b.outputPort.label ?? b.outputPort.fqn ?? "";
        return left.localeCompare(right, "ja");
    });
}

function formatPersistenceOperations(persistenceOperations) {
    if (!Array.isArray(persistenceOperations) || persistenceOperations.length === 0) {
        return "なし";
    }
    return persistenceOperations
        .map(operation => {
            const id = operation.id ?? "";
            const sqlType = operation.sqlType ?? "";
            const targets = Array.isArray(operation.targets) ? operation.targets.join(", ") : "";
            return `${sqlType} ${id} [${targets}]`.trim();
        })
        .join("\n");
}

function renderOutputsTable(grouped) {
    const tbody = document.querySelector("#outputs-list tbody");
    if (!tbody) return;
    tbody.innerHTML = "";

    if (grouped.length === 0) {
        const row = document.createElement("tr");
        const cell = document.createElement("td");
        cell.textContent = "データなし";
        cell.setAttribute("colspan", "5");
        row.appendChild(cell);
        tbody.appendChild(row);
        return;
    }

    grouped.forEach(group => {
        group.links.forEach((link, index) => {
            const row = document.createElement("tr");

            if (index === 0) {
                const outputPortCell = document.createElement("td");
                outputPortCell.textContent = group.outputPort.label ?? group.outputPort.fqn ?? "";
                outputPortCell.setAttribute("rowspan", String(group.links.length));
                row.appendChild(outputPortCell);
            }

            const operationCell = document.createElement("td");
            operationCell.textContent = link.outputPortOperation?.signature ?? link.outputPortOperation?.name ?? "";
            row.appendChild(operationCell);

            const outputAdapterCell = document.createElement("td");
            outputAdapterCell.textContent = link.outputAdapter?.label ?? link.outputAdapter?.fqn ?? "";
            row.appendChild(outputAdapterCell);

            const outputAdapterExecutionCell = document.createElement("td");
            outputAdapterExecutionCell.textContent = link.outputAdapterExecution?.signature ?? link.outputAdapterExecution?.name ?? "";
            row.appendChild(outputAdapterExecutionCell);

            const persistenceOperationCell = document.createElement("td");
            persistenceOperationCell.textContent = formatPersistenceOperations(link.persistenceOperations);
            row.appendChild(persistenceOperationCell);

            tbody.appendChild(row);
        });
    });
}

if (typeof window !== "undefined" && typeof document !== "undefined") {
    window.addEventListener("DOMContentLoaded", () => {
        const data = getOutputsData();
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
