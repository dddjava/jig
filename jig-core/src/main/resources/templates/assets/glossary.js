
const createElement = globalThis.Jig.dom.createElement;

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

function normalizeGlossaryData(data) {
    if (!data) return null;
    if (Array.isArray(data)) return data;
    if (data.terms) return Object.entries(data.terms).map(([fqn, term]) => ({...term, fqn}));
    return Object.entries(data).map(([fqn, term]) => ({...term, fqn}));
}

function getDomainPackageRoots() {
    const glossaryData = globalThis.glossaryData;
    if (glossaryData && glossaryData.domainPackageRoots) {
        return glossaryData.domainPackageRoots;
    }
    return [];
}

function getGlossaryData() {
    const glossaryData = globalThis.glossaryData;
    const normalized = normalizeGlossaryData(glossaryData);
    if (normalized) return normalized;

    const script = typeof document !== "undefined" ? document.getElementById("glossary-data") : null;
    if (!script) return [];

    const jsonText = script.textContent || "{}";
    try {
        const parsed = JSON.parse(jsonText);
        return normalizeGlossaryData(parsed) ?? [];
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

function getInitialChar(term) {
    const title = term.title || "";
    if (!title) return "#";
    const first = title.charAt(0).toUpperCase();

    // アルファベット
    if (/^[A-Z]$/.test(first)) return first;

    // ひらがな (3040-309F) / カタカナ (30A0-30FF)
    if (/^[\u3040-\u309F\u30A0-\u30FF]$/.test(first)) {
        return first;
    }

    // 数字
    if (/^[0-9]$/.test(first)) return first;

    // その他（漢字含む）はそのまま
    return first;
}

function renderJumpBar(chars) {
    const jumpBar = document.getElementById("jump-bar");
    if (!jumpBar) return;

    jumpBar.textContent = "";
    chars.forEach(char => {
        const link = createElement("a", {
            className: "glossary-jump-link",
            href: `#group-${char}`,
            textContent: char
        });
        // スムーズなスクロールのためのイベント
        link.addEventListener("click", (e) => {
            e.preventDefault();
            const targetId = `group-${char}`;
            const target = document.getElementById(targetId);
            if (target) {
                target.scrollIntoView();
                history.pushState(null, null, `#${targetId}`);
            }
        });
        jumpBar.appendChild(link);
    });
}

function renderGlossaryTerms(terms, displayMode) {
    const list = document.getElementById("term-list");
    if (!list) return;

    // 頭文字によるグルーピング
    const groups = {};
    terms.forEach(term => {
        const char = getInitialChar(term);
        if (!groups[char]) groups[char] = [];
        groups[char].push(term);
    });

    const sortedChars = Object.keys(groups).sort(termCollator.compare);

    // ジャンプバーの更新
    renderJumpBar(sortedChars);

    list.textContent = "";

    sortedChars.forEach(char => {
        const groupTerms = groups[char];
        const groupSection = createElement("section", {
            className: "glossary-group",
            id: `group-${char}`,
            children: [
                createElement("h2", { className: "glossary-group-header", textContent: char })
            ]
        });

        groupTerms.forEach((term, index) => {
            const anchorId = buildTermAnchorId(term, index);
            const isCompact = displayMode === "summary";

            const metaChildren = [];
            if (!isCompact) {
                const metaItems = [];
                if (term.fqn) {
                    metaItems.push(createElement("div", {children: [
                        createElement("span", {className: "meta-label", textContent: "完全修飾名"}),
                        createElement("span", {className: "meta-value", textContent: term.fqn}),
                    ]}));
                }
                if (term.simpleText) {
                    metaItems.push(createElement("div", {children: [
                        createElement("span", {className: "meta-label", textContent: "単純名"}),
                        createElement("span", {className: "meta-value", textContent: term.simpleText}),
                    ]}));
                }
                if (term.kind) {
                    metaItems.push(createElement("div", {children: [
                        createElement("span", {className: "meta-label", textContent: "種類"}),
                        createElement("span", {className: "meta-value", textContent: term.kind}),
                    ]}));
                }
                if (metaItems.length > 0) {
                    metaChildren.push(createElement("section", {
                        className: "jig-card jig-card--item weak",
                        children: metaItems
                    }));
                }
            }

            const article = createElement("article", {
                id: anchorId,
                className: `jig-card jig-card--type ${isCompact ? 'jig-card--compact' : ''}`,
                children: [
                    createElement("h3", {children: [createElement("a", {textContent: term.title || ""})]}),
                    ...metaChildren,
                    createElement("div", {className: "markdown", innerHTML: globalThis.Jig.markdown.parse(term.description || "")}),
                ]
            });
            groupSection.appendChild(article);
        });

        list.appendChild(groupSection);
    });
}

function renderFilteredTerms(terms, controls) {
    const filteredTerms = getFilteredTerms(terms, controls);
    const sortedTerms = sortTerms(filteredTerms, "name");
    // renderGlossaryTerms に表示モードを渡す
    renderTermSidebar(sortedTerms);
    renderGlossaryTerms(sortedTerms, controls.displayModeSelect?.value);
}

function renderMarkdownDescriptions() {
    Array.from(document.getElementsByClassName("markdown"))
        .forEach(node => {
            node.innerHTML = globalThis.Jig.markdown.parse(node.innerHTML);
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

    // 正規表現を関数の先頭で一度だけコンパイル
    let compiledRegex = null;
    if (searchMethod === "regex" && searchKeyword.trim() !== '') {
        try {
            compiledRegex = new RegExp(searchKeyword, 'i');
        } catch (e) {
            // 不正な正規表現: 全件不一致とする
            return [];
        }
    }

    const targetsToSearch = {
        title: controls.searchTargetName.checked,
        description: controls.searchTargetDescription.checked,
        fqn: controls.searchTargetFqn.checked,
        simpleText: controls.searchTargetSimple.checked,
        kind: controls.searchTargetKind.checked,
    };

    const showOnlyDomain = controls.showOnlyDomain && controls.showOnlyDomain.checked;
    const domainPackageRoots = getDomainPackageRoots();

    return terms.filter(term => {
        // 種類で絞り込む
        const kindText = term.kind || "";
        if (!kindVisibilityMap[kindText]) return false;

        // 説明文有無での判定
        const description = (term.description || "");
        if (!showEmptyDescription && !description) return false;

        // ドメインパッケージでの絞り込み
        if (showOnlyDomain && domainPackageRoots.length > 0) {
            const fqn = term.fqn || "";
            const isInDomainPackage = domainPackageRoots.some(root =>
                fqn === root || fqn.startsWith(root + ".") || fqn.startsWith(root + "#")
            );
            if (!isInDomainPackage) return false;
        }

        // キーワード検索
        if (searchKeyword) {
            const isMatch = Object.keys(targetsToSearch).some(prop => {
                if (!targetsToSearch[prop]) return false;

                const targetText = term[prop] || "";

                switch (searchMethod) {
                    case "exact":
                        return targetText.toLowerCase() === searchKeyword.toLowerCase();
                    case "regex":
                        return compiledRegex ? compiledRegex.test(targetText) : false;
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
            searchTargetName: document.getElementById('search-target-name'),
            searchTargetDescription: document.getElementById('search-target-description'),
            searchTargetFqn: document.getElementById('search-target-fqn'),
            searchTargetSimple: document.getElementById('search-target-simple'),
            searchTargetKind: document.getElementById('search-target-kind'),
            displayModeSelect: document.getElementById('display-mode-select'),
            showOnlyDomain: document.getElementById('show-only-domain'),
        };

        // domainPackageRootsが空の場合、show-only-domainチェックボックスをdisabledにする
        const domainPackageRoots = getDomainPackageRoots();
        if (controls.showOnlyDomain) {
            if (domainPackageRoots.length === 0) {
                controls.showOnlyDomain.disabled = true;
            }
        }

        const updateArticles = () => renderFilteredTerms(terms, controls);

        // "change" イベントを購読するコントロールを配列で管理
        const changeControls = [
            controls.showEmptyDescription,
            controls.showPackage,
            controls.showClass,
            controls.showMethod,
            controls.showField,
            controls.displayModeSelect,
            controls.searchTargetName,
            controls.searchTargetDescription,
            controls.searchTargetFqn,
            controls.searchTargetSimple,
            controls.searchTargetKind,
            controls.showOnlyDomain,
        ].filter(Boolean);

        changeControls.forEach(el => el.addEventListener("change", updateArticles));

        controls.searchInput.addEventListener("input", updateArticles);
        document.querySelectorAll('input[name="search-method"]')
            .forEach(radio => radio.addEventListener('change', updateArticles));

        const exportButton = document.getElementById("export-csv");
        if (exportButton) {
            exportButton.addEventListener("click", () => {
                const filteredTerms = getFilteredTerms(terms, controls);
                const csvText = buildGlossaryCsv(filteredTerms);
                globalThis.Jig.dom.downloadCsv(csvText, "glossary.csv");
            });
        }

        updateArticles();

        // ページ内リンクが確実に機能するように hashchange を監視
        const scrollToHash = () => {
            const hash = location.hash;
            if (hash) {
                const el = document.getElementById(hash.substring(1));
                if (el) el.scrollIntoView();
            }
        };
        window.addEventListener("hashchange", scrollToHash);
        // 初期表示時
        scrollToHash();
    });
}
// Test-only exports for Node; no-op in browsers.
if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        getInitialChar,
        renderJumpBar,
        sortTerms,
        getFilteredTerms,
        getGlossaryData,
        getDomainPackageRoots,
        normalizeGlossaryData,
        buildTermAnchorId,
        escapeCsvValue,
        buildGlossaryCsv,
        renderTermSidebar,
        renderGlossaryTerms,
        renderFilteredTerms,
        renderMarkdownDescriptions,
    };
}
