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
    let typeLinkResolver = null;

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

    function parseMarkdown(markdown) {
        const source = markdown != null ? String(markdown) : "";
        if (globalThis.marked && typeof globalThis.marked.parse === "function") {
            return globalThis.marked.parse(source);
        }
        return source;
    }

    function createMarkdownElement(markdown) {
        return createElement("div", {
            className: "markdown",
            innerHTML: parseMarkdown(markdown)
        });
    }

    function normalizeNavigationHref(href) {
        return String(href || "").replace(/^\.\//, "");
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

        const container = document.createElement("div");
        container.className = "jig-header-nav";

        const trigger = document.createElement("span");
        trigger.className = "jig-header-nav__trigger";
        trigger.textContent = pageTitleEl.textContent;

        const dropdown = document.createElement("ul");
        dropdown.className = "jig-header-nav__dropdown";
        dropdown.setAttribute("role", "list");

        navigationData.links.forEach(link => {
            if (!link) return;
            const href = normalizeNavigationHref(link.href);
            const label = link.label != null ? String(link.label) : href;
            if (!href) return;

            const isCurrent = (href === normalizedCurrent);
            const li = document.createElement("li");
            li.className = "jig-header-nav__item" + (isCurrent ? " jig-header-nav__item--current" : "");

            if (isCurrent) {
                const span = document.createElement("span");
                span.textContent = label;
                li.appendChild(span);
            } else {
                const a = document.createElement("a");
                a.href = href;
                a.textContent = label;
                li.appendChild(a);
            }
            dropdown.appendChild(li);
        });

        container.appendChild(trigger);
        container.appendChild(dropdown);
        pageTitleEl.replaceWith(container);
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
        setupDocumentHelp();
        if (globalThis.Jig?.data?.createTypeLinkResolver) {
            setTypeLinkResolver(globalThis.Jig.data.createTypeLinkResolver());
        }
    }

    /**
     * テキストとクラス名を指定してtd要素を作成する
     * @param {string} text
     * @param {string} [className]
     * @returns {HTMLElement}
     */
    function createCell(text, className) {
        return createElement("td", {
            className: className || undefined,
            textContent: text
        });
    }

    /**
     * @param {string} fqn
     * @param {string | undefined} className
     * @returns {HTMLElement}
     */
    function createTypeLink(fqn, className = undefined) {
        // 配列型（例: Hoge[], Hoge[][]）はベース型で解決し、[] を付け直す
        const arraySuffix = fqn.match(/(\[\])+$/)?.[0] ?? '';
        const baseFqn = arraySuffix ? fqn.slice(0, -arraySuffix.length) : fqn;

        const resolved = typeLinkResolver?.(baseFqn);
        const title = (resolved?.text ?? globalThis.Jig.glossary.getTypeTerm(baseFqn).title) + arraySuffix;
        const classes = [className, resolved?.className].filter(Boolean).join(' ') || undefined;
        if (resolved?.href) {
            return createElement('a', {
                className: classes,
                attributes: {href: resolved.href},
                textContent: title
            });
        }
        return createElement('span', {
            className: classes,
            textContent: title
        });
    }

    /**
     * TypeRefを表現する要素を返す。
     * typeLinkResolver が設定されている場合はリンク付きの要素を返す。
     *
     * @param {TypeRef} typeRef
     * @param {string | undefined} className
     * @returns {HTMLElement}
     */
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

        // 型引数なし
        return createTypeLink(typeRef.fqn, className);
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

    /**
     * @param {Array} fields
     * @param {Function} [createTypeRefFn]
     * @returns {HTMLElement | null}
     */
    function createFieldsList(fields, createTypeRefFn) {
        const fn = createTypeRefFn || createElementForTypeRef;
        if (fields.length === 0) return null;

        const items = fields.map(field => createElement("div", {
            className: "method-item",
            children: [
                createElement("div", {
                    className: "method-signature",
                    children: [
                        createElement("span", {
                            className: "method-name" + (field.isDeprecated ? " deprecated" : ""),
                            textContent: field.name
                        }),
                        createElement("span", {className: "method-return-sep", textContent: ":"}),
                        fn(field.typeRef)
                    ]
                })
            ]
        }));

        return createElement("section", {
            className: "methods-section jig-card jig-card--item fields",
            children: [
                createElement("h4", {textContent: "フィールド"}),
                ...items
            ]
        });
    }

    /**
     * @param {Object} method
     * @param {Function} [createTypeRefFn]
     * @returns {HTMLElement}
     */
    function createMethodItem(method, createTypeRefFn) {
        const fn = createTypeRefFn || createElementForTypeRef;
        const methodTerm = globalThis.Jig.glossary.getMethodTerm(method.fqn, true);

        const paramElements = method.parameterTypeRefs
            .map(param => fn(param))
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
                fn(method.returnTypeRef)
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

    /**
     * @param {string} kind
     * @param {Array} methods
     * @param {Function} [createTypeRefFn]
     * @returns {HTMLElement | null}
     */
    function createMethodsList(kind, methods, createTypeRefFn) {
        if (methods.length === 0) return null;

        return createElement("section", {
            className: "methods-section jig-card jig-card--item",
            children: [
                createElement("h4", {textContent: kind}),
                ...methods.map(method => createMethodItem(method, createTypeRefFn))
            ]
        });
    }

    function setTypeLinkResolver(resolver) {
        typeLinkResolver = (typeof resolver === "function") ? resolver : null;
    }

    function clearTypeLinkResolver() {
        typeLinkResolver = null;
    }

    function getTypeLinkResolver() {
        return typeLinkResolver;
    }

    function setupSortableTables() {
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

    function createSection(title, items) {
        if (!items || items.length === 0) return null;

        return createElement("section", {
            className: "in-page-sidebar__section",
            children: [
                createElement("p", {
                    className: "in-page-sidebar__title",
                    textContent: title
                }),
                createElement("ul", {
                    className: "in-page-sidebar__links",
                    children: items.map(({id, label}) => createElement("li", {
                        className: "in-page-sidebar__item",
                        children: [
                            createElement("a", {
                                className: "in-page-sidebar__link",
                                attributes: {href: "#" + id},
                                textContent: label
                            })
                        ]
                    }))
                })
            ]
        });
    }

    function renderSection(container, title, items) {
        if (!container) return;
        const section = createSection(title, items);
        if (section) {
            container.appendChild(section);
        }
    }

    /**
     * サイドバーのテキストフィルタ入力を初期化する
     * @param {string} inputId - input要素のID
     * @param {(filterText: string) => void} onChange
     */
    function initSidebarTextFilter(inputId, onChange) {
        const input = document.getElementById(inputId);
        if (!input) return;
        input.addEventListener('input', () => onChange(input.value.trim()));
    }

    return {
        createElement,
        parseMarkdown,
        createMarkdownElement,
        initCommonUi,
        createCell,
        downloadCsv,
        setupSortableTables,

        type: {
            setResolver: setTypeLinkResolver,
            clearResolver: clearTypeLinkResolver,
            getResolver: getTypeLinkResolver,
            elementForRef: createElementForTypeRef,
            fieldsList: createFieldsList,
            methodItem: createMethodItem,
            methodsList: createMethodsList,
        },
        kind: {
            badgeChar: kindBadgeChar,
            badgeElement: kindBadgeElement,
        },
        sidebar: {
            createSection,
            renderSection,
            initTextFilter: initSidebarTextFilter,
        },
    };
})();

if (typeof document !== 'undefined') {
    document.addEventListener("DOMContentLoaded", function () {
        globalThis.Jig.dom.initCommonUi();
    });
}
