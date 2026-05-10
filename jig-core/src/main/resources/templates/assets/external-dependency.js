(() => {
    const Jig = globalThis.Jig;

    function init() {
        const data = globalThis.externalDependencyData || {
            internalPackages: [],
            externalGroups: [],
            relations: []
        };

        const diagramEl = document.getElementById("external-dependency-diagram");
        const jdkToggle = document.getElementById("show-jdk-toggle");
        const depthSelect = document.getElementById("package-depth-select");
        const depthUp = document.getElementById("depth-up-button");
        const depthDown = document.getElementById("depth-down-button");
        const tableBody = document.querySelector("#external-group-table tbody");

        const maxDepth = computeMaxDepth(data.internalPackages);
        populateDepthSelect(depthSelect, maxDepth);

        const groupsById = new Map((data.externalGroups || []).map(g => [g.id, g]));

        const renderDiagram = () => {
            if (!diagramEl) return;
            const includeJdk = !!(jdkToggle && jdkToggle.checked);
            const depth = Number(depthSelect && depthSelect.value) || maxDepth;
            const text = buildMermaidText(data, groupsById, depth, includeJdk);
            Jig.mermaid.render.renderWithControls(diagramEl, () => text);
        };
        renderDiagram();

        if (jdkToggle) jdkToggle.addEventListener("change", renderDiagram);
        if (depthSelect) depthSelect.addEventListener("change", renderDiagram);
        if (depthUp) depthUp.addEventListener("click", () => stepDepth(depthSelect, +1, renderDiagram));
        if (depthDown) depthDown.addEventListener("click", () => stepDepth(depthSelect, -1, renderDiagram));

        if (tableBody) renderGroupTable(tableBody, data.externalGroups || []);
    }

    function computeMaxDepth(internalPackages) {
        let max = 1;
        (internalPackages || []).forEach(fqn => {
            const d = fqn.split('.').length;
            if (d > max) max = d;
        });
        return max;
    }

    function populateDepthSelect(select, maxDepth) {
        if (!select) return;
        for (let d = 1; d <= maxDepth; d++) {
            const opt = document.createElement("option");
            opt.value = String(d);
            opt.textContent = String(d);
            select.appendChild(opt);
        }
        select.value = String(maxDepth);
    }

    function stepDepth(select, delta, onChange) {
        if (!select) return;
        const current = Number(select.value) || 1;
        const next = current + delta;
        const min = Number(select.firstChild && select.firstChild.value) || 1;
        const max = Number(select.lastChild && select.lastChild.value) || current;
        if (next < min || next > max) return;
        select.value = String(next);
        onChange();
    }

    function buildMermaidText(data, groupsById, depth, includeJdk) {
        const visibleEdges = [];
        const seen = new Set();
        const internalFqnsAggregated = new Set();
        (data.relations || []).forEach(rel => {
            const group = groupsById.get(rel.to);
            if (!group) return;
            if (!includeJdk && group.isJdk) return;
            const aggregatedFrom = Jig.util.getAggregatedFqn(rel.from, depth) || rel.from;
            internalFqnsAggregated.add(aggregatedFrom);
            const key = `${aggregatedFrom}::${rel.to}`;
            if (!seen.has(key)) {
                seen.add(key);
                visibleEdges.push({from: aggregatedFrom, to: rel.to});
            }
        });

        const lines = ["flowchart LR"];
        const parentSelfIds = [];
        const tree = buildPackageTree(internalFqnsAggregated);
        renderTreeChildren(tree, lines, 1, "", parentSelfIds);

        const visibleGroups = (data.externalGroups || [])
            .filter(g => includeJdk || !g.isJdk)
            .sort((a, b) => (a.isJdk === b.isJdk) ? 0 : a.isJdk ? 1 : -1);
        visibleGroups.forEach(g => {
            lines.push(`    ${nodeId(g.id)}([\"${escape(g.displayName)}\"])`);
        });

        visibleEdges.forEach(e => {
            lines.push(`    ${nodeId(e.from)} --> ${nodeId(e.to)}`);
        });

        lines.push("    classDef parentPackage fill:#ffffce,stroke:#aaaa00,stroke-dasharray:10 3");
        parentSelfIds.forEach(id => lines.push(`    class ${id} parentPackage`));

        return lines.join("\n") + "\n";
    }

    function buildPackageTree(fqns) {
        const root = {fqn: "", children: new Map(), isLeaf: false};
        [...fqns].sort().forEach(fqn => {
            const parts = fqn.split('.');
            let current = root;
            let path = "";
            parts.forEach(part => {
                path = path ? `${path}.${part}` : part;
                if (!current.children.has(path)) {
                    current.children.set(path, {fqn: path, children: new Map(), isLeaf: false});
                }
                current = current.children.get(path);
            });
            current.isLeaf = true;
        });
        return root;
    }

    function renderTreeChildren(node, lines, indent, parentFqn, parentSelfIds) {
        node.children.forEach(child => renderTreeNode(child, lines, indent, parentFqn, parentSelfIds));
    }

    function renderTreeNode(node, lines, indent, parentFqn, parentSelfIds) {
        const label = (parentFqn && node.fqn.startsWith(parentFqn + '.'))
            ? node.fqn.substring(parentFqn.length + 1)
            : node.fqn;
        if (node.children.size === 0) {
            lines.push(`${pad(indent)}${nodeId(node.fqn)}@{shape: st-rect, label: \"${escape(label)}\"}`);
            return;
        }
        if (!node.isLeaf && node.children.size === 1) {
            const onlyChild = node.children.values().next().value;
            renderTreeNode(onlyChild, lines, indent, parentFqn, parentSelfIds);
            return;
        }
        const groupId = `${nodeId(node.fqn)}_grp`;
        lines.push(`${pad(indent)}subgraph ${groupId} [\"${escape(label)}\"]`);
        if (node.isLeaf) {
            const selfId = nodeId(node.fqn);
            lines.push(`${pad(indent + 1)}${selfId}@{shape: st-rect, label: \"${escape(node.fqn)}\"}`);
            parentSelfIds.push(selfId);
        }
        renderTreeChildren(node, lines, indent + 1, node.fqn, parentSelfIds);
        lines.push(`${pad(indent)}end`);
    }

    function nodeId(text) {
        return "n_" + (text || "").replace(/[^A-Za-z0-9]/g, "_");
    }

    function escape(text) {
        return (text || "").replace(/"/g, '\\"');
    }

    function pad(level) {
        return "    ".repeat(level);
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
