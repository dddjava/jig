(() => {
    const Jig = globalThis.Jig;

    const selectedGroupIds = new Set();
    const groupIdByNodeId = new Map();
    let onSelectionChanged = () => {};

    function init() {
        const data = globalThis.externalDependencyData || {
            internalPackages: [],
            externalGroups: [],
            relations: []
        };

        const diagramEl = document.getElementById("external-dependency-diagram");
        const javaStandardToggle = document.getElementById("show-java-standard-toggle");
        const depthSelect = document.getElementById("package-depth-select");
        const depthUp = document.getElementById("depth-up-button");
        const depthDown = document.getElementById("depth-down-button");
        const clearSelection = document.getElementById("clear-selection-button");
        const tableBody = document.querySelector("#external-group-table tbody");

        const maxDepth = computeMaxDepth(data.internalPackages);
        const initialDepth = computeInitialDepth(data.internalPackages, maxDepth);
        populateDepthSelect(depthSelect, maxDepth, initialDepth);
        updateDepthButtonStates(depthSelect, depthUp, depthDown);

        const groupsById = new Map((data.externalGroups || []).map(g => [g.id, g]));

        // 向き変更ボタンで切り替えた direction を保持し、再描画時に再適用する。
        // 初期値は TB（renderWithControls は未指定時に diagramFn("LR") を呼んで先頭から
        // direction を抽出するため、ここで明示しないと LR になってしまう）
        let currentDirection = "TB";
        const diagramFn = (direction) => {
            currentDirection = direction;
            const includeJavaStandard = !!(javaStandardToggle && javaStandardToggle.checked);
            const depth = depthSelect && depthSelect.value !== "" ? Number(depthSelect.value) : maxDepth;
            return buildMermaidText(data, groupsById, depth, includeJavaStandard, selectedGroupIds, direction);
        };
        const renderDiagram = () => {
            if (!diagramEl) return;
            Jig.mermaid.render.renderWithControls(diagramEl, diagramFn, {direction: currentDirection});
        };

        // Mermaid のクリックハンドラ。グローバル関数として登録する必要がある。
        // 引数は sanitize 済みのノード ID なので、元の group.id へ逆引きする。
        globalThis.handleExternalGroupClick = (nodeIdArg) => {
            const groupId = groupIdByNodeId.get(nodeIdArg);
            if (groupId) toggleSelection(groupId);
        };

        const rerender = () => {
            renderDiagram();
            highlightSelectedRows(tableBody);
        };
        onSelectionChanged = rerender;

        rerender();

        if (javaStandardToggle) javaStandardToggle.addEventListener("change", renderDiagram);
        if (depthSelect) depthSelect.addEventListener("change", () => {
            renderDiagram();
            updateDepthButtonStates(depthSelect, depthUp, depthDown);
        });
        // パッケージ関連と揃える: ▲は集約解除側（index-1）、▼は集約強化側（index+1）
        if (depthUp) depthUp.addEventListener("click", () => stepDepthByIndex(depthSelect, -1));
        if (depthDown) depthDown.addEventListener("click", () => stepDepthByIndex(depthSelect, +1));
        if (clearSelection) clearSelection.addEventListener("click", () => {
            selectedGroupIds.clear();
            onSelectionChanged();
        });

        if (tableBody) {
            renderGroupTable(tableBody, data.externalGroups || []);
            highlightSelectedRows(tableBody);
        }
    }

    function toggleSelection(groupId) {
        if (!groupId) return;
        if (selectedGroupIds.has(groupId)) selectedGroupIds.delete(groupId);
        else selectedGroupIds.add(groupId);
        onSelectionChanged();
    }

    function computeMaxDepth(internalPackages) {
        let max = 1;
        (internalPackages || []).forEach(fqn => {
            const d = fqn.split('.').length;
            if (d > max) max = d;
        });
        return max;
    }

    function populateDepthSelect(select, maxDepth, initialDepth) {
        if (!select) return;
        select.innerHTML = "";
        const noAgg = document.createElement("option");
        noAgg.value = "0";
        noAgg.textContent = "集約なし";
        select.appendChild(noAgg);
        for (let d = 1; d <= maxDepth; d++) {
            const opt = document.createElement("option");
            opt.value = String(d);
            opt.textContent = `深さ${d}`;
            select.appendChild(opt);
        }
        select.value = String(initialDepth != null ? initialDepth : maxDepth);
    }

    // 集約後の内部パッケージが複数になる最も浅い階層を選ぶ
    function computeInitialDepth(internalPackages, maxDepth) {
        const fqns = internalPackages || [];
        if (fqns.length <= 1) return maxDepth;
        for (let d = 1; d <= maxDepth; d++) {
            const aggregated = new Set(fqns.map(fqn => Jig.util.getAggregatedFqn(fqn, d) || fqn));
            if (aggregated.size >= 2) return d;
        }
        return maxDepth;
    }

    function stepDepthByIndex(select, delta) {
        if (!select) return;
        const options = Array.from(select.options);
        const currentIndex = options.findIndex(opt => opt.value === select.value);
        const nextIndex = currentIndex + delta;
        if (nextIndex < 0 || nextIndex >= options.length) return;
        select.value = options[nextIndex].value;
        select.dispatchEvent(new Event("change"));
    }

    function updateDepthButtonStates(select, upButton, downButton) {
        if (!select || !upButton || !downButton) return;
        const options = Array.from(select.options);
        const currentIndex = options.findIndex(opt => opt.value === select.value);
        upButton.disabled = currentIndex <= 0;
        downButton.disabled = currentIndex < 0 || currentIndex >= options.length - 1;
    }

    function buildMermaidText(data, groupsById, depth, includeJavaStandard, selected, direction) {
        const hasSelection = selected && selected.size > 0;
        const visibleEdges = [];
        const seen = new Set();
        const internalFqnsAggregated = new Set();
        const referencedGroupIds = new Set();
        (data.relations || []).forEach(rel => {
            const group = groupsById.get(rel.to);
            if (!group) return;
            if (!includeJavaStandard && group.isJavaStandard) return;
            if (hasSelection && !selected.has(group.id)) return;
            const aggregatedFrom = Jig.util.getAggregatedFqn(rel.from, depth) || rel.from;
            internalFqnsAggregated.add(aggregatedFrom);
            referencedGroupIds.add(group.id);
            const key = `${aggregatedFrom}::${rel.to}`;
            if (!seen.has(key)) {
                seen.add(key);
                visibleEdges.push({from: aggregatedFrom, to: rel.to});
            }
        });

        const lines = [`flowchart ${direction || "TB"}`];
        const parentSelfIds = [];
        const tree = buildPackageTree(internalFqnsAggregated);
        renderTreeChildren(tree, lines, 1, "", parentSelfIds);

        const visibleGroups = (data.externalGroups || [])
            .filter(g => includeJavaStandard || !g.isJavaStandard)
            .filter(g => !hasSelection || referencedGroupIds.has(g.id))
            .sort((a, b) => (a.isJavaStandard === b.isJavaStandard) ? 0 : a.isJavaStandard ? 1 : -1);
        visibleGroups.forEach(g => {
            const id = nodeId(g.id);
            groupIdByNodeId.set(id, g.id);
            lines.push(`    ${id}@{shape: doc, label: \"${escape(g.displayName)}\"}`);
            lines.push(`    click ${id} handleExternalGroupClick \"${escape(g.displayName)}\"`);
        });

        visibleEdges.forEach(e => {
            lines.push(`    ${nodeId(e.from)} --> ${nodeId(e.to)}`);
        });

        lines.push("    classDef parentPackage fill:#ffffce,stroke:#aaaa00,stroke-dasharray:10 3");
        parentSelfIds.forEach(id => lines.push(`    class ${id} parentPackage`));

        lines.push("    classDef selectedGroup fill:#cfe9ff,stroke:#1769aa,stroke-width:2px,font-weight:bold");
        visibleGroups
            .filter(g => selected && selected.has(g.id))
            .forEach(g => lines.push(`    class ${nodeId(g.id)} selectedGroup`));

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
            if (a.isJavaStandard !== b.isJavaStandard) return a.isJavaStandard ? 1 : -1;
            return a.displayName.localeCompare(b.displayName);
        });
        sorted.forEach(group => {
            const tr = document.createElement("tr");
            tr.dataset.groupId = group.id;
            tr.style.cursor = "pointer";
            tr.addEventListener("click", () => toggleSelection(group.id));

            const nameTd = document.createElement("td");
            nameTd.textContent = group.displayName;
            tr.appendChild(nameTd);

            const kindTd = document.createElement("td");
            kindTd.textContent = group.isJavaStandard ? "Java標準" : "外部ライブラリ";
            tr.appendChild(kindTd);

            const samplesTd = document.createElement("td");
            samplesTd.style.whiteSpace = "pre-line";
            samplesTd.textContent = (group.samplePackages || []).join("\n");
            tr.appendChild(samplesTd);

            const classesTd = document.createElement("td");
            const classes = group.usingClasses || [];
            if (classes.length > 0) {
                const details = document.createElement("details");
                // 行クリック→外部グループ選択 と干渉しないように分離
                details.addEventListener("click", e => e.stopPropagation());
                const summary = document.createElement("summary");
                summary.textContent = `${classes.length} 件`;
                summary.style.cursor = "pointer";
                details.appendChild(summary);
                const list = document.createElement("div");
                list.style.whiteSpace = "pre-line";
                list.textContent = classes.join("\n");
                details.appendChild(list);
                classesTd.appendChild(details);
            }
            tr.appendChild(classesTd);

            tbody.appendChild(tr);
        });
    }

    function highlightSelectedRows(tbody) {
        if (!tbody) return;
        tbody.querySelectorAll("tr").forEach(tr => {
            const isSelected = selectedGroupIds.has(tr.dataset.groupId);
            tr.classList.toggle("is-selected", isSelected);
            tr.style.background = isSelected ? "#cfe9ff" : "";
        });
    }

    if (typeof document !== "undefined") {
        document.addEventListener("DOMContentLoaded", init);
    }
})();
