const InsightApp = (() => {
    const Jig = globalThis.Jig;


    function parseInsightData() {
        return Jig.data.insight.get();
    }

    function setInsightCount(elementId, count) {
        const element = document.getElementById(elementId);
        if (element) {
            element.textContent = count;
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
        Jig.dom.renderTableRows("package-insight-list", packages, (row, pkg) => {
            row.dataset.fqn = pkg.fqn;
            row.appendChild(createZoomCell());
            row.appendChild(Jig.dom.createCell(pkg.fqn, "fqn"));
            row.appendChild(Jig.dom.createCell(Jig.glossary.getPackageTerm(pkg.fqn).title));
            row.appendChild(Jig.dom.createCell(pkg.numberOfTypes, "number"));
            row.appendChild(Jig.dom.createCell(pkg.numberOfMethods, "number"));
            row.appendChild(Jig.dom.createCell(pkg.numberOfUsingTypes, "number"));
            row.appendChild(Jig.dom.createCell(pkg.cyclomaticComplexity, "number"));
            row.appendChild(Jig.dom.createCell(pkg.size, "number"));
        }, {clear: true});
    }

    function renderTypeInsights(types) {
        Jig.dom.renderTableRows("type-insight-list", types, (row, type) => {
            row.dataset.fqn = type.fqn;
            row.dataset.packageFqn = type.packageFqn;
            row.appendChild(createZoomCell());
            row.appendChild(Jig.dom.createCell(type.fqn, "fqn"));
            row.appendChild(Jig.dom.createCell(Jig.glossary.getTypeTerm(type.fqn).title));
            row.appendChild(Jig.dom.createCell(type.numberOfMethods, "number"));
            row.appendChild(Jig.dom.createCell(type.numberOfUsingTypes, "number"));
            row.appendChild(Jig.dom.createCell(type.numberOfUsedByTypes, "number"));
            row.appendChild(Jig.dom.createCell(type.instability.toFixed(2), "number"));
            row.appendChild(Jig.dom.createCell(type.lcom.toFixed(2), "number"));
            row.appendChild(Jig.dom.createCell(type.cyclomaticComplexity, "number"));
            row.appendChild(Jig.dom.createCell(type.size, "number"));
        }, {clear: true});
    }

    function renderMethodInsights(methods) {
        Jig.dom.renderTableRows("method-insight-list", methods, (row, method) => {
            row.dataset.fqn = method.fqn;
            row.dataset.packageFqn = method.packageFqn;
            row.dataset.typeFqn = method.typeFqn;
            row.appendChild(createZoomCell());
            row.appendChild(Jig.dom.createCell(method.fqn, "fqn"));
            row.appendChild(Jig.dom.createCell(Jig.glossary.getMethodTerm(method.fqn).title));
            row.appendChild(Jig.dom.createCell(method.cyclomaticComplexity, "number"));
            row.appendChild(Jig.dom.createCell(method.numberOfUsingTypes, "number"));
            row.appendChild(Jig.dom.createCell(method.numberOfUsingMethods, "number"));
            row.appendChild(Jig.dom.createCell(method.numberOfUsingFields, "number"));
            row.appendChild(Jig.dom.createCell(method.numberOfUsingOwnFields, "number"));
            row.appendChild(Jig.dom.createCell(method.numberOfUsingOwnMethods, "number"));
            row.appendChild(Jig.dom.createCell(method.size, "number"));
        }, {clear: true});
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

        Jig.dom.setupSortableTables();
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
