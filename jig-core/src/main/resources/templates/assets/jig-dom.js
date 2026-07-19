globalThis.Jig ??= {};

// ブラウザバックなどで該当要素に移動する
// Safariなどではブラウザバックでも移動するが、ChromeやEdgeだと移動しない。
// なのでpopstateイベントでlocationからhashを取得し、hashがある場合はその要素に移動する
if (typeof window !== 'undefined') {
    window.addEventListener("popstate", function (event) {
        const hash = event.target.location.hash;

        if (hash) {
            const anchor = document.getElementById(hash.substring(1))
            if (anchor) {
                anchor.scrollIntoView();
            }
        }
    });
}

globalThis.Jig.dom = (() => {

    // --- Base DOM utility ---

    function createElement(tagName, options = {}) {
        const element = document.createElement(tagName);
        if (options.className) element.className = options.className;
        if (options.id) element.id = options.id;
        if (options.textContent != null) element.textContent = options.textContent;
        if (options.innerHTML != null) element.innerHTML = options.innerHTML;
        if (options.attributes) {
            for (const [key, value] of Object.entries(options.attributes)) {
                element.setAttribute(key, value);
            }
        }
        // i18n: true で data-i18n マーカーを付与（textContent をキーとして翻訳対象にする）
        if (options.i18n) {
            element.setAttribute("data-i18n", options.i18n === true ? "" : String(options.i18n));
        }
        if (options.style) {
            Object.assign(element.style, options.style);
        }
        if (options.children) {
            options.children.forEach(child => {
                // 文字列を指定することもあるのでappendChildではなくappendを使用する
                if (child) element.append(child);
            });
        }
        return element;
    }

    function createCell(text, className) {
        return createElement("td", {className, textContent: text});
    }

    /**
     * textContent を i18n キー（日本語）として翻訳対象にする要素を生成する高レベル API。
     * createElement(tag, {textContent: key, i18n: true, ...options}) のショートカット。
     * children を伴う複合要素や label の mixed content では createElement に i18n: true を直接渡す。
     */
    function i18nText(tagName, key, options = {}) {
        const el = createElement(tagName, {...options, textContent: key, i18n: true});
        el.dataset.i18nOriginal = key;
        return el;
    }

    // --- Markdown ---

    function parseMarkdown(markdown) {
        const source = markdown != null ? String(markdown) : "";
        if (globalThis.marked && typeof globalThis.marked.parse === "function") {
            return globalThis.marked.parse(source);
        }
        return source;
    }

    // Javadoc 由来テキストの XSS を防ぐ。DOMPurify が無い場合は null を返し、呼び出し側で安全側に倒す
    function sanitizeHtml(html) {
        const purifier = globalThis.DOMPurify;
        if (purifier && typeof purifier.sanitize === "function") {
            return purifier.sanitize(html);
        }
        return null;
    }

    function createMarkdownElement(markdown) {
        const source = markdown != null ? String(markdown) : "";
        const sanitized = sanitizeHtml(parseMarkdown(source));
        // サニタイザ不在（CDN 不達など）で innerHTML に入れるのは危険なので textContent で表示する
        const element = sanitized != null
            ? createElement("div", {className: "markdown", innerHTML: sanitized})
            : createElement("div", {className: "markdown", textContent: source});
        // Javadoc に書いた ```mermaid コードブロックをダイアグラムとして描画する
        globalThis.Jig?.mermaid?.renderMarkdownDiagrams?.(element);
        return element;
    }

    // --- CSV utility ---

    function escapeCsvValue(value) {
        const text = String(value ?? "").replace(/\r\n|\r/g, "\n");
        return `"${text.replace(/"/g, "\"\"")}"`;
    }

    function buildCsv(header, rows) {
        return [header, ...rows].map(row => row.map(escapeCsvValue).join(",")).join("\r\n");
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

    // --- Kind badge ---

    const KIND_BADGE = {"パッケージ": "P", "クラス": "C", "メソッド": "M", "フィールド": "F"};

    function kindBadgeChar(kind) {
        return KIND_BADGE[kind] ?? (kind ? kind.charAt(0).toUpperCase() : "?");
    }

    function kindBadgeElement(kind) {
        return createElement("span", {
            className: "kind-badge",
            attributes: {"data-kind": kind},
            textContent: kindBadgeChar(kind),
        });
    }

    // --- Type link ---

    let typeLinkResolver = null;

    function setTypeLinkResolver(resolver) {
        typeLinkResolver = (typeof resolver === "function") ? resolver : null;
    }

    function clearTypeLinkResolver() {
        typeLinkResolver = null;
    }

    function getTypeLinkResolver() {
        return typeLinkResolver;
    }

    // Jig.glossary (jig-glossary.js) のロードが必要
    function createTypeLink(fqn, className = undefined) {
        // 配列型（例: Hoge[], Hoge[][]）はベース型で解決し、[] を付け直す
        const arraySuffix = fqn.match(/(\[\])+$/)?.[0] ?? '';
        const baseFqn = arraySuffix ? fqn.slice(0, -arraySuffix.length) : fqn;

        const resolved = typeLinkResolver?.(baseFqn);
        const title = (resolved?.text ?? globalThis.Jig.glossary.getTypeTerm(baseFqn).title) + arraySuffix;
        const classes = [className, resolved?.className].filter(Boolean).join(' ') || undefined;
        if (resolved?.href) {
            return createElement('a', {
                className: [classes, 'type-ref-link'].filter(Boolean).join(' '),
                attributes: {href: resolved.href},
                textContent: title
            });
        }
        return createElement('span', {
            className: classes,
            textContent: title
        });
    }

    function createElementForTypeRef(typeRef, className = undefined) {
        if (typeRef.typeArgumentRefs && typeRef.typeArgumentRefs.length) {
            const typeElements = createTypeLink(typeRef.fqn);
            const argumentElements = typeRef.typeArgumentRefs
                .map(argumentTypeRef => createElementForTypeRef(argumentTypeRef))
                // カンマを挟む。HTML Elementが文字列になってしまうのでjoinは使えない。
                .flatMap((v, i) => i ? [', ', v] : [v]);

            return createElement("span", {
                className: className,
                children: [typeElements, '<', ...argumentElements, '>']
            });
        }

        return createTypeLink(typeRef.fqn, className);
    }

    // --- Type detail builders ---

    function createParameterElement(param) {
        return createElement("span", {
            children: param.nameSource === 'METHOD_PARAMETERS'
                ? [param.name + ': ', createElementForTypeRef(param.typeRef)]
                : [createElementForTypeRef(param.typeRef)]
        });
    }

    function createMethodItem(method) {
        const methodTerm = globalThis.Jig.glossary.getMethodTerm(method.fqn, true);

        const paramElements = method.parameters
            .map(param => createParameterElement(param))
            .flatMap((el, i) => i ? [', ', el] : [el]);

        const signatureEl = createElement("div", {
            className: "method-signature",
            children: [
                createElement("span", {
                    className: "method-name" + (method.isDeprecated ? " deprecated" : ""),
                    textContent: methodTerm.title
                }),
                '(',
                ...paramElements,
                ')',
                createElement("span", {className: "method-return-sep", textContent: ":"}),
                createElementForTypeRef(method.returnTypeRef)
            ]
        });

        const children = [signatureEl];
        if (methodTerm.description) {
            children.push(createMarkdownElement(methodTerm.description));
        }

        return createElement("div", {
            className: "method-item",
            children
        });
    }

    function createMethodIOSection(parameters, returnTypeRef) {
        const inputDd = parameters.length > 0
            ? createElement("dd", {
                className: "entrypoint-item__params",
                children: parameters.flatMap(param => [
                    createElementForTypeRef(param.typeRef),
                    createElement("span", {
                        className: "entrypoint-item__param-name",
                        textContent: param.nameSource === 'METHOD_PARAMETERS' ? param.name : ''
                    })
                ])
            })
            : createElement("dd", {className: "entrypoint-item__empty", textContent: "-"});

        return createElement("dl", {
            className: "entrypoint-item__io",
            children: [
                i18nText("dt", "入力"),
                inputDd,
                i18nText("dt", "出力"),
                createElement("dd", {children: [createElementForTypeRef(returnTypeRef)]})
            ]
        });
    }

    function createFieldItem(field) {
        return createElement("div", {
            className: "field-item",
            children: [
                createElement("div", {
                    className: "field-signature",
                    children: [
                        createElement("span", {
                            className: "field-name" + (field.isDeprecated ? " deprecated" : ""),
                            textContent: field.name
                        }),
                        createElement("span", {className: "field-type-sep", textContent: ":"}),
                        createElementForTypeRef(field.typeRef)
                    ]
                })
            ]
        });
    }

    function createMemberSection(items, title, className, createItem) {
        return createElement("section", {
            className,
            children: [
                ...(title !== undefined ? [i18nText("h4", title)] : []),
                ...items.map(createItem)
            ]
        });
    }

    function createFieldsList(fields, options = {}) {
        if (fields.length === 0) return null;
        const title = options.showTitle !== false ? "フィールド" : undefined;
        return createMemberSection(fields, title, "methods-section fields", createFieldItem);
    }

    function createMethodsList(kind, methods, options = {}) {
        if (methods.length === 0) return null;
        const title = options.showTitle !== false ? kind : undefined;
        return createMemberSection(methods, title, "methods-section", createMethodItem);
    }

    // --- Card builders ---

    function createItemCard({id, title, tagName = "section", extraClass} = {}) {
        return createElement(tagName, {
            id,
            className: ["jig-card", "jig-card--item", extraClass].filter(Boolean).join(" "),
            children: title !== undefined ? [i18nText("h4", title)] : []
        });
    }

    function createTypeCard({id, title, fqn, kind, attributes, titleSuffix, tagName = "section", extraClass} = {}) {
        const titleEl = typeof title === 'string' ? createElement("span", {textContent: title}) : title;
        const titleContent = id
            ? createElement("a", {className: "card-title-anchor", attributes: {href: `#${id}`}, children: [titleEl]})
            : titleEl;
        const h3Children = kind !== undefined ? [kindBadgeElement(kind), titleContent] : [titleContent];
        if (titleSuffix) h3Children.push(titleSuffix);

        const card = createElement(tagName, {
            id,
            className: ["jig-card", "jig-card--type", extraClass].filter(Boolean).join(" "),
            attributes,
            children: [createElement("h3", {children: h3Children})]
        });

        if (fqn != null) {
            card.appendChild(typeof fqn === 'string'
                ? createElement("div", {className: "fully-qualified-name", textContent: fqn})
                : fqn);
        }

        return card;
    }

    // --- Sidebar ---

    // 折りたたみ状態の表現（hiddenクラスとトグルのaria）を一箇所で扱う
    function applySidebarListState(list, toggle, expanded) {
        list.classList.toggle("in-page-sidebar__links--hidden", !expanded);
        if (toggle) {
            toggle.setAttribute("aria-expanded", String(expanded));
            toggle.setAttribute("aria-label", expanded ? "折りたたむ" : "展開");
        }
    }

    function setSidebarListExpanded(list, toggle, expanded, recursive = false) {
        applySidebarListState(list, toggle, expanded);
        // 閉じるときは配下もすべて閉じ、次に開いたとき1階層だけ開くようにする
        // recursive指定時は開くときも配下をすべて開く（Alt+クリック用）
        if (!expanded || recursive) {
            list.querySelectorAll(".in-page-sidebar__links").forEach(descendant => {
                const descendantToggle = descendant.previousElementSibling?.querySelector(".in-page-sidebar__toggle");
                applySidebarListState(descendant, descendantToggle, expanded);
            });
        }
    }

    function createSidebarToggle(targetEl) {
        const toggle = createElement("button", {
            className: "in-page-sidebar__toggle",
            attributes: {"aria-expanded": "true", "aria-label": "折りたたむ"}
        });
        toggle.addEventListener("click", (e) => {
            const collapsing = toggle.getAttribute("aria-expanded") === "true";
            setSidebarListExpanded(targetEl, toggle, !collapsing, e.altKey);
        });
        return toggle;
    }

    function buildCollapsibleTitle(title, links) {
        return createElement("p", {
            className: "in-page-sidebar__title in-page-sidebar__title--collapsible",
            children: [i18nText("span", title), createSidebarToggle(links)]
        });
    }

    /**
     * サイドバーのリーフ（リンク1つのli）を生成する
     * @param {string} href
     * @param {string} label
     */
    function createSidebarLeaf(href, label) {
        return createElement("li", {
            className: "in-page-sidebar__item",
            children: [
                createElement("a", {
                    className: "in-page-sidebar__link",
                    attributes: {href},
                    textContent: label
                })
            ]
        });
    }

    function createSection(items) {
        if (!items || items.length === 0) return null;

        const links = createElement("ul", {
            className: "in-page-sidebar__links",
            children: items.map(({id, label}) => createSidebarLeaf("#" + id, label))
        });

        return createElement("section", {
            className: "in-page-sidebar__section",
            children: [links]
        });
    }

    function renderSection(container, items) {
        if (!container) return;
        const section = createSection(items);
        if (section) {
            container.appendChild(section);
        }
    }

    /**
     * サイドバーのツリーセクションを描画する。3種類の要素で構成する:
     * - グループ: ページ内の大きな塊（例: リクエストハンドラ）。折りたたみ可能で、背景色で他と区別する
     * - パッケージ: FQNの1階層。折りたたみと、packageHref によるメインセクションへのリンクを持つ。
     *   用語のない単一子パッケージ（item無し）は省略し、最深のパッケージのみ表示する
     * - リーフ: renderLeaf で描画されるアイテム（クラスやメソッド）
     *
     * items が空の場合は何も描画しない。
     *
     * @template T
     * @param {Element} container
     * @param {Object} options
     * @param {string} options.title - グループ名
     * @param {T[]} options.items
     * @param {function(T): string} options.getFqn - itemの型FQNを返す関数
     * @param {function(T): Element} options.renderLeaf - リーフのli要素を生成する関数
     * @param {function({fqn: string, items: T[], children: Object[]}): (string|null)} [options.packageHref]
     *        パッケージノードのリンク先を返す関数。nullを返すとリンクなしのラベルになる
     * @param {boolean} [options.showTitle=true] - falseの場合、グループ見出し（背景色・積み重ね
     *        ピン留め含む）を描画せず、パッケージ階層のulを直接containerへ追加する。
     *        グループが1つしかなく見出しが冗長なページで使う
     */
    function renderTreeSection(container, {title, items, getFqn, renderLeaf, packageHref, showTitle = true}) {
        if (!container || !items || items.length === 0) return;
        const glossary = globalThis.Jig.glossary;
        const roots = globalThis.Jig.util.buildPackageTree(items, getFqn);

        function renderNode(node) {
            // 用語のない単一子パッケージ（item無し）は省略し、最深のパッケージのみ表示する
            let current = node;
            while (current.children.length === 1 && current.items.length === 0 && !glossary.findTerm(current.fqn)) {
                current = current.children[0];
            }

            const childList = createElement("ul", {className: "in-page-sidebar__links"});
            current.children.forEach(child => childList.appendChild(renderNode(child)));
            current.items.forEach(item => childList.appendChild(renderLeaf(item)));

            const labelText = glossary.getPackageTerm(current.fqn).title;
            const href = packageHref ? packageHref(current) : null;
            const label = href
                ? createElement("a", {
                    className: "in-page-sidebar__package-link",
                    attributes: {href},
                    textContent: labelText
                })
                : createElement("span", {textContent: labelText});

            return createElement("li", {
                className: "in-page-sidebar__item",
                children: [
                    createElement("div", {
                        className: "in-page-sidebar__item-header",
                        children: [label, createSidebarToggle(childList)]
                    }),
                    childList
                ]
            });
        }

        const list = createElement("ul", {className: "in-page-sidebar__links"});
        if (roots.length === 1 && roots[0].fqn === "(default)" && roots[0].children.length === 0) {
            // パッケージを持たない項目（テーブル名など）だけの場合は、階層を挟まずリーフを直接並べる
            roots[0].items.forEach(item => list.appendChild(renderLeaf(item)));
        } else {
            roots.forEach(root => list.appendChild(renderNode(root)));
        }

        if (!showTitle) {
            // グループが1つしかなく見出しが冗長な場合、見出し・背景色・ピン留めなしで
            // パッケージ階層のulをそのまま追加する
            container.appendChild(list);
            return;
        }

        const titleEl = buildCollapsibleTitle(title, list);
        titleEl.classList.add("in-page-sidebar__title--group");
        const toggle = titleEl.querySelector(".in-page-sidebar__toggle");

        container.appendChild(createElement("section", {
            className: "in-page-sidebar__section in-page-sidebar__section--group",
            children: [titleEl, list]
        }));

        const scroller = sidebarScrollerOf(container);
        recomputeGroupTitleOffsets(scroller);
        initGroupTitlePinning(scroller, titleEl, toggle, list);
    }

    /**
     * 開閉可能な子リストを持たない、単一リンクのグループをサイドバーに追加する。
     * 他のグループ（renderTreeSection）と同じ背景色・積み重ねピン留めの並びにしたい、
     * ツリー展開の必要がない項目（例: 単一ページへのリンクのみのセクション）で使う。
     *
     * @param {Element} container
     * @param {Object} options
     * @param {string} options.title - リンクのラベル
     * @param {string} options.href
     */
    function renderLinkGroup(container, {title, href}) {
        if (!container) return;

        const titleEl = createElement("p", {
            className: "in-page-sidebar__title in-page-sidebar__title--group",
            children: [createElement("a", {
                className: "in-page-sidebar__link",
                attributes: {href},
                textContent: title
            })]
        });

        container.appendChild(createElement("section", {
            className: "in-page-sidebar__section in-page-sidebar__section--group",
            children: [titleEl]
        }));

        recomputeGroupTitleOffsets(sidebarScrollerOf(container));
    }

    // グループ見出しがスクロール範囲外に押し出された場合も下部に積み重なって留まるよう、
    // スクロール領域内の全グループ見出しの積み重ねオフセットを再計算する
    function recomputeGroupTitleOffsets(scroller) {
        const groupTitles = [...scroller.querySelectorAll(".in-page-sidebar__title--group")];
        groupTitles.forEach((groupTitle, index) => {
            groupTitle.style.bottom = `calc(${groupTitles.length - 1 - index} * var(--group-title-height))`;
        });
    }

    const GROUP_TITLE_PINNED_CLASS = "in-page-sidebar__title--pinned";
    // ピン留めのscrollリスナーをスクロール領域ごとに1つに保つ
    const pinningInitializedScrollers = new WeakSet();

    // グループ見出しの sticky / ピン留めの基準となるスクロール領域。
    // 描画先がスクロール領域内のネストした要素でも動作するよう closest で解決する
    function sidebarScrollerOf(container) {
        return container.closest(".in-page-sidebar__list") ?? container;
    }

    function isContentBelowView(scroller, list) {
        if (typeof scroller.getBoundingClientRect !== "function") return false;
        const scrollerRect = scroller.getBoundingClientRect();
        if (!scrollerRect.height) return false;
        if (!list || list.classList.contains("in-page-sidebar__links--hidden")) return false;
        return list.getBoundingClientRect().top >= scrollerRect.bottom - 1;
    }

    // スクロール領域内の全グループ見出しのピン留め状態を更新する。
    // 見出しの次の要素がそのグループの内容リスト（renderTreeSectionの構造）
    function updatePinnedStates(scroller) {
        scroller.querySelectorAll(".in-page-sidebar__title--group").forEach(titleEl => {
            titleEl.classList.toggle(GROUP_TITLE_PINNED_CLASS, isContentBelowView(scroller, titleEl.nextElementSibling));
        });
    }

    /**
     * グループ内容がスクロール範囲外（下）にあり見出しだけが下部に留まっている状態を
     * 「閉じている」扱いにする。閉じた見た目のクラスを付け、クリックで展開して
     * グループが見える位置までスクロールする。
     */
    function initGroupTitlePinning(scroller, titleEl, toggle, list) {
        // ピン留め中はトグルの折りたたみ動作より優先するため、captureで処理する
        titleEl.addEventListener("click", (e) => {
            const collapsed = list.classList.contains("in-page-sidebar__links--hidden");
            const pinned = titleEl.classList.contains(GROUP_TITLE_PINNED_CLASS);
            e.stopPropagation();

            if (!collapsed && !pinned) {
                // 内容が見えている状態でのクリックは折りたたむ（展開時のクリックと対称にする）
                setSidebarListExpanded(list, toggle, false);
                updatePinnedStates(scroller);
                return;
            }

            if (collapsed) {
                setSidebarListExpanded(list, toggle, true, e.altKey);
            }

            // グループ見出しが上部に来る位置までスクロールして内容を見せる
            if (typeof scroller.getBoundingClientRect === "function") {
                const scrollerRect = scroller.getBoundingClientRect();
                const titleRect = titleEl.getBoundingClientRect();
                const listGap = typeof globalThis.getComputedStyle === "function"
                    ? parseFloat(globalThis.getComputedStyle(scroller).rowGap) || 0
                    : 0;
                scroller.scrollTop += list.getBoundingClientRect().top
                    - scrollerRect.top - titleRect.height - listGap;
            }
            updatePinnedStates(scroller);
        }, true);

        if (!pinningInitializedScrollers.has(scroller)) {
            pinningInitializedScrollers.add(scroller);
            scroller.addEventListener("scroll", () => updatePinnedStates(scroller));
        }
        updatePinnedStates(scroller);
    }

    function initSidebarTextFilter(inputId, onChange) {
        const input = document.getElementById(inputId);
        if (!input) return;
        input.addEventListener('input', () => onChange(input.value.trim()));
    }

    // 折りたたまれた祖先（in-page-sidebar__links--hidden）を展開し、対応するトグルの状態も合わせる
    function expandSidebarAncestors(sidebar, link) {
        let el = link.parentElement;
        while (el && el !== sidebar) {
            if (el.classList.contains("in-page-sidebar__links--hidden")) {
                const toggle = el.previousElementSibling?.querySelector(".in-page-sidebar__toggle");
                setSidebarListExpanded(el, toggle, true);
            }
            el = el.parentElement;
        }
    }

    // サイドバーのスクロール領域内で該当リンクを表示する。メインコンテンツのスクロール位置は動かさない
    function scrollSidebarLinkIntoView(sidebar, link) {
        const container = sidebar.querySelector(".in-page-sidebar__list") || sidebar;
        const containerRect = container.getBoundingClientRect();
        const linkRect = link.getBoundingClientRect();
        if (linkRect.top >= containerRect.top && linkRect.bottom <= containerRect.bottom) return;
        container.scrollTop += (linkRect.top + linkRect.bottom) / 2 - (containerRect.top + containerRect.bottom) / 2;
    }

    // 指定したhashに対応するサイドバーリンクをハイライトし、折りたたまれた祖先を展開する。
    // scrollIntoSidebar が true なら該当リンクをサイドバー内に表示する。
    function applyActiveSidebarLink(sidebar, hash, scrollIntoSidebar = false) {
        sidebar.querySelectorAll(".in-page-sidebar__link--active")
            .forEach(el => el.classList.remove("in-page-sidebar__link--active"));

        if (!hash || hash === "#") return;

        // 同一アンカーへのリンクが複数グループに現れることがあるため、全て同期する
        const links = [
            ...sidebar.querySelectorAll(".in-page-sidebar__link"),
            ...sidebar.querySelectorAll(".in-page-sidebar__package-link"),
        ].filter(a => a.getAttribute("href") === hash);
        if (links.length === 0) return;

        links.forEach(link => {
            link.classList.add("in-page-sidebar__link--active");
            expandSidebarAncestors(sidebar, link);
        });
        if (scrollIntoSidebar) scrollSidebarLinkIntoView(sidebar, links[0]);
    }

    // location.hash に対応するサイドバーリンクをハイライトする。hashchangeやブラウザの戻る/進み、
    // 初回ロード時の同期に使う
    function syncActiveSidebarLink(scrollIntoSidebar = false) {
        if (typeof document === "undefined") return;
        const sidebar = document.querySelector(".in-page-sidebar");
        if (!sidebar) return;
        applyActiveSidebarLink(sidebar, location.hash, scrollIntoSidebar);
    }

    // サイドバー内のリンククリックを即座にハイライトする。移動先のレンダリングが重い場合、
    // hashchangeの発火（≒描画完了後）を待つとハイライトが遅れて見えるため、
    // クリック時点でリンク自身のhrefを元に先行してハイライトする。
    // hashchangeによる同期（syncActiveSidebarLink）はブラウザの戻る/進み用に引き続き動作する
    function initSidebarClickHighlight(sidebar) {
        if (!sidebar || sidebar.dataset.clickHighlightInitialized) return;
        sidebar.dataset.clickHighlightInitialized = "true";
        sidebar.addEventListener("click", (e) => {
            const link = e.target.closest("a[href^='#']");
            if (!link) return;
            applyActiveSidebarLink(sidebar, link.getAttribute("href"));
        });
    }

    function initSidebarCollapseBtn() {
        const collapseBtn = document.getElementById('sidebar-collapse-btn');
        if (!collapseBtn || collapseBtn.dataset.initialized) return;
        collapseBtn.dataset.initialized = 'true';
        const nav = collapseBtn.closest('nav');
        if (!nav) return;
        const setCollapsed = (collapsed) => {
            nav.classList.toggle('sidebar--collapsed', collapsed);
            collapseBtn.setAttribute('aria-expanded', String(!collapsed));
        };
        collapseBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            setCollapsed(!nav.classList.contains('sidebar--collapsed'));
        });
        nav.addEventListener('click', () => {
            if (nav.classList.contains('sidebar--collapsed')) setCollapsed(false);
        });
        // 縦並びレイアウト（common.css の max-width: 900px と同期）ではデフォルト折りたたみ
        const narrowLayout = typeof window !== "undefined" && window.matchMedia
            ? window.matchMedia('(max-width: 900px)') : null;
        if (narrowLayout) {
            setCollapsed(narrowLayout.matches);
            narrowLayout.addEventListener('change', (e) => setCollapsed(e.matches));
        }
    }

    // Altキー押下中はトグルホバー時に見た目を変え、配下もまとめて開閉することを示す
    function initSidebarAltKeyIndicator() {
        if (typeof window === "undefined" || typeof document === "undefined") return;
        if (document.body.dataset.altKeyIndicatorInitialized) return;
        document.body.dataset.altKeyIndicatorInitialized = "true";
        const setAltHeld = (held) => document.body.classList.toggle("jig-alt-held", held);
        window.addEventListener("keydown", (e) => {
            if (e.key === "Alt") setAltHeld(true);
        });
        window.addEventListener("keyup", (e) => {
            if (e.key === "Alt") setAltHeld(false);
        });
        window.addEventListener("blur", () => setAltHeld(false));
        // OSにAlt単独押下を奪われてkeyupが届かない場合があるため、マウス移動時にaltKeyの実値で再同期する
        window.addEventListener("mousemove", (e) => setAltHeld(e.altKey));
    }

    // --- Tab section ---

    function buildTabSection(tabDefs, options = {}) {
        const {className, initialActiveId, onTabChange} = options;
        const tabsBar = createElement("div", {className: "jig-tabs"});
        const panels = {};
        const buttons = [];

        const activeId = initialActiveId ?? tabDefs[0]?.id;

        tabDefs.forEach(tab => {
            panels[tab.id] = createElement("div", {
                className: ["jig-tab-panel", tab.id !== activeId ? "hidden" : null].filter(Boolean).join(" ")
            });
            const btn = i18nText("button", tab.label, {
                className: ["jig-tab", tab.id === activeId ? "active" : null].filter(Boolean).join(" "),
            });
            buttons.push(btn);
            btn.addEventListener('click', () => {
                buttons.forEach(b => b.classList.remove('active'));
                Object.values(panels).forEach(p => p.classList.add('hidden'));
                btn.classList.add('active');
                panels[tab.id].classList.remove('hidden');
                if (onTabChange) onTabChange(tab.id);
            });
            tabsBar.appendChild(btn);
        });

        const panelEls = Object.values(panels);
        const section = createElement("div", {
            className,
            children: [tabsBar, ...panelEls],
        });
        return {panels, section};
    }

    // --- Table ---

    function renderTableRows(tableId, items, buildRow, {clear = false} = {}) {
        const tableBody = document.querySelector(`#${tableId} tbody`);
        if (!tableBody) return;
        if (clear) tableBody.innerHTML = "";
        const fragment = document.createDocumentFragment();
        items.forEach(item => {
            const row = createElement("tr");
            buildRow(row, item);
            fragment.appendChild(row);
        });
        tableBody.appendChild(fragment);
    }

    function setupSortableTables() {
        function sortTable(event) {
            const headerColumn = event.target;
            const table = headerColumn.closest("table");
            const columnIndex = Array.from(headerColumn.parentNode.children).indexOf(headerColumn);

            const rows = Array.from(table.querySelectorAll("tbody tr"));

            const orderFlag = headerColumn.dataset.orderFlag === "true";

            let type = "string";
            const firstRow = rows[0];
            if (firstRow) {
                const cell = firstRow.cells[columnIndex];
                if (cell && cell.classList.contains("number")) {
                    type = "number";
                }
            }

            rows.sort(function (a, b) {
                const aValue = a.cells[columnIndex]?.textContent ?? "";
                const bValue = b.cells[columnIndex]?.textContent ?? "";

                // 数値は降順、文字は昇順
                if (type === "number") {
                    const aNumber = parseFloat(aValue) || 0;
                    const bNumber = parseFloat(bValue) || 0;
                    return (aNumber - bNumber) * (orderFlag ? 1 : -1);
                }
                return (aValue.localeCompare(bValue)) * (orderFlag ? -1 : 1);
            });

            rows.forEach(row => table.querySelector("tbody").appendChild(row));

            headerColumn.dataset.orderFlag = (!orderFlag).toString();
        }

        document.querySelectorAll("table.sortable").forEach(table => {
            const headers = table.querySelectorAll("thead th");
            headers.forEach(header => {
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

    // --- Depth aggregation control ---
    // パッケージ関連図・ライブラリ依存図の階層集約セレクト（集約なし・深さN）とその
    // ▲▼ステップボタンで共通に使う。両画面で選択肢生成・活性制御・step操作を揃えるための実装。

    function buildDepthOptions(maxDepth) {
        const options = [{value: "0", text: "集約なし"}];
        for (let depth = 1; depth <= maxDepth; depth += 1) {
            options.push({value: String(depth), text: `深さ${depth}`});
        }
        return options;
    }

    function renderDepthOptions(select, maxDepth, value) {
        if (!select) return;
        select.innerHTML = "";
        buildDepthOptions(maxDepth).forEach(option => {
            select.appendChild(createElement("option", {textContent: option.text, attributes: {value: option.value}}));
        });
        select.value = String(Math.min(Math.max(Number(value) || 0, 0), maxDepth));
    }

    function updateDepthButtonStates(select, upButton, downButton) {
        if (!select || !upButton || !downButton) return;
        const options = Array.from(select.options);
        const currentIndex = options.findIndex(opt => opt.value === select.value);
        upButton.disabled = currentIndex <= 0;
        downButton.disabled = currentIndex < 0 || currentIndex >= options.length - 1;
    }

    function stepDepthByIndex(select, delta) {
        if (!select) return;
        const options = Array.from(select.options);
        const currentIndex = options.findIndex(opt => opt.value === select.value);
        const nextIndex = currentIndex + delta;
        if (nextIndex < 0 || nextIndex >= options.length) return;
        select.value = options[nextIndex].value;
        select.dispatchEvent(new Event("change"));
    }

    // --- Common UI setup ---

    function normalizeNavigationHref(href) {
        return String(href || "").replace(/^\.\//, "");
    }

    /**
     * hover で開閉するヘッダ用ドロップダウンナビ (.jig-header-nav) の構造を作る共通ヘルパ。
     * setupHeaderNavigation / setupLanguageSwitcher の両方から使う。
     *
     * @param {HTMLElement} triggerEl 既に組み立て済みのトリガー要素 (.jig-header-nav__trigger)
     * @param {string} [modifierClass] バリエーション用の追加クラス名（例: "jig-lang-switcher"）
     * @returns {{wrapper: HTMLElement, dropdown: HTMLElement}}
     */
    function createDropdownNav(triggerEl, modifierClass) {
        const dropdown = createElement("ul", {
            className: ["jig-header-nav__dropdown", modifierClass && `${modifierClass}__dropdown`].filter(Boolean).join(" "),
            attributes: {role: "list"}
        });
        const wrapper = createElement("div", {
            className: ["jig-header-nav", modifierClass].filter(Boolean).join(" "),
            children: [triggerEl, dropdown]
        });
        return {wrapper, dropdown};
    }

    function appendDropdownItem(dropdown, child, isCurrent) {
        dropdown.appendChild(createElement("li", {
            className: "jig-header-nav__item" + (isCurrent ? " jig-header-nav__item--current" : ""),
            children: [child]
        }));
    }

    function setupHeaderNavigation() {
        if (document.body.classList.contains("index")) return;

        const navigationData = globalThis.Jig.data.navigation.get();
        if (!navigationData || !Array.isArray(navigationData.links) || navigationData.links.length === 0) return;

        const header = document.querySelector("header.top") || document.querySelector("header");
        if (!header) return;
        if (header.querySelector(".jig-header-nav")) return;

        const pageTitleEl = header.querySelector(".jig-page-title");
        if (!pageTitleEl) return;

        const currentFileName = (location.pathname.split("/").pop() || "");
        const normalizedCurrent = normalizeNavigationHref(currentFileName);

        // ナビゲーションデータの当該リンクのラベルが locale 対応済みなのでそれを使う。
        // 該当が見つからない場合はテンプレート静的テキストにフォールバック。
        const currentLink = navigationData.links.find(
            link => link && normalizeNavigationHref(link.href) === normalizedCurrent
        );
        const triggerLabel = (currentLink && currentLink.label != null)
            ? String(currentLink.label)
            : pageTitleEl.textContent;

        // ラベルは日本語キー（textContent）で描画し i18n マーカーで翻訳対象にする
        const trigger = i18nText("span", triggerLabel, {className: "jig-header-nav__trigger"});
        const {wrapper, dropdown} = createDropdownNav(trigger);

        navigationData.links.forEach(link => {
            if (!link) return;
            const href = normalizeNavigationHref(link.href);
            const label = link.label != null ? String(link.label) : href;
            if (!href) return;

            const isCurrent = (href === normalizedCurrent);
            const child = isCurrent
                ? i18nText("span", label)
                : i18nText("a", label, {attributes: {href}});
            appendDropdownItem(dropdown, child, isCurrent);
        });

        pageTitleEl.replaceWith(wrapper);
    }

    function setupLanguageSwitcher() {
        const i18n = globalThis.Jig?.i18n;
        if (!i18n || typeof i18n.setLanguage !== "function") return;

        const header = document.querySelector("header.top") || document.querySelector("header");
        if (!header) return;
        if (header.querySelector(".jig-lang-switcher")) return;

        const langs = i18n.availableLanguages();
        if (!Array.isArray(langs) || langs.length < 2) return;

        const labels = {ja: "日本語", en: "English"};
        const display = lang => labels[lang] || lang.toUpperCase();

        const trigger = createElement("span", {
            className: "jig-header-nav__trigger jig-lang-switcher__trigger",
            textContent: display(i18n.currentLanguage())
        });
        const {wrapper, dropdown} = createDropdownNav(trigger, "jig-lang-switcher");

        const renderItems = () => {
            dropdown.replaceChildren();
            const current = i18n.currentLanguage();
            langs.forEach(lang => {
                const isCurrent = (lang === current);
                const child = isCurrent
                    ? createElement("span", {textContent: display(lang)})
                    : createElement("a", {
                        textContent: display(lang),
                        attributes: {href: "#", "data-lang": lang}
                    });
                if (!isCurrent) {
                    child.addEventListener("click", event => {
                        event.preventDefault();
                        i18n.setLanguage(lang);
                    });
                }
                appendDropdownItem(dropdown, child, isCurrent);
            });
        };

        renderItems();

        document.addEventListener("jig:locale-change", event => {
            trigger.textContent = display(event?.detail?.lang || i18n.currentLanguage());
            renderItems();
        });

        header.appendChild(wrapper);
    }

    function setupDocumentHelp() {
        const helpContent = document.getElementById("jig-document-description");
        if (!helpContent || !helpContent.textContent) return;

        const header = document.querySelector("header.top") || document.querySelector("header");
        if (!header) return;

        const helpButton = createElement("button", {
            className: "jig-help-button",
            attributes: {"aria-label": "ドキュメントの説明を表示", "title": "ドキュメントの説明"},
            innerHTML: `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>`
        });

        const helpPanel = createElement("section", {
            id: "jig-document-help-panel",
            children: [
                createElement("div", {
                    className: "help-content",
                    children: [helpContent]
                })
            ]
        });

        helpButton.addEventListener("click", () => {
            helpPanel.classList.toggle("is-active");
        });

        header.appendChild(helpButton);
        header.after(helpPanel);
        helpContent.classList.remove("hidden");
    }

    function initCommonUi() {
        setupHeaderNavigation();
        setupLanguageSwitcher();
        setupDocumentHelp();
        if (globalThis.Jig?.data?.createTypeLinkResolver) {
            setTypeLinkResolver(globalThis.Jig.data.createTypeLinkResolver());
        }
        // 動的挿入された data-i18n 要素は jig-i18n.js の MutationObserver が初期 locale に追従させる

        if (typeof window !== "undefined") {
            // ブラウザの戻る/進みに合わせてサイドバーの該当箇所を表示する
            window.addEventListener("hashchange", () => syncActiveSidebarLink(true));
            // 初回ロード時のサイドバー描画はこのあとの DOMContentLoaded ハンドラで行われるため、
            // 描画完了後に同期する
            if (typeof window.requestAnimationFrame === "function") {
                window.requestAnimationFrame(() => syncActiveSidebarLink(true));
            } else {
                syncActiveSidebarLink(true);
            }
        }
        if (typeof document !== "undefined") {
            // サイドバー内リンクのクリックは、移動先の描画完了（hashchange）を待たずに即座にハイライトする
            initSidebarClickHighlight(document.querySelector(".in-page-sidebar"));
            initSidebarAltKeyIndicator();
        }
    }

    /**
     * サイドバーのパッケージノードからメインのパッケージ見出しへのリンク先を返す。
     * メインに見出しがあるのは「クラスを直接持つ」または「用語(package-info)を持つ」パッケージ
     * （flattenPackageTree に Jig.glossary.hasTerm を渡して生成する見出しと対になる規則）
     */
    function packageHeadingHref(node) {
        return (node.items.length > 0 || globalThis.Jig.glossary.hasTerm(node.fqn))
            ? "#" + globalThis.Jig.util.fqnToId("package", node.fqn)
            : null;
    }

    /**
     * パッケージ見出しセクション。用語のタイトル・FQN・説明（あれば）を表示する。
     * サイドバーのパッケージノードのリンク先になる
     */
    function createPackageHeading(id, packageFqn) {
        const term = globalThis.Jig.glossary.getPackageTerm(packageFqn);
        const section = createElement("section", {
            id,
            className: "package-heading",
            children: [
                createElement("h2", {textContent: term.title}),
                createElement("div", {className: "fully-qualified-name", textContent: packageFqn})
            ]
        });
        if (term.description) {
            section.appendChild(createElement("section", {
                className: "description",
                children: [createMarkdownElement(term.description)]
            }));
        }
        return section;
    }

    return {
        createElement,
        createCell,
        i18nText,
        parseMarkdown,
        sanitizeHtml,
        createMarkdownElement,
        createPackageHeading,
        escapeCsvValue,
        buildCsv,
        downloadCsv,
        renderTableRows,
        setupSortableTables,
        initCommonUi,

        card: {
            type: createTypeCard,
            item: createItemCard,
        },
        kind: {
            badgeChar: kindBadgeChar,
            badgeElement: kindBadgeElement,
        },
        type: {
            setResolver: setTypeLinkResolver,
            clearResolver: clearTypeLinkResolver,
            getResolver: getTypeLinkResolver,
            refElement: createElementForTypeRef,
            parameterElement: createParameterElement,
            methodIOSection: createMethodIOSection,
            fieldItem: createFieldItem,
            fieldsList: createFieldsList,
            methodItem: createMethodItem,
            methodsList: createMethodsList,
        },
        sidebar: {
            section: createSection,
            leaf: createSidebarLeaf,
            renderSection,
            renderTreeSection,
            renderLinkGroup,
            packageHeadingHref,
            initTextFilter: initSidebarTextFilter,
            initCollapseBtn: initSidebarCollapseBtn,
            createToggle: createSidebarToggle,
            syncActiveLink: syncActiveSidebarLink,
            initClickHighlight: initSidebarClickHighlight,
            initAltKeyIndicator: initSidebarAltKeyIndicator,
        },
        tab: {
            buildSection: buildTabSection,
        },
        depthControl: {
            buildOptions: buildDepthOptions,
            renderOptions: renderDepthOptions,
            updateButtonStates: updateDepthButtonStates,
            step: stepDepthByIndex,
        },
    };
})();

if (typeof document !== 'undefined') {
    document.addEventListener("DOMContentLoaded", function () {
        globalThis.Jig.dom.initCommonUi();
    });
}
