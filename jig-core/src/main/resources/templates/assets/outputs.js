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

    link.persistenceOperations?.forEach((op, index) => {
        const sqlType = op.sqlType || "";
        const opId = `Op${index}`;
        op.targets?.forEach((target, tIndex) => {
            const targetId = `Target${index}_${tIndex}`;
            mermaidCode += `  Execution -- "${sqlType}" --> ${targetId}[(${target})]\n`;
        });
    });

    const id = "mermaid-" + Math.random().toString(36).substr(2, 9);
    mermaid.render(id, mermaidCode).then(({svg}) => {
        container.innerHTML = svg;
    });
}

function initMermaid() {
    if (typeof mermaid !== "undefined") {
        mermaid.initialize({ startOnLoad: false });
    }
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
            renderMermaid(link, mermaidContainer);

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
