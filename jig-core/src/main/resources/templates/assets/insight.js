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

    setupSortableTables();
    setupZoomIcons();
    document.getElementById("cancel-zoom").addEventListener("click", cancelZoom);
});
