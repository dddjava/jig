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

function sortTable(tableId, columnIndex) {
    var table = document.getElementById(tableId);
    var rows = Array.from(table.getElementsByTagName("tbody")[0].getElementsByTagName("tr"));

    rows.sort(function (a, b) {
        var aValue = a.getElementsByTagName("td")[columnIndex].textContent;
        var bValue = b.getElementsByTagName("td")[columnIndex].textContent;
        return aValue.localeCompare(bValue);
    });

    var tbody = table.getElementsByTagName("tbody")[0];
    rows.forEach(function (row) {
        tbody.appendChild(row);
    });
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
    const showPackage = document.getElementById("show-package").checked;
    const showClass = document.getElementById("show-class").checked;
    const showMethod = document.getElementById("show-method").checked;
    const showField = document.getElementById("show-field").checked;

    const searchKeyword = document.getElementById("search-input").value.toLowerCase();
    const termArticles = document.getElementsByClassName("term");

    Array.from(termArticles).forEach(term => {
        const kindText = term.getElementsByClassName("kind")[0]?.textContent || "";

        // 種類で絞り込む
        let isVisible = false;
        if (kindText === "パッケージ" && showPackage) isVisible = true;
        if (kindText === "クラス" && showClass) isVisible = true;
        if (kindText === "メソッド" && showMethod) isVisible = true;
        if (kindText === "フィールド" && showField) isVisible = true;
        if (!isVisible) {
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
        // 検索キーワードでの判定
        const title = term.getElementsByTagName("h2")[0]?.textContent?.toLowerCase() || ""; // タイトル
        if (searchKeyword && !title.includes(searchKeyword) && !description.includes(searchKeyword)) {
            term.classList.add("hidden");
            return;
        }

        // すべての条件をパスした場合に表示
        term.classList.remove("hidden");
    });

    removeContiguousLetterNavigation();
}

function removeContiguousLetterNavigation() {
    console.log("removeContiguousLetterNavigation");
    const letterNavigations = Array.from(document.getElementsByClassName("letter-navigation"));

    letterNavigations.forEach((nav, index) => {
        // 1件目は無視
        if (index === 0) return;

        let visibleCount = 0;
        let sibling = nav.previousElementSibling;
        while (sibling) {
            // 表示しているものだけ対象にする
            if (!sibling.classList.contains("hidden")) {
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
            nav.classList.remove("hidden");
        } else {
            nav.classList.add("hidden");
        }
    });
}

document.getElementById("search-input").addEventListener("input", updateArticleVisibility);
document.getElementById("show-empty-description").addEventListener("change", updateArticleVisibility);
document.getElementById("show-package").addEventListener("change", updateArticleVisibility);
document.getElementById("show-class").addEventListener("change", updateArticleVisibility);
document.getElementById("show-method").addEventListener("change", updateArticleVisibility);
document.getElementById("show-field").addEventListener("change", updateArticleVisibility);

document.getElementById("show-navigation").addEventListener("change", function (event) {
    const showNavigation = document.getElementById("show-navigation").checked;
    const navigations = document.getElementsByClassName("letter-navigation");
    for (let i = 0; i < navigations.length; i++) {
        const navigation = navigations[i];
        if (showNavigation && navigation.classList.contains("fix")) {
            navigation.classList.remove("hidden");
        } else {
            navigation.classList.add("hidden");
        }
    }
});
