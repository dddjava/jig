const LibraryDependencyApp = (() => {
    const Jig = globalThis.Jig;

    const selectedLibraryIds = new Set();
    const libraryIdByNodeId = new Map();
    let onSelectionChanged = () => {};

    function init() {
        const data = Jig.data.library.get() || {
            internalPackages: [],
            libraries: [],
            relations: []
        };

        const diagramEl = document.getElementById("library-dependency-diagram");
        const javaStandardToggle = document.getElementById("show-java-standard-toggle");
        const depthSelect = document.getElementById("package-depth-select");
        const depthUp = document.getElementById("depth-up-button");
        const depthDown = document.getElementById("depth-down-button");
        const clearSelection = document.getElementById("clear-selection-button");
        const tableBody = document.querySelector("#library-table tbody");

        const maxDepth = computeMaxDepth(data.internalPackages);
        const initialDepth = computeInitialDepth(data.internalPackages, maxDepth);
        Jig.dom.depthControl.renderOptions(depthSelect, maxDepth, initialDepth);
        Jig.dom.depthControl.updateButtonStates(depthSelect, depthUp, depthDown);

        const librariesById = new Map((data.libraries || []).map(g => [g.id, g]));

        // 向き変更ボタンで切り替えた direction を保持し、再描画時に再適用する。
        // 初期値は TB（renderWithControls は未指定時に diagramFn("LR") を呼んで先頭から
        // direction を抽出するため、ここで明示しないと LR になってしまう）
        let currentDirection = "TB";
        const diagramFn = (direction, opts) => {
            currentDirection = direction;
            const includeJavaStandard = !!(javaStandardToggle && javaStandardToggle.checked);
            const depth = depthSelect && depthSelect.value !== "" ? Number(depthSelect.value) : maxDepth;
            const showPhysicalName = !!(opts && opts.showPhysicalName);
            return buildMermaidText(data, librariesById, depth, includeJavaStandard, selectedLibraryIds, direction, showPhysicalName);
        };
        const renderDiagram = () => {
            if (!diagramEl) return;
            Jig.mermaid.render.renderWithControls(diagramEl, diagramFn, {direction: currentDirection, enableLabelToggle: true});
        };

        // Mermaid のクリックハンドラ。グローバル関数として登録する必要がある。
        // 引数は sanitize 済みのノード ID なので、元の group.id へ逆引きする。
        globalThis.handleLibraryClick = (nodeIdArg) => {
            const groupId = libraryIdByNodeId.get(nodeIdArg);
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
            Jig.dom.depthControl.updateButtonStates(depthSelect, depthUp, depthDown);
        });
        // ▲は集約解除側（index-1）、▼は集約強化側（index+1）
        if (depthUp) depthUp.addEventListener("click", () => Jig.dom.depthControl.step(depthSelect, -1));
        if (depthDown) depthDown.addEventListener("click", () => Jig.dom.depthControl.step(depthSelect, +1));
        if (clearSelection) clearSelection.addEventListener("click", () => {
            selectedLibraryIds.clear();
            onSelectionChanged();
        });

        if (tableBody) {
            renderLibraryTable(tableBody, data.libraries || []);
            highlightSelectedRows(tableBody);
        }
    }

    function toggleSelection(groupId) {
        if (!groupId) return;
        if (selectedLibraryIds.has(groupId)) selectedLibraryIds.delete(groupId);
        else selectedLibraryIds.add(groupId);
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

    function buildMermaidText(data, librariesById, depth, includeJavaStandard, selected, direction, showPhysicalName) {
        const hasSelection = selected && selected.size > 0;
        const visibleEdges = [];
        const seen = new Set();
        const internalFqnsAggregated = new Set();
        const referencedLibraryIds = new Set();
        (data.relations || []).forEach(rel => {
            const group = librariesById.get(rel.to);
            if (!group) return;
            if (!includeJavaStandard && group.isJavaStandard) return;
            if (hasSelection && !selected.has(group.id)) return;
            const aggregatedFrom = Jig.util.getAggregatedFqn(rel.from, depth) || rel.from;
            internalFqnsAggregated.add(aggregatedFrom);
            referencedLibraryIds.add(group.id);
            const key = `${aggregatedFrom}::${rel.to}`;
            if (!seen.has(key)) {
                seen.add(key);
                visibleEdges.push({from: aggregatedFrom, to: rel.to});
            }
        });

        const lines = [`flowchart ${direction || "TB"}`];
        const parentSelfIds = [];
        const tree = buildPackageTree(internalFqnsAggregated);
        renderTreeChildren(tree, lines, 1, "", parentSelfIds, showPhysicalName);

        const visibleLibraries = (data.libraries || [])
            .filter(g => includeJavaStandard || !g.isJavaStandard)
            .filter(g => !hasSelection || referencedLibraryIds.has(g.id))
            .sort((a, b) => (a.isJavaStandard === b.isJavaStandard) ? 0 : a.isJavaStandard ? 1 : -1);
        visibleLibraries.forEach(g => {
            const id = nodeId(g.id);
            libraryIdByNodeId.set(id, g.id);
            lines.push(`    ${id}@{shape: doc, label: "${escape(g.displayName)}"}`);
            lines.push(`    click ${id} handleLibraryClick "${escape(g.displayName)}"`);
        });

        visibleEdges.forEach(e => {
            lines.push(`    ${nodeId(e.from)} --> ${nodeId(e.to)}`);
        });

        lines.push("    classDef parentPackage fill:#ffffce,stroke:#aaaa00,stroke-dasharray:10 3");
        parentSelfIds.forEach(id => lines.push(`    class ${id} parentPackage`));

        lines.push("    classDef selectedLibrary fill:#cfe9ff,stroke:#1769aa,stroke-width:2px,font-weight:bold");
        visibleLibraries
            .filter(g => selected && selected.has(g.id))
            .forEach(g => lines.push(`    class ${nodeId(g.id)} selectedLibrary`));

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

    function renderTreeChildren(node, lines, indent, parentFqn, parentSelfIds, showPhysicalName) {
        node.children.forEach(child => renderTreeNode(child, lines, indent, parentFqn, parentSelfIds, showPhysicalName));
    }

    function renderTreeNode(node, lines, indent, parentFqn, parentSelfIds, showPhysicalName) {
        const segmentLabel = (parentFqn && node.fqn.startsWith(parentFqn + '.'))
            ? node.fqn.substring(parentFqn.length + 1)
            : node.fqn;
        const label = showPhysicalName ? segmentLabel : (packageTermTitle(node.fqn) || segmentLabel);
        if (node.children.size === 0) {
            lines.push(`${pad(indent)}${nodeId(node.fqn)}@{shape: st-rect, label: "${escape(label)}"}`);
            return;
        }
        if (!node.isLeaf && node.children.size === 1) {
            const onlyChild = node.children.values().next().value;
            renderTreeNode(onlyChild, lines, indent, parentFqn, parentSelfIds, showPhysicalName);
            return;
        }
        const groupId = `${nodeId(node.fqn)}_grp`;
        lines.push(`${pad(indent)}subgraph ${groupId} ["${escape(label)}"]`);
        if (node.isLeaf) {
            const selfId = nodeId(node.fqn);
            const selfLabel = showPhysicalName ? node.fqn : (packageTermTitle(node.fqn) || node.fqn);
            lines.push(`${pad(indent + 1)}${selfId}@{shape: st-rect, label: "${escape(selfLabel)}"}`);
            parentSelfIds.push(selfId);
        }
        renderTreeChildren(node, lines, indent + 1, node.fqn, parentSelfIds, showPhysicalName);
        lines.push(`${pad(indent)}end`);
    }

    function packageTermTitle(fqn) {
        if (!Jig || !Jig.glossary || typeof Jig.glossary.getPackageTerm !== "function") return null;
        const term = Jig.glossary.getPackageTerm(fqn);
        return term && term.title ? term.title : null;
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

    function renderLibraryTable(tbody, groups) {
        tbody.textContent = "";
        const sorted = [...groups].sort((a, b) => {
            if (a.isJavaStandard !== b.isJavaStandard) return a.isJavaStandard ? 1 : -1;
            return a.displayName.localeCompare(b.displayName);
        });
        sorted.forEach(group => {
            const tr = document.createElement("tr");
            tr.dataset.libraryId = group.id;
            tr.style.cursor = "pointer";
            tr.addEventListener("click", () => toggleSelection(group.id));

            tr.appendChild(Jig.dom.createElement("td", {textContent: group.displayName}));
            tr.appendChild(Jig.dom.createElement("td", {
                style: {whiteSpace: "pre-line"},
                textContent: (group.samplePackages || []).join("\n"),
            }));

            const classes = group.usingClasses || [];
            const classesTd = document.createElement("td");
            if (classes.length > 0) {
                const details = document.createElement("details");
                // 行クリック→外部グループ選択 と干渉しないように分離
                details.addEventListener("click", e => e.stopPropagation());
                details.appendChild(Jig.dom.createElement("summary", {
                    style: {cursor: "pointer"},
                    textContent: `${classes.length} 件`,
                }));
                details.appendChild(Jig.dom.createElement("div", {
                    style: {whiteSpace: "pre-line"},
                    textContent: classes.join("\n"),
                }));
                classesTd.appendChild(details);
            }
            tr.appendChild(classesTd);

            tbody.appendChild(tr);
        });
    }

    function highlightSelectedRows(tbody) {
        if (!tbody) return;
        tbody.querySelectorAll("tr").forEach(tr => {
            const isSelected = selectedLibraryIds.has(tr.dataset.libraryId);
            tr.classList.toggle("is-selected", isSelected);
        });
    }

    return {
        init,
        toggleSelection,
        computeMaxDepth,
        computeInitialDepth,
        buildMermaidText,
        nodeId,
        escape,
    };
})();

Jig.bootstrap.register("library-dependency", LibraryDependencyApp.init);

if (typeof module !== "undefined" && module.exports) {
    module.exports = LibraryDependencyApp;
}
