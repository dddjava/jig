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

function changeArticleVisibility(event) {
    const showEmptyDescription = document.getElementById("show-empty-description").checked;
    const showPackage = document.getElementById("show-package").checked;
    const showClass = document.getElementById("show-class").checked;
    const showMethod = document.getElementById("show-method").checked;

    const termArticles = document.getElementsByClassName("term");
    for (let i = 0; i < termArticles.length; i++) {
        const kindText = termArticles[i].getElementsByClassName("kind")[0].textContent;
        if (kindText === "パッケージ") {
            if (showPackage) {
                termArticles[i].classList.remove("hidden");
            } else {
                termArticles[i].classList.add("hidden");
                // ここでhiddenとするものは以降の判定不要
                continue;
            }
        } else if (kindText === "クラス") {
            if (showClass) {
                termArticles[i].classList.remove("hidden");
            } else {
                termArticles[i].classList.add("hidden");
                // ここでhiddenとするものは以降の判定不要
                continue;
            }
        } else if (kindText === "メソッド") {
            if (showMethod) {
                termArticles[i].classList.remove("hidden");
            } else {
                termArticles[i].classList.add("hidden");
                // ここでhiddenとするものは以降の判定不要
                continue;
            }
        }

        if (showEmptyDescription) {
            termArticles[i].classList.remove("hidden");
        } else {
            const description = termArticles[i].getElementsByClassName("description")[0];
            if (!description || description.textContent.trim().length === 0) {
                termArticles[i].classList.add("hidden");
            }
        }
    }
}

document.getElementById("show-empty-description").addEventListener("change", changeArticleVisibility);
document.getElementById("show-package").addEventListener("change", changeArticleVisibility);
document.getElementById("show-class").addEventListener("change", changeArticleVisibility);
document.getElementById("show-method").addEventListener("change", changeArticleVisibility);
document.getElementById("show-field").addEventListener("change", changeArticleVisibility);

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

document.getElementById("search-input").addEventListener("input", function () {
    const searchKeyword = this.value.toLowerCase(); // 小文字で比較する
    const termElements = document.getElementsByClassName("term");

    Array.from(termElements).forEach(term => {
        const title = term.getElementsByClassName("term-title")[0].textContent.toLowerCase();
        const descriptionElement = term.getElementsByClassName("description")[0];
        const description = descriptionElement ? descriptionElement.textContent.toLowerCase() : "";

        // タイトルまたは説明文に検索キーワードが含まれている場合
        if (title.includes(searchKeyword) || description.includes(searchKeyword)) {
            term.classList.remove("hidden");
        } else {
            term.classList.add("hidden");
        }
    });
});