globalThis.Jig ??= {};

const GlossaryApp = (() => {
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
        const glossaryData = Jig.data.glossary.get();
        if (glossaryData && glossaryData.domainPackageRoots) {
            return glossaryData.domainPackageRoots;
        }
        return [];
    }

    function getGlossaryData() {
        const glossaryData = Jig.data.glossary.get();
        return normalizeGlossaryData(glossaryData) ?? [];
    }

    function buildTermAnchorId(term, index) {
        return term.fqn ? Jig.util.fqnToId("term", term.fqn) : `term-${index}`;
    }

    function createMetaItem(label, value) {
        return Jig.dom.createElement("div", {
            children: [
                Jig.dom.createElement("span", {className: "meta-label", textContent: label}),
                typeof value === "string"
                    ? Jig.dom.createElement("span", {className: "meta-value", textContent: value})
                    : value,
            ]
        });
    }

    function buildGlossaryCsv(terms) {
        const header = ["用語（英名）", "用語", "説明", "種類", "由来", "識別子"];
        const rows = terms.map(term => [
            term.simpleText ?? "",
            term.title ?? "",
            term.description ?? "",
            term.kind ?? "",
            term.origin ?? "",
            term.fqn ?? "",
        ]);
        return Jig.dom.buildCsv(header, rows);
    }

    function renderTermSidebar(terms) {
        const list = document.getElementById("term-sidebar-list");
        if (!list) return;

        list.innerHTML = "";
        const items = terms.map((term, index) => ({id: buildTermAnchorId(term, index), label: term.title || ""}));
        const section = Jig.dom.sidebar.section("用語一覧", items);
        if (!section) return;

        const links = section.querySelectorAll(".in-page-sidebar__link");
        links.forEach((link, i) => {
            const kind = terms[i]?.kind;
            if (kind) {
                link.setAttribute("data-kind", kind);
                link.setAttribute("data-kind-char", Jig.dom.kind.badgeChar(kind));
            }
        });

        list.appendChild(section);
    }

    function getInitialChar(term) {
        const title = term.title || "";
        if (!title) return "#";
        // ASCII 英字は大文字に揃え、それ以外（ひらがな/カタカナ/漢字/数字等）はそのまま先頭1文字を返す
        return title.charAt(0).toUpperCase();
    }

    function renderJumpBar(chars) {
        const jumpBar = document.getElementById("jump-bar");
        if (!jumpBar) return;

        jumpBar.textContent = "";
        chars.forEach(char => {
            const link = Jig.dom.createElement("a", {
                className: "glossary-jump-link",
                href: `#group-${char}`,
                textContent: char
            });
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

    function renderGlossaryTerms(terms, showAttributes) {
        const list = document.getElementById("term-list");
        if (!list) return;

        const groups = new Map();
        terms.forEach(term => {
            Jig.util.pushToMap(groups, getInitialChar(term), term);
        });

        const sortedChars = [...groups.keys()].sort(termCollator.compare);
        const domainRoots = getDomainPackageRoots();

        renderJumpBar(sortedChars);

        list.textContent = "";

        sortedChars.forEach(char => {
            const groupTerms = groups.get(char);
            const groupSection = Jig.dom.createElement("section", {
                className: "glossary-group",
                id: `group-${char}`,
                children: [
                    Jig.dom.createElement("h2", {className: "glossary-group-header", textContent: char})
                ]
            });

            groupTerms.forEach((term, index) => {
                const anchorId = buildTermAnchorId(term, index);
                const isCompact = !showAttributes;

                const metaChildren = [];
                const metaItems = [];
                if (term.fqn) metaItems.push(createMetaItem("完全修飾名", term.fqn));
                if (term.simpleText) metaItems.push(createMetaItem("単純名", term.simpleText));
                if (term.kind) metaItems.push(createMetaItem("種類", term.kind));
                if (term.origin) metaItems.push(createMetaItem("由来", term.origin));
                if ((term.kind === "クラス" || term.kind === "パッケージ") && term.fqn) {
                    const fqn = term.fqn;
                    const isInDomain = domainRoots.length > 0 && domainRoots.some(root =>
                        fqn === root || fqn.startsWith(root + ".")
                    );
                    if (isInDomain) {
                        metaItems.push(createMetaItem("関連ドキュメント", Jig.dom.createElement("a", {
                            className: "meta-value",
                            attributes: {href: "domain.html#" + Jig.util.fqnToId("domain", fqn)},
                            textContent: "ドメインモデル",
                        })));
                    }
                }
                if (term.sourcePath) {
                    const blobUrlPrefix = Jig.data.summary.getGit()?.blobUrlPrefix;
                    if (blobUrlPrefix) {
                        metaItems.push(createMetaItem("ソースコード", Jig.dom.createElement("a", {
                            className: "meta-value",
                            attributes: {href: `${blobUrlPrefix}/${term.sourcePath}`, target: "_blank", rel: "noopener"},
                            textContent: term.sourcePath,
                        })));
                    }
                }
                if (metaItems.length > 0) {
                    const metaCard = Jig.dom.card.item({extraClass: "weak"});
                    metaItems.forEach(item => metaCard.appendChild(item));
                    const details = Jig.dom.createElement("details", {
                        children: [
                            Jig.dom.createElement("summary", {className: "term-attributes-toggle", textContent: "属性情報"}),
                            metaCard
                        ]
                    });
                    if (!isCompact) details.open = true;
                    metaChildren.push(details);
                }

                const article = Jig.dom.card.type({
                    id: anchorId,
                    title: term.title || "",
                    kind: term.kind || "",
                    tagName: "article",
                });
                metaChildren.forEach(child => article.appendChild(child));
                article.appendChild(Jig.dom.createMarkdownElement(term.description || ""));
                groupSection.appendChild(article);
            });

            list.appendChild(groupSection);
        });
    }

    function renderFilteredTerms(terms, controls) {
        const filteredTerms = getFilteredTerms(terms, controls);
        const sortedTerms = sortTerms(filteredTerms, "name");
        renderTermSidebar(sortedTerms);
        renderGlossaryTerms(sortedTerms, controls.showAttributesCheckbox?.checked);
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
        const filterText = (controls.filterText || '').toLowerCase();
        const showOnlyDomain = controls.showOnlyDomain && controls.showOnlyDomain.checked;
        const domainPackageRoots = getDomainPackageRoots();

        return terms.filter(term => {
            const kindText = term.kind || "";
            if (!kindVisibilityMap[kindText]) return false;

            const description = term.description || "";
            if (!showEmptyDescription && !description) return false;

            if (showOnlyDomain && domainPackageRoots.length > 0) {
                const fqn = term.fqn || "";
                const isInDomainPackage = domainPackageRoots.some(root =>
                    fqn === root || fqn.startsWith(root + ".") || fqn.startsWith(root + "#")
                );
                if (!isInDomainPackage) return false;
            }

            if (filterText) {
                if (!(term.title || "").toLowerCase().includes(filterText)) return false;
            }

            return true;
        });
    }

    function init() {
        if (typeof document === "undefined" || !document.body.classList.contains("glossary")) {
            return;
        }

        const terms = getGlossaryData();

        const controls = {
            filterText: '',
            showEmptyDescription: document.getElementById("show-empty-description"),
            showPackage: document.getElementById("show-package"),
            showClass: document.getElementById("show-class"),
            showMethod: document.getElementById("show-method"),
            showField: document.getElementById("show-field"),
            showAttributesCheckbox: document.getElementById("show-attributes"),
            showOnlyDomain: document.getElementById("show-only-domain"),
        };

        const domainPackageRoots = getDomainPackageRoots();
        if (controls.showOnlyDomain && domainPackageRoots.length === 0) {
            controls.showOnlyDomain.disabled = true;
        }

        const updateArticles = () => renderFilteredTerms(terms, controls);

        const changeControls = [
            controls.showEmptyDescription,
            controls.showPackage,
            controls.showClass,
            controls.showMethod,
            controls.showField,
            controls.showAttributesCheckbox,
            controls.showOnlyDomain,
        ].filter(Boolean);

        changeControls.forEach(el => el.addEventListener("change", updateArticles));

        Jig.dom.sidebar.initCollapseBtn();
        Jig.dom.sidebar.initTextFilter('glossary-sidebar-filter', text => {
            controls.filterText = text;
            updateArticles();
        });

        const exportButton = document.getElementById("export-csv");
        if (exportButton) {
            exportButton.addEventListener("click", () => {
                const filteredTerms = getFilteredTerms(terms, controls);
                const csvText = buildGlossaryCsv(filteredTerms);
                Jig.dom.downloadCsv(csvText, "glossary.csv");
            });
        }

        updateArticles();

        // ページ内リンクが確実に機能するように hashchange を監視
        const scrollToHash = () => {
            const hash = location.hash;
            if (!hash) return;
            const targetId = hash.substring(1);
            let el = document.getElementById(targetId);
            if (!el) {
                // フィルタで非表示になっている場合、対象の用語を強制的に追加して再描画する
                const targetTerm = terms.find((t, i) => buildTermAnchorId(t, i) === targetId);
                if (targetTerm) {
                    const filteredTerms = getFilteredTerms(terms, controls);
                    const sortedTerms = sortTerms([...filteredTerms, targetTerm], "name");
                    renderTermSidebar(sortedTerms);
                    renderGlossaryTerms(sortedTerms, controls.showAttributesCheckbox?.checked);
                    el = document.getElementById(targetId);
                }
            }
            if (el) el.scrollIntoView();
        };
        window.addEventListener("hashchange", scrollToHash);
        scrollToHash();
    }

    return {
        init,
        getInitialChar,
        renderJumpBar,
        sortTerms,
        getFilteredTerms,
        getGlossaryData,
        getDomainPackageRoots,
        normalizeGlossaryData,
        buildTermAnchorId,
        buildGlossaryCsv,
        renderTermSidebar,
        renderGlossaryTerms,
        renderFilteredTerms,
    };
})();

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", () => {
        GlossaryApp.init();
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = GlossaryApp;
}
