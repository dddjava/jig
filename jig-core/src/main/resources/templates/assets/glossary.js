
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
    const glossaryData = globalThis.glossaryData;
    if (glossaryData) {
        if (Array.isArray(glossaryData)) return glossaryData;
        if (glossaryData.terms) return glossaryData.terms;
        return Object.entries(glossaryData).map(([fqn, term]) => ({...term, fqn}));
    }

    const script = typeof document !== "undefined" ? document.getElementById("glossary-data") : null;
    if (!script) return [];

    const jsonText = script.textContent || "{}";
    try {
        const parsed = JSON.parse(jsonText);
        if (Array.isArray(parsed)) return parsed;
        if (parsed?.terms) return parsed.terms;
        return Object.entries(parsed).map(([fqn, term]) => ({...term, fqn}));
    } catch (e) {
        return [];
    }
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
    const items = terms.map((term, index) => ({id: buildTermAnchorId(term, index), label: term.title || ""}));
    globalThis.Jig.sidebar.renderSection(list, "用語一覧", items);
}

function renderGlossaryTerms(terms, displayMode) {
    const list = document.getElementById("term-list");
    if (!list) return;
    list.innerHTML = "";

    const createElement = globalThis.Jig.dom.createElement;
    terms.forEach((term, index) => {
        const anchorId = buildTermAnchorId(term, index);

        const metaChildren = [];
        if (displayMode === "full") {
            if (term.fqn) {
                metaChildren.push(createElement("div", {className: "fully-qualified-name", textContent: term.fqn}));
            }
            const metaTexts = [];
            if (term.simpleText) metaTexts.push(`単純名: ${term.simpleText}`);
            if (term.kind) metaTexts.push(`種類: ${term.kind}`);
            if (metaTexts.length > 0) {
                metaChildren.push(createElement("p", {className: "weak", textContent: metaTexts.join(" / ")}));
            }
        }

        const article = createElement("article", {
            className: "jig-card jig-card--type",
            children: [
                createElement("h3", {children: [createElement("a", {id: anchorId, textContent: term.title || ""})]}),
                ...metaChildren,
                createElement("div", {className: "markdown", innerHTML: term.description || ""}),
            ]
        });
        list.appendChild(article);
    });
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
    Array.from(document.getElementsByClassName("markdown"))
        .forEach(node => {
            if (globalThis.Jig?.markdown?.parse) {
                node.innerHTML = globalThis.Jig.markdown.parse(node.innerHTML);
                return;
            }
            if (window.marked) {
                node.innerHTML = marked.parse(node.innerHTML);
            }
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
