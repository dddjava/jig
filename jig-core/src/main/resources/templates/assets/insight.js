function parseInsightData() {
    const dataElement = document.getElementById("insight-data");
    if (!dataElement) {
        return null;
    }
    try {
        return JSON.parse(dataElement.textContent);
    } catch (error) {
        console.error("Failed to parse insight JSON.", error);
        return null;
    }
}

function setInsightCount(elementId, count) {
    const element = document.getElementById(elementId);
    if (element) {
        element.textContent = count.toString();
    }
}

function createCell(text, className) {
    const cell = document.createElement("td");
    if (className) {
        cell.className = className;
    }
    cell.textContent = text;
    return cell;
}

function createZoomCell() {
    const cell = document.createElement("td");
    const icon = document.createElement("i");
    icon.className = "zoom";
    icon.textContent = "🔍";
    cell.appendChild(icon);
    return cell;
}

function renderPackageInsights(packages) {
    const tbody = document.querySelector("#package-insight-list tbody");
    if (!tbody) {
        return;
    }
    packages.forEach(packageInsight => {
        const row = document.createElement("tr");
        row.dataset.fqn = packageInsight.fqn;
        row.appendChild(createZoomCell());
        row.appendChild(createCell(packageInsight.fqn, "fqn"));
        row.appendChild(createCell(packageInsight.label));
        row.appendChild(createCell(packageInsight.numberOfTypes.toString(), "number"));
        row.appendChild(createCell(packageInsight.numberOfMethods.toString(), "number"));
        row.appendChild(createCell(packageInsight.numberOfUsingTypes.toString(), "number"));
        row.appendChild(createCell(packageInsight.cyclomaticComplexity.toString(), "number"));
        row.appendChild(createCell(packageInsight.size.toString(), "number"));
        tbody.appendChild(row);
    });
}

function renderTypeInsights(types) {
    const tbody = document.querySelector("#type-insight-list tbody");
    if (!tbody) {
        return;
    }
    types.forEach(typeInsight => {
        const row = document.createElement("tr");
        row.dataset.fqn = typeInsight.fqn;
        row.dataset.packageFqn = typeInsight.packageFqn;
        row.appendChild(createZoomCell());
        row.appendChild(createCell(typeInsight.fqn, "fqn"));
        row.appendChild(createCell(typeInsight.label));
        row.appendChild(createCell(typeInsight.numberOfMethods.toString(), "number"));
        row.appendChild(createCell(typeInsight.numberOfUsingTypes.toString(), "number"));
        row.appendChild(createCell(typeInsight.cyclomaticComplexity.toString(), "number"));
        row.appendChild(createCell(typeInsight.size.toString(), "number"));
        tbody.appendChild(row);
    });
}

function renderMethodInsights(methods) {
    const tbody = document.querySelector("#method-insight-list tbody");
    if (!tbody) {
        return;
    }
    methods.forEach(methodInsight => {
        const row = document.createElement("tr");
        row.dataset.fqn = methodInsight.fqn;
        row.dataset.packageFqn = methodInsight.packageFqn;
        row.dataset.typeFqn = methodInsight.typeFqn;
        row.appendChild(createZoomCell());
        row.appendChild(createCell(methodInsight.fqn, "fqn"));
        row.appendChild(createCell(methodInsight.label));
        row.appendChild(createCell(methodInsight.cyclomaticComplexity.toString(), "number"));
        row.appendChild(createCell(methodInsight.numberOfUsingTypes.toString(), "number"));
        row.appendChild(createCell(methodInsight.numberOfUsingMethods.toString(), "number"));
        row.appendChild(createCell(methodInsight.numberOfUsingFields.toString(), "number"));
        row.appendChild(createCell(methodInsight.size.toString(), "number"));
        tbody.appendChild(row);
    });
}

// 拡大アイコンをクリックしたときに、その行以外を非表示にする
function setupZoomIcons() {
    const zoomIcons = document.querySelectorAll("i.zoom");

    zoomIcons.forEach(icon => {
        icon.style.cursor = "pointer";

        icon.addEventListener("click", function () {
            const row = this.closest("tr");
            const table = this.closest("table");
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

// ズームを解除する
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

// ページ読み込み時のイベント
document.addEventListener("DOMContentLoaded", function () {
    if (!document.body.classList.contains("insight")) {
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

    setupSortableTables();
    setupZoomIcons();
    document.getElementById("cancel-zoom").addEventListener("click", cancelZoom);
});
