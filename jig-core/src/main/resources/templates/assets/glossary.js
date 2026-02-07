function updateArticleVisibility(controls) {
    const showEmptyDescription = controls.showEmptyDescription.checked;
    const kindVisibilityMap = {
        "パッケージ": controls.showPackage.checked,
        "クラス": controls.showClass.checked,
        "メソッド": controls.showMethod.checked,
        "フィールド": controls.showField.checked,
    };

    const searchKeyword = controls.searchInput.value.toLowerCase();
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

}

// 文字列の比較は日本語を優先しつつ大小を無視する
const termCollator = new Intl.Collator("ja", {numeric: true, sensitivity: "base"});

function sortTerms(terms, sortKey) {
    const keyMap = {
        name: "title",
        fqn: "fqn",
        simple: "simpleText",
    };
    const key = keyMap[sortKey] ?? "title";
    return [...terms].sort((left, right) => {
        const leftValue = left?.[key] ?? "";
        const rightValue = right?.[key] ?? "";
        const primary = termCollator.compare(leftValue, rightValue);
        if (primary !== 0) return primary;
        return termCollator.compare(left?.fqn ?? "", right?.fqn ?? "");
    });
}

function getFilteredTerms(terms, controls) {
    if (!controls) return terms;
    const showEmptyDescription = controls.showEmptyDescription.checked;
    const kindVisibilityMap = {
        "パッケージ": controls.showPackage.checked,
        "クラス": controls.showClass.checked,
        "メソッド": controls.showMethod.checked,
        "フィールド": controls.showField.checked,
    };
    const searchKeyword = controls.searchInput.value.toLowerCase();

    return terms.filter(term => {
        const kindText = term.kind || "";
        if (!kindVisibilityMap[kindText]) return false;

        const description = (term.description || "").toLowerCase();
        if (!showEmptyDescription && !description) return false;

        const title = (term.title || "").toLowerCase();
        if (searchKeyword && !title.includes(searchKeyword) && !description.includes(searchKeyword)) {
            return false;
        }
        return true;
    });
}

function getGlossaryData() {
    const jsonText = document.getElementById("glossary-data")?.textContent || "{}";
    /** @type {{terms?: Array<{title: string, simpleText: string, fqn: string, kind: string, description: string}>} | Array<{title: string, simpleText: string, fqn: string, kind: string, description: string}>} */
    const glossaryData = JSON.parse(jsonText);
    if (Array.isArray(glossaryData)) {
        return glossaryData;
    }
    return glossaryData.terms ?? [];
}

function escapeCsvValue(value) {
    const text = String(value ?? "")
        .replace(/\r\n/g, "\n")
        .replace(/\r/g, "\n");
    return `"${text.replace(/"/g, "\"\"")}"`;
}

function buildGlossaryCsv(terms) {
    const header = ["用語（英名）", "用語", "説明", "種類", "識別子"];
    const rows = terms.map(term => [
        term.simpleText ?? "",
        term.title ?? "",
        term.description ?? "",
        term.kind ?? "",
        term.fqn ?? "",
    ]);

    const lines = [header, ...rows].map(row => row.map(escapeCsvValue).join(","));
    return lines.join("\r\n");
}

function downloadCsv(text, filename) {
    const blob = new Blob([text], {type: "text/csv;charset=utf-8;"});
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = filename;
    document.body.appendChild(anchor);
    anchor.click();
    anchor.remove();
    URL.revokeObjectURL(url);
}

function renderGlossaryTerms(terms) {
    const list = document.getElementById("term-list");
    if (!list) return;
    list.innerHTML = "";

    const fragment = document.createDocumentFragment();
    terms.forEach(term => {
        const article = document.createElement("article");
        article.className = "term";

        const title = document.createElement("h2");
        title.className = "term-title";
        title.textContent = term.title || "";
        article.appendChild(title);

        const dl = document.createElement("dl");

        const simpleNameTitle = document.createElement("dt");
        simpleNameTitle.textContent = "単純名";
        const simpleNameValue = document.createElement("dd");
        simpleNameValue.textContent = term.simpleText || "";

        const fqnTitle = document.createElement("dt");
        fqnTitle.textContent = "完全修飾名";
        const fqnValue = document.createElement("dd");
        fqnValue.textContent = term.fqn || "";

        const kindTitle = document.createElement("dt");
        kindTitle.textContent = "種類";
        const kindValue = document.createElement("dd");
        kindValue.className = "kind";
        kindValue.textContent = term.kind || "";

        dl.appendChild(simpleNameTitle);
        dl.appendChild(simpleNameValue);
        dl.appendChild(fqnTitle);
        dl.appendChild(fqnValue);
        dl.appendChild(kindTitle);
        dl.appendChild(kindValue);
        article.appendChild(dl);

        const description = document.createElement("div");
        description.className = "description markdown";
        description.innerHTML = term.description || "";
        article.appendChild(description);

        fragment.appendChild(article);
    });

    list.appendChild(fragment);
}

function renderMarkdownDescriptions() {
    if (!window.marked) return;
    Array.from(document.getElementsByClassName("markdown"))
        .forEach(node => node.innerHTML = marked.parse(node.innerHTML));
}

function renderFilteredTerms(terms, controls) {
    const filteredTerms = getFilteredTerms(terms, controls);
    const sortedTerms = sortTerms(filteredTerms, controls.sortOrder?.value);
    renderGlossaryTerms(sortedTerms);
    renderMarkdownDescriptions();
}

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", function () {
        if (!document.body.classList.contains("glossary")) return;

        const terms = getGlossaryData();

        const controls = {
            searchInput: document.getElementById("search-input"),
            showEmptyDescription: document.getElementById("show-empty-description"),
            showPackage: document.getElementById("show-package"),
            showClass: document.getElementById("show-class"),
            showMethod: document.getElementById("show-method"),
            showField: document.getElementById("show-field"),
            sortOrder: document.getElementById("sort-order"),
        };

        const updateArticles = () => renderFilteredTerms(terms, controls);

        controls.searchInput.addEventListener("input", updateArticles);
        controls.showEmptyDescription.addEventListener("change", updateArticles);
        controls.showPackage.addEventListener("change", updateArticles);
        controls.showClass.addEventListener("change", updateArticles);
        controls.showMethod.addEventListener("change", updateArticles);
        controls.showField.addEventListener("change", updateArticles);
        if (controls.sortOrder) {
            controls.sortOrder.addEventListener("change", updateArticles);
        }
        const exportButton = document.getElementById("export-csv");
        if (exportButton) {
            exportButton.addEventListener("click", () => {
                const filteredTerms = getFilteredTerms(terms, controls);
                const csvText = buildGlossaryCsv(filteredTerms);
                downloadCsv(csvText, "glossary.csv");
            });
        }

        updateArticles();
    });
}

// Test-only exports for Node; no-op in browsers.
if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        updateArticleVisibility,
        sortTerms,
        getFilteredTerms,
        getGlossaryData,
        escapeCsvValue,
        buildGlossaryCsv,
        renderGlossaryTerms,
        renderFilteredTerms,
        renderMarkdownDescriptions,
    };
}
