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
        return createElement("td", {
            className: className || undefined,
            textContent: text
        });
    }

    // --- Markdown ---

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

    // --- CSV utility ---

    function escapeCsvValue(value) {
        const text = String(value ?? "")
            .replace(/\r\n/g, "\n")
            .replace(/\r/g, "\n");
        return `"${text.replace(/"/g, "\"\"")}"`;
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

    function createParameterElement(param, createTypeRefFn) {
        const fn = createTypeRefFn || createElementForTypeRef;
        return createElement("span", {
            children: param.nameSource === 'METHOD_PARAMETERS'
                ? [param.name + ': ', fn(param.typeRef)]
                : [fn(param.typeRef)]
        });
    }

    function createMethodItem(method, createTypeRefFn) {
        const fn = createTypeRefFn || createElementForTypeRef;
        const methodTerm = globalThis.Jig.glossary.getMethodTerm(method.fqn, true);

        const paramElements = method.parameters
            .map(param => createParameterElement(param, fn))
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

        const card = createItemCard({title: "フィールド", extraClass: "methods-section fields"});
        items.forEach(item => card.appendChild(item));
        return card;
    }

    function createMethodsList(kind, methods, createTypeRefFn) {
        if (methods.length === 0) return null;

        const card = createItemCard({title: kind, extraClass: "methods-section"});
        methods.forEach(method => card.appendChild(createMethodItem(method, createTypeRefFn)));
        return card;
    }

    // --- Card builders ---

    function createItemCard({id, title, tagName = "section", extraClass} = {}) {
        return createElement(tagName, {
            id,
            className: ["jig-card", "jig-card--item", extraClass].filter(Boolean).join(" "),
            children: title !== undefined ? [createElement("h4", {textContent: title})] : []
        });
    }

    function createTypeCard({id, title, fqn, kind, attributes, tagName = "section", extraClass} = {}) {
        const titleEl = typeof title === 'string' ? createElement("span", {textContent: title}) : title;
        const titleContent = id
            ? createElement("a", {className: "card-title-anchor", attributes: {href: `#${id}`}, children: [titleEl]})
            : titleEl;
        const h3Children = kind !== undefined ? [kindBadgeElement(kind), titleContent] : [titleContent];

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

    function createSidebarToggle(targetEl) {
        const toggle = createElement("button", {
            className: "in-page-sidebar__toggle",
            attributes: {"aria-expanded": "true", "aria-label": "折りたたむ"}
        });
        toggle.addEventListener("click", () => {
            const collapsing = toggle.getAttribute("aria-expanded") === "true";
            toggle.setAttribute("aria-expanded", String(!collapsing));
            toggle.setAttribute("aria-label", collapsing ? "展開" : "折りたたむ");
            targetEl.classList.toggle("in-page-sidebar__links--hidden", collapsing);
        });
        return toggle;
    }

    function buildCollapsibleTitle(title, links) {
        return createElement("p", {
            className: "in-page-sidebar__title in-page-sidebar__title--collapsible",
            children: [createElement("span", {textContent: title}), createSidebarToggle(links)]
        });
    }

    function createSection(title, items, {collapsible = false} = {}) {
        if (!items || items.length === 0) return null;

        const links = createElement("ul", {
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
        });

        const titleEl = !title ? null
            : collapsible ? buildCollapsibleTitle(title, links)
            : createElement("p", {className: "in-page-sidebar__title", textContent: title});

        return createElement("section", {
            className: "in-page-sidebar__section",
            children: [titleEl, links]
        });
    }

    function renderSection(container, title, items, options) {
        if (!container) return;
        const section = createSection(title, items, options);
        if (section) {
            container.appendChild(section);
        }
    }

    function initSidebarTextFilter(inputId, onChange) {
        const input = document.getElementById(inputId);
        if (!input) return;
        input.addEventListener('input', () => onChange(input.value.trim()));
    }

    // --- Table ---

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

            rows.forEach(row => table.getElementsByTagName("tbody")[0].appendChild(row));

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

    // --- Common UI setup ---

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

        const trigger = createElement("span", {
            className: "jig-header-nav__trigger",
            textContent: pageTitleEl.textContent
        });

        const dropdown = createElement("ul", {
            className: "jig-header-nav__dropdown",
            attributes: {role: "list"}
        });

        navigationData.links.forEach(link => {
            if (!link) return;
            const href = normalizeNavigationHref(link.href);
            const label = link.label != null ? String(link.label) : href;
            if (!href) return;

            const isCurrent = (href === normalizedCurrent);
            const child = isCurrent
                ? createElement("span", {textContent: label})
                : createElement("a", {textContent: label, attributes: {href}});
            dropdown.appendChild(createElement("li", {
                className: "jig-header-nav__item" + (isCurrent ? " jig-header-nav__item--current" : ""),
                children: [child]
            }));
        });

        pageTitleEl.replaceWith(createElement("div", {
            className: "jig-header-nav",
            children: [trigger, dropdown]
        }));
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

    return {
        createElement,
        createCell,
        parseMarkdown,
        createMarkdownElement,
        escapeCsvValue,
        downloadCsv,
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
            fieldsList: createFieldsList,
            methodItem: createMethodItem,
            methodsList: createMethodsList,
        },
        sidebar: {
            section: createSection,
            renderSection,
            initTextFilter: initSidebarTextFilter,
            createToggle: createSidebarToggle,
        },
    };
})();

if (typeof document !== 'undefined') {
    document.addEventListener("DOMContentLoaded", function () {
        globalThis.Jig.dom.initCommonUi();
    });
}
