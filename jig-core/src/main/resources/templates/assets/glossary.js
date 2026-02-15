
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

function getGlossaryData() {
    const jsonText = document.getElementById("glossary-data")?.textContent || "{}";
    /** @type {{terms?: Array<{title: string, simpleText: string, fqn: string, kind: string, description: string}>} | Array<{title: string, simpleText: string, fqn: string, kind: string, description: string}>} */
    const glossaryData = JSON.parse(jsonText);
    if (Array.isArray(glossaryData)) {
        return glossaryData;
    }
    return glossaryData.terms ?? [];
}

function buildTermAnchorId(term, index) {
    return term.fqn || `term-${index}`;
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

function renderTermSidebar(terms) {
    const list = document.getElementById("term-sidebar-list");
    if (!list) return;

    list.innerHTML = "";
    if (terms.length === 0) return;

    const fragment = document.createDocumentFragment();
    terms.forEach((term, index) => {
        const link = document.createElement("a");
        link.className = "term-sidebar__item";
        link.href = `#${buildTermAnchorId(term, index)}`;
        link.textContent = term.title || "";
        fragment.appendChild(link);
    });
    list.appendChild(fragment);
}

function renderGlossaryTerms(terms, displayMode) { // displayMode を引数に追加
    const list = document.getElementById("term-list");
    if (!list) return;
    list.innerHTML = "";

    const fragment = document.createDocumentFragment();
    terms.forEach((term, index) => {
        const article = document.createElement("article");
        article.className = "term";
        // 他ドキュメントからのリンク用にFQNをIDとして設定する
        article.id = buildTermAnchorId(term, index);

        const title = document.createElement("h2");
        title.className = "term-title";
        title.textContent = term.title || "";
        article.appendChild(title);

        // 概要表示の場合はdlタグを出力しない
        if (displayMode === 'full') {
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
        }

        const description = document.createElement("div");
        description.className = "description markdown";
        description.innerHTML = term.description || "";
        article.appendChild(description);

        fragment.appendChild(article);
    });

    list.appendChild(fragment);
}

function renderFilteredTerms(terms, controls) {
    const filteredTerms = getFilteredTerms(terms, controls);
    const sortedTerms = sortTerms(filteredTerms, controls.sortOrder?.value);
    // renderGlossaryTerms に表示モードを渡す
    renderTermSidebar(sortedTerms);
    renderGlossaryTerms(sortedTerms, controls.displayModeSelect?.value);
    renderMarkdownDescriptions();
}

function renderMarkdownDescriptions() {
    if (!window.marked) return;
    Array.from(document.getElementsByClassName("markdown"))
        .forEach(node => node.innerHTML = marked.parse(node.innerHTML));
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
    const searchKeyword = controls.searchInput.value;

    const searchMethod = document.querySelector('input[name="search-method"]:checked')?.value || 'partial';
    const targetsToSearch = {
        title: controls.searchTargetName.checked,
        description: controls.searchTargetDescription.checked,
        fqn: controls.searchTargetFqn.checked,
        simpleText: controls.searchTargetSimple.checked,
        kind: controls.searchTargetKind.checked,
    };

    return terms.filter(term => {
        // 種類で絞り込む
        const kindText = term.kind || "";
        if (!kindVisibilityMap[kindText]) return false;

        // 説明文有無での判定
        const description = (term.description || "");
        if (!showEmptyDescription && !description) return false;

        // キーワード検索
        if (searchKeyword) {
            const isMatch = Object.keys(targetsToSearch).some(prop => {
                if (!targetsToSearch[prop]) return false;

                const targetText = term[prop] || "";

                switch (searchMethod) {
                    case "exact":
                        return targetText.toLowerCase() === searchKeyword.toLowerCase();
                    case "regex":
                        try {
                            if (searchKeyword.trim() === '') return false;
                            return new RegExp(searchKeyword, 'i').test(targetText);
                        } catch (e) {
                            return false; // 不正な正規表現
                        }
                    case "partial":
                    default:
                        return targetText.toLowerCase().includes(searchKeyword.toLowerCase());
                }
            });

            if (!isMatch) return false;
        }

        return true;
    });
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

            // 新しい検索オプション
            searchTargetName: document.getElementById('search-target-name'),
            searchTargetDescription: document.getElementById('search-target-description'),
            searchTargetFqn: document.getElementById('search-target-fqn'),
            searchTargetSimple: document.getElementById('search-target-simple'),
            searchTargetKind: document.getElementById('search-target-kind'),

            // 新しい表示モード選択
            displayModeSelect: document.getElementById('display-mode-select'),
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
        // 新しい表示モード選択のイベントリスナー
        if (controls.displayModeSelect) {
            controls.displayModeSelect.addEventListener("change", updateArticles);
        }

        // 新しい検索オプションのイベントリスナー
        document.querySelectorAll('input[name="search-method"]').forEach(radio => radio.addEventListener('change', updateArticles));
        controls.searchTargetName.addEventListener('change', updateArticles);
        controls.searchTargetDescription.addEventListener('change', updateArticles);
        controls.searchTargetFqn.addEventListener('change', updateArticles);
        controls.searchTargetSimple.addEventListener('change', updateArticles);
        controls.searchTargetKind.addEventListener('change', updateArticles);

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

        sortTerms,
        getFilteredTerms,
        getGlossaryData,
        buildTermAnchorId,
        escapeCsvValue,
        buildGlossaryCsv,
        renderTermSidebar,
        renderGlossaryTerms,
        renderFilteredTerms,
        renderMarkdownDescriptions,
    };
}
