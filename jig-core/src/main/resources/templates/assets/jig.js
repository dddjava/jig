Array.from(document.getElementsByClassName("markdown")).forEach(x => x.innerHTML = marked.parse(x.innerHTML))

function toggleTableColumn(tableId, columnIndex) {
    var table = document.getElementById(tableId);
    var rows = table.rows;

    for (var i = 0; i < rows.length; i++) {
        var row = rows[i];
        var cells = row.cells;
        var cell = cells[columnIndex];

        if (cell.classList.contains("hidden")) {
            cell.classList.remove("hidden");
        } else {
            cell.classList.add("hidden");
        }
    }
}

function filterTable(tableId, filterInputId) {
    var table = document.getElementById(tableId);
    var rows = table.getElementsByTagName("tr");
    var filterText = document.getElementById(filterInputId).value;

    for (var i = 1; i < rows.length; i++) {
        var row = rows[i];
        var cells = row.getElementsByTagName("td");
        var match = false;

        for (var j = 0; j < cells.length; j++) {
            var cell = cells[j];
            if (cell) {
                var cellText = cell.textContent || cell.innerText;
                if (cellText.indexOf(filterText) > -1) {
                    match = true;
                    break;
                }
            }
        }

        if (match) {
            row.style.display = "";
        } else {
            row.style.display = "none";
        }
    }
}

function sortTable(event) {
    const headerColumn = event.target;
    const columnIndex = Array.from(headerColumn.parentNode.children).indexOf(headerColumn);

    const rows = Array.from(headerColumn.closest("table").querySelectorAll("tbody tr"));

    const orderFlag = headerColumn.dataset.orderFlag === "true";

    // デフォルトでは辞書順でソート
    let type = "string";

    // 1行目を見てclass=numberがあれば数値としてソート
    const firstRow = rows[0];
    if (firstRow) {
        const cell = firstRow.cells[columnIndex];
        if (cell && cell.classList.contains("number")) {
            type = "number";
        }
    }

    rows.sort(function (a, b) {
        const aValue = a.getElementsByTagName("td")[columnIndex].textContent;
        const bValue = b.getElementsByTagName("td")[columnIndex].textContent;

        // 数値は降順、文字は昇順
        if (type === "number") {
            const aNumber = parseFloat(aValue) || 0;
            const bNumber = parseFloat(bValue) || 0;
            return (aNumber - bNumber) * (orderFlag ? 1 : -1);
        }
        return (aValue.localeCompare(bValue)) * (orderFlag ? -1 : 1);
    });

    rows.forEach(row => headerColumn.closest("table").getElementsByTagName("tbody")[0].appendChild(row));

    headerColumn.dataset.orderFlag = (!orderFlag).toString();
}

// ブラウザバックなどで該当要素に移動する
// Safariなどではブラウザバックでも移動するが、ChromeやEdgeだと移動しない。
// なのでpopstateイベントでlocationからhashを取得し、hashがある場合はその要素に移動する
window.addEventListener("popstate", function (event) {
    const hash = event.target.location.hash;

    if (hash) {
        const anchor = document.getElementById(hash.substring(1))
        if (anchor) {
            anchor.scrollIntoView();
        }
    }
});

function setupSortableTables() {
    document.querySelectorAll("table.sortable").forEach(table => {
        const headers = table.querySelectorAll("thead th");
        headers.forEach((header, index) => {
            if (header.hasAttribute("onclick")) {
                return;
            }
            if (header.classList.contains("no-sort")) {
                return;
            }

            header.addEventListener("click", sortTable);
            header.style.cursor = "pointer";
        });
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
// リスナーの登録はそのページだけでやる
document.addEventListener("DOMContentLoaded", function () {
    if (document.body.classList.contains("insight")) {
        setupSortableTables();
        setupZoomIcons();
        document.getElementById("cancel-zoom").addEventListener("click", cancelZoom);
    } else if (document.body.classList.contains("repository")) {
        setupSortableTables();
    }
});
