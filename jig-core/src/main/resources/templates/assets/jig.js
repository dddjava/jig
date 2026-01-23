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

function updateArticleVisibility() {
    const showEmptyDescription = document.getElementById("show-empty-description").checked;
    const kindVisibilityMap = {
        "パッケージ": document.getElementById("show-package").checked,
        "クラス": document.getElementById("show-class").checked,
        "メソッド": document.getElementById("show-method").checked,
        "フィールド": document.getElementById("show-field").checked,
    };

    const searchKeyword = document.getElementById("search-input").value.toLowerCase();
    const termArticles = document.getElementsByClassName("term");

    Array.from(termArticles).forEach(term => {
        const kindText = term.getElementsByClassName("kind")[0]?.textContent || "";

        // 種類で絞り込む
        if (!kindVisibilityMap[kindText]) {
            term.classList.add("hidden");
            return;
        }

        // 以降の判定で使用する説明文を取得
        const description = term.getElementsByClassName("description")[0]?.textContent?.toLowerCase() || "";

        // 説明文有無での判定
        if (!showEmptyDescription && !description) {
            term.classList.add("hidden");
            return;
        }
        // 検索キーワードでの判定（タイトルと説明文）
        const title = term.getElementsByClassName("term-title")[0]?.textContent?.toLowerCase() || "";
        if (searchKeyword && !title.includes(searchKeyword) && !description.includes(searchKeyword)) {
            term.classList.add("hidden");
            return;
        }

        // すべての条件をパスした場合に表示
        term.classList.remove("hidden");
    });

    // 表示が変わるのでnavも更新する
    updateLetterNavigationVisibility();
}

function updateLetterNavigationVisibility() {
    const letterNavigations = Array.from(document.getElementsByClassName("letter-navigation"));
    const showNavigation = document.getElementById("show-letter-navigation").checked;
    if (!showNavigation) {
        letterNavigations.forEach(nav => nav.classList.add("invisible"));
        return;
    }

    letterNavigations.forEach((nav, index) => {
        // 1件目は無視
        if (index === 0) {
            nav.classList.remove("invisible");
            return;
        }

        let visibleCount = 0;
        let sibling = nav.previousElementSibling;
        while (sibling) {
            // 表示しているものだけ対象にする
            if (!sibling.classList.contains("invisible")) {
                // letter-navigationはカウント対象外
                if (sibling.classList.contains("letter-navigation")) break;
                visibleCount++;
                // これ以上カウントする意味がないので抜ける
                if (visibleCount >= 10) break;
            }
            sibling = sibling.previousElementSibling;
        }

        // 10個以上表示するものがあったら表示する
        if (visibleCount >= 10) {
            nav.classList.remove("invisible");
        } else {
            nav.classList.add("invisible");
        }
    });
}

function toggleDescription() {
    // クラス名に一致する要素を全部取得
    const elements = document.getElementsByClassName("description");

    // 各要素に対して「hidden」クラスをトグル（付けたり外したり）する
    Array.from(elements).forEach(el => {
        console.log(el);
        el.classList.toggle("hidden");
    });
}

