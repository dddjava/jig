globalThis.Jig ??= {};
globalThis.Jig.dom ??= {};

const InsightApp = (() => {

    function parseInsightData() {
        return globalThis.insightData ?? null;
    }

    function setInsightCount(elementId, count) {
        const element = document.getElementById(elementId);
        if (element) {
            element.textContent = count.toString();
        }
    }

    function createZoomCell() {
        return Jig.dom.createElement("td", {
            children: [
                Jig.dom.createElement("i", {
                    className: "zoom",
                    textContent: "🔍"
                })
            ]
        });
    }

    function renderPackageInsights(packages) {
        const tbody = document.querySelector("#package-insight-list tbody");
        if (!tbody) {
            return;
        }
        packages.forEach(packageInsight => {
            const row = Jig.dom.createElement("tr");
            row.dataset.fqn = packageInsight.fqn;
            row.appendChild(createZoomCell());
            row.appendChild(Jig.dom.createCell(packageInsight.fqn, "fqn"));
            row.appendChild(Jig.dom.createCell(packageInsight.label));
            row.appendChild(Jig.dom.createCell(packageInsight.numberOfTypes.toString(), "number"));
            row.appendChild(Jig.dom.createCell(packageInsight.numberOfMethods.toString(), "number"));
            row.appendChild(Jig.dom.createCell(packageInsight.numberOfUsingTypes.toString(), "number"));
            row.appendChild(Jig.dom.createCell(packageInsight.cyclomaticComplexity.toString(), "number"));
            row.appendChild(Jig.dom.createCell(packageInsight.size.toString(), "number"));
            tbody.appendChild(row);
        });
    }

    function renderTypeInsights(types) {
        const tbody = document.querySelector("#type-insight-list tbody");
        if (!tbody) {
            return;
        }
        types.forEach(typeInsight => {
            const row = Jig.dom.createElement("tr");
            row.dataset.fqn = typeInsight.fqn;
            row.dataset.packageFqn = typeInsight.packageFqn;
            row.appendChild(createZoomCell());
            row.appendChild(Jig.dom.createCell(typeInsight.fqn, "fqn"));
            row.appendChild(Jig.dom.createCell(typeInsight.label));
            row.appendChild(Jig.dom.createCell(typeInsight.numberOfMethods.toString(), "number"));
            row.appendChild(Jig.dom.createCell(typeInsight.numberOfUsingTypes.toString(), "number"));
            row.appendChild(Jig.dom.createCell(typeInsight.numberOfUsedByTypes.toString(), "number"));
            row.appendChild(Jig.dom.createCell(typeInsight.instability.toFixed(2), "number"));
            row.appendChild(Jig.dom.createCell(typeInsight.lcom.toFixed(2), "number"));
            row.appendChild(Jig.dom.createCell(typeInsight.cyclomaticComplexity.toString(), "number"));
            row.appendChild(Jig.dom.createCell(typeInsight.size.toString(), "number"));
            tbody.appendChild(row);
        });
    }

    function renderMethodInsights(methods) {
        const tbody = document.querySelector("#method-insight-list tbody");
        if (!tbody) {
            return;
        }
        methods.forEach(methodInsight => {
            const row = Jig.dom.createElement("tr");
            row.dataset.fqn = methodInsight.fqn;
            row.dataset.packageFqn = methodInsight.packageFqn;
            row.dataset.typeFqn = methodInsight.typeFqn;
            row.appendChild(createZoomCell());
            row.appendChild(Jig.dom.createCell(methodInsight.fqn, "fqn"));
            row.appendChild(Jig.dom.createCell(methodInsight.label));
            row.appendChild(Jig.dom.createCell(methodInsight.cyclomaticComplexity.toString(), "number"));
            row.appendChild(Jig.dom.createCell(methodInsight.numberOfUsingTypes.toString(), "number"));
            row.appendChild(Jig.dom.createCell(methodInsight.numberOfUsingMethods.toString(), "number"));
            row.appendChild(Jig.dom.createCell(methodInsight.numberOfUsingFields.toString(), "number"));
            row.appendChild(Jig.dom.createCell(methodInsight.numberOfUsingOwnFields.toString(), "number"));
            row.appendChild(Jig.dom.createCell(methodInsight.numberOfUsingOwnMethods.toString(), "number"));
            row.appendChild(Jig.dom.createCell(methodInsight.size.toString(), "number"));
            tbody.appendChild(row);
        });
    }

    function setupZoomIcons() {
        const zoomIcons = document.querySelectorAll("i.zoom");

        zoomIcons.forEach(icon => {
            icon.style.cursor = "pointer";

            icon.addEventListener("click", (event) => {
                const row = event.target.closest("tr");
                const table = event.target.closest("table");
                const tbody = table.querySelector("tbody");
                const allRows = tbody.querySelectorAll("tr");
                const fqn = row.querySelector("td.fqn").textContent;

                // クリックされた行以外を非表示にする
                allRows.forEach(r => {
                    if (r !== row && !fqnStartsWith(fqn, r)) {
                        r.classList.add("hidden-by-zoom");
                    }
                });

                zoomFamilyTables(table, row);
                // ズーム解除ボタンを表示
                document.getElementById("cancel-zoom").classList.remove("hidden");
            });
        });
    }

    function cancelZoom(event) {
        // すべてのテーブルからhidden-by-zoomクラスを削除
        document.querySelectorAll("table tbody tr.hidden-by-zoom").forEach(row => {
            row.classList.remove("hidden-by-zoom");
        });
        event.target.classList.add("hidden");
    }

    function fqnStartsWith(prefix, targetRow) {
        return targetRow.querySelector("td.fqn").textContent.startsWith(prefix);
    }

    function zoomFamilyTables(baseTable, baseRow) {
        baseTable.parentElement.querySelectorAll("table").forEach(table => {
            if (table === baseTable) return;

            const allRows = table.querySelectorAll("tbody tr");

            // zoomされているものがあったら一旦解除して
            const hiddenRows = table.querySelectorAll("tbody tr.hidden-by-zoom");
            if (hiddenRows.length > 0) {
                allRows.forEach(r => {
                    r.classList.remove("hidden-by-zoom");
                });
            }

            // 関係するもの以外を非表示にする
            // 元テーブルと対象テーブルの組み合わせでprefixが変わる
            let prefix = baseRow.dataset.fqn;
            if (baseTable.id.includes("package")) {
                prefix = prefix + '.';
            } else if (baseTable.id.includes("type")) {
                if (table.id.includes("package")) {
                    prefix = baseRow.dataset.packageFqn;
                } else if (table.id.includes("method")) {
                    prefix = prefix + '#';
                }
            } else if (baseTable.id.includes("method")) {
                if (table.id.includes("package")) {
                    prefix = baseRow.dataset.packageFqn;
                }
                if (table.id.includes("type")) {
                    prefix = baseRow.dataset.typeFqn;
                }
            }
            allRows.forEach(r => {
                if (!fqnStartsWith(prefix, r)) r.classList.add("hidden-by-zoom");
            });
        })
    }

    function init() {
        if (typeof document === "undefined" || !document.body.classList.contains("insight")) {
            return;
        }

        const insightData = parseInsightData();
        if (insightData) {
            renderPackageInsights(insightData.packages || []);
            renderTypeInsights(insightData.types || []);
            renderMethodInsights(insightData.methods || []);
            setInsightCount("package-count", (insightData.packages || []).length);
            setInsightCount("type-count", (insightData.types || []).length);
            setInsightCount("method-count", (insightData.methods || []).length);
        }

        globalThis.setupSortableTables?.();
        setupZoomIcons();
        document.getElementById("cancel-zoom")?.addEventListener("click", (e) => cancelZoom(e));
    }

    return {
        init,
        parseInsightData,
        setInsightCount,
        createZoomCell,
        renderPackageInsights,
        renderTypeInsights,
        renderMethodInsights,
        setupZoomIcons,
        cancelZoom,
    }
})();

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", () => {
        InsightApp.init();
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = InsightApp;
}
