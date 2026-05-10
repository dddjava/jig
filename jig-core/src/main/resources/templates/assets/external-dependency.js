(() => {
    const Jig = globalThis.Jig;

    function init() {
        const data = globalThis.externalDependencyData || {
            internalPackages: [],
            externalGroups: [],
            relations: [],
            mermaidTextWithJdk: "",
            mermaidTextWithoutJdk: ""
        };

        const diagramEl = document.getElementById("external-dependency-diagram");
        const toggle = document.getElementById("show-jdk-toggle");
        const tableBody = document.querySelector("#external-group-table tbody");

        if (diagramEl) {
            const renderDiagram = () => {
                const includeJdk = !!(toggle && toggle.checked);
                const text = includeJdk ? data.mermaidTextWithJdk : data.mermaidTextWithoutJdk;
                Jig.mermaid.render.renderWithControls(diagramEl, () => text);
            };
            renderDiagram();
            if (toggle) toggle.addEventListener("change", renderDiagram);
        }

        if (tableBody) {
            renderGroupTable(tableBody, data.externalGroups || []);
        }
    }

    function renderGroupTable(tbody, groups) {
        tbody.textContent = "";
        const sorted = [...groups].sort((a, b) => {
            if (a.isJdk !== b.isJdk) return a.isJdk ? 1 : -1;
            return a.displayName.localeCompare(b.displayName);
        });
        sorted.forEach(group => {
            const tr = document.createElement("tr");

            const nameTd = document.createElement("td");
            nameTd.textContent = group.displayName;
            tr.appendChild(nameTd);

            const kindTd = document.createElement("td");
            kindTd.textContent = group.isJdk ? "JDK" : "外部ライブラリ";
            tr.appendChild(kindTd);

            const samplesTd = document.createElement("td");
            samplesTd.textContent = (group.samplePackages || []).join(", ");
            tr.appendChild(samplesTd);

            tbody.appendChild(tr);
        });
    }

    if (typeof document !== "undefined") {
        document.addEventListener("DOMContentLoaded", init);
    }
})();