function setupSortableTables() {
    document.querySelectorAll("table.sortable").forEach(table => {
        const headers = table.querySelectorAll("thead th");
        headers.forEach((header, index) => {
            if (header.hasAttribute("onclick")) {
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

let packageSummaryCache = null;
let packageDiagramNodeIdToFqn = new Map();

function readPackageSummaryData() {
    if (packageSummaryCache) return packageSummaryCache;
    const jsonText = document.getElementById('package-data').textContent;
    /** @type {{packages?: Array<{fqn: string, name: string, classCount: number, description: string}>, relations?: Array<{from: string, to: string}>} | Array<{fqn: string, name: string, classCount: number, description: string}>} */
    const packageData = JSON.parse(jsonText);
    packageSummaryCache = {
        packages: Array.isArray(packageData) ? packageData : (packageData.packages ?? []),
        relations: Array.isArray(packageData) ? [] : (packageData.relations ?? []),
    };
    return packageSummaryCache;
}

function writePackageTable() {
    const {packages, relations} = readPackageSummaryData();
    const incomingCounts = new Map();
    const outgoingCounts = new Map();
    relations.forEach(relation => {
        outgoingCounts.set(relation.from, (outgoingCounts.get(relation.from) ?? 0) + 1);
        incomingCounts.set(relation.to, (incomingCounts.get(relation.to) ?? 0) + 1);
    });

    const tbody = document.querySelector('#package-table tbody');
    //tbody.innerHTML = '';

    packages.forEach(item => {
        const tr = document.createElement('tr');

        const fqnTd = document.createElement('td');
        fqnTd.textContent = item.fqn;
        fqnTd.className = 'fqn';
        fqnTd.addEventListener('click', () => filterPackageDiagramByFqn(item.fqn));
        tr.appendChild(fqnTd);

        const nameTd = document.createElement('td');
        nameTd.textContent = item.name;
        tr.appendChild(nameTd);

        const classCountTd = document.createElement('td');
        classCountTd.textContent = String(item.classCount);
        classCountTd.className = 'number';
        tr.appendChild(classCountTd);

        const incomingCountTd = document.createElement('td');
        incomingCountTd.textContent = String(incomingCounts.get(item.fqn) ?? 0);
        incomingCountTd.className = 'number';
        tr.appendChild(incomingCountTd);

        const outgoingCountTd = document.createElement('td');
        outgoingCountTd.textContent = String(outgoingCounts.get(item.fqn) ?? 0);
        outgoingCountTd.className = 'number';
        tr.appendChild(outgoingCountTd);

        const descTd = document.createElement('td');
        descTd.textContent = item.description;
        descTd.className = 'description hidden markdown';
        tr.appendChild(descTd);

        tbody.appendChild(tr);
    });
}

function writePackageRelationDiagram(filterFqn) {
    const diagram = document.getElementById('package-relation-diagram');
    if (!diagram) return;

    const {packages, relations} = readPackageSummaryData();
    const escapeMermaidText = text => text.replace(/"/g, '\\"');
    const lines = ['graph TD'];
    const scopePrefix = filterFqn ? `${filterFqn}.` : null;
    const withinScope = fqn => !filterFqn || fqn === filterFqn || fqn.startsWith(scopePrefix);
    const visiblePackages = packages.filter(item => withinScope(item.fqn));
    const visibleSet = new Set(visiblePackages.map(item => item.fqn));
    const visibleRelations = relations.filter(relation => withinScope(relation.from) && withinScope(relation.to));
    visibleRelations.forEach(relation => {
        visibleSet.add(relation.from);
        visibleSet.add(relation.to);
    });

    const nodeIdByFqn = new Map();
    packageDiagramNodeIdToFqn = new Map();
    let nodeIndex = 0;
    const ensureNodeId = fqn => {
        if (nodeIdByFqn.has(fqn)) return nodeIdByFqn.get(fqn);
        const nodeId = `P${nodeIndex++}`;
        nodeIdByFqn.set(fqn, nodeId);
        packageDiagramNodeIdToFqn.set(nodeId, fqn);
        lines.push(`${nodeId}["${escapeMermaidText(fqn)}"]`);
        lines.push(`click ${nodeId} filterPackageDiagram`);
        return nodeId;
    };

    Array.from(visibleSet).sort().forEach(ensureNodeId);
    visibleRelations.forEach(relation => {
        const fromId = ensureNodeId(relation.from);
        const toId = ensureNodeId(relation.to);
        lines.push(`${fromId} --> ${toId}`);
    });

    diagram.removeAttribute('data-processed');
    diagram.textContent = lines.join('\n');
    if (window.mermaid) {
        mermaid.initialize({startOnLoad: false, securityLevel: 'loose'});
        mermaid.run({nodes: [diagram]});
    }
}

function filterPackageDiagramByFqn(fqn) {
    writePackageRelationDiagram(fqn);
}

window.filterPackageDiagram = function (nodeId) {
    const fqn = packageDiagramNodeIdToFqn.get(nodeId);
    if (!fqn) return;
    filterPackageDiagramByFqn(fqn);
};

// ページ読み込み時のイベント
// リスナーの登録はそのページだけでやる
document.addEventListener("DOMContentLoaded", function () {
    if (document.body.classList.contains("glossary")) {
        document.getElementById("search-input").addEventListener("input", updateArticleVisibility);
        document.getElementById("show-empty-description").addEventListener("change", updateArticleVisibility);
        document.getElementById("show-package").addEventListener("change", updateArticleVisibility);
        document.getElementById("show-class").addEventListener("change", updateArticleVisibility);
        document.getElementById("show-method").addEventListener("change", updateArticleVisibility);
        document.getElementById("show-field").addEventListener("change", updateArticleVisibility);
        document.getElementById("show-letter-navigation").addEventListener("change", updateLetterNavigationVisibility);

        updateLetterNavigationVisibility();
    } else if (document.body.classList.contains("package-list")) {
        document.getElementById("toggle-description-btn").addEventListener("click", toggleDescription);
        setupSortableTables();
        writePackageRelationDiagram();
        writePackageTable();
    } else if (document.body.classList.contains("insight")) {
        setupSortableTables();
        setupZoomIcons();
        document.getElementById("cancel-zoom").addEventListener("click", cancelZoom);
    } else if (document.body.classList.contains("repository")) {
        setupSortableTables();
    }
});
