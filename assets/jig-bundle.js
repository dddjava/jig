// Source: jig-util.js
globalThis.Jig ??= {};

globalThis.Jig.util = (() => {

    /**
     * FQNのリストから、共通プレフィックスを返す。
     *
     * 先に共通プレフィックスを作ってからgetCommonPrefixDepthはそのdepthを返すほうが自然に思うが、
     * ドット区切りを意識する限りdepthが先に出る。そのためこの関数がDepthに依存する形のほうが実装上は自然。
     *
     * @param {string[]} fqns - ドット区切りのFQNの配列（例: ["com.example.foo.Bar", "com.example.foo.Baz"]）
     * @returns {string} 共通プレフィックス。なければ空文字列。
     *
     * @example
     * getCommonPrefix(["com.example.foo.Bar", "com.example.foo.Baz", "com.example.qux.Quux"]);
     * // => "com.example"
     */
    function getCommonPrefix(fqns) {
        if (!fqns?.length) return '';
        const depth = getCommonPrefixDepth(fqns);
        if (!depth) return '';
        return fqns[0].split('.').slice(0, depth).join('.');
    }

    /**
     * FQNのリストから、共通プレフィックスの深さを返す。
     *
     * @param {string[]} fqns - ドット区切りのFQNの配列（例: ["com.example.foo.Bar", "com.example.foo.Baz"]）
     * @returns {number} 共通プレフィックスのセグメント数。配列が空またはnull/undefinedの場合は0。
     *
     * @example
     * getCommonPrefixDepth(["com.example.foo.Bar", "com.example.foo.Baz", "com.example.qux.Quux"]);
     * // => 2  ("com.example" が共通)
     */
    function getCommonPrefixDepth(fqns) {
        if (!fqns?.length) return 0;
        // 共通の抜き出しなので、最初に "com" とか入ってたら役に立たない系
        const firstParts = fqns[0].split('.');
        let depth = firstParts.length;
        for (let i = 1; i < fqns.length; i += 1) {
            const parts = fqns[i].split('.');
            depth = Math.min(depth, parts.length);
            for (let j = 0; j < depth; j += 1) {
                if (parts[j] !== firstParts[j]) {
                    depth = j;
                    break;
                }
            }
        }
        return depth;
    }

    /**
     * @param {string} fqn
     * @return {number} 深さ
     */
    function getPackageDepth(fqn) {
        if (!fqn || fqn === '(default)') return 0;
        return fqn.split('.').length;
    }

    function getPackageFqnFromTypeFqn(typeFqn) {
        if (!typeFqn || !typeFqn.includes('.')) return '(default)';
        const parts = typeFqn.split('.');
        return parts.slice(0, parts.length - 1).join('.');
    }

    // パッケージフィルタのマッチ判定
    function isWithinPackageFilters(fqn, packageFilterFqn) {
        if (!packageFilterFqn?.length) return true;
        return packageFilterFqn.some(filter => {
            const prefix = `${filter}.`;
            return fqn === filter || fqn.startsWith(prefix);
        });
    }

    // FQN ユーティリティ
    /**
     * FQNを指定された深さのセグメントに切り詰めて返す。
     * depthが0以下、fqnが空や"(default)"、セグメント数がdepth以下の場合はそのまま返す。
     *
     * @param {string} fqn
     * @param {number} depth
     * @returns {string}
     *
     * @example
     * getAggregatedFqn("com.example.foo.Bar", 2); // => "com.example"
     * getAggregatedFqn("com.example.foo.Bar", 9); // => "com.example.foo.Bar"
     */
    function getAggregatedFqn(fqn, depth) {
        if (!depth || depth <= 0) return fqn;
        if (!fqn || fqn === '(default)') return fqn;
        const parts = fqn.split('.');
        if (parts.length <= depth) return fqn;
        return parts.slice(0, depth).join('.');
    }

    /**
     * FQNから一意なHTML IDを生成する
     * HTMLおよびMermaidで使用する
     *
     * @param prefix
     * @param fqn
     * @return {string}
     */
    function fqnToId(prefix, fqn) {
        // マルチバイト文字をハッシュ化して一意なIDを生成
        let hash = 0;
        for (let i = 0; i < fqn.length; i++) {
            const char = fqn.charCodeAt(i);
            hash = ((hash << 5) - hash) + char;
            hash = hash & hash; // Convert to 32bit integer
        }
        const hashStr = Math.abs(hash).toString(36); // 36進数で短くする

        // 英数以外を＿に置換し、_で連結する
        // Mermaidは -x を含む（ hoge-xyz など）とエラーになるため、-ではなく_を使用する
        const reversed = fqn.split('.').reverse().join('_');
        const sanitized = reversed.replace(/[^a-zA-Z0-9]/g, '_').substring(0, 10);
        return `${prefix}_${sanitized}_${hashStr}`;
    }

    return {
        fqnToId,
        getCommonPrefix,
        getCommonPrefixDepth,
        getPackageDepth,
        getPackageFqnFromTypeFqn,
        isWithinPackageFilters,
        getAggregatedFqn,
    }
})();

// 互換用
globalThis.Jig.fqnToId = globalThis.Jig.util.fqnToId;

if (typeof module !== "undefined" && module.exports) {
    module.exports = globalThis.Jig.util;
}

// Source: jig-glossary.js
globalThis.Jig ??= {};

globalThis.Jig.glossary = (() => {

    /**
     * @param {string} fqn
     * @return {Term | undefined}
     */
    function findTerm(fqn) {
        return globalThis.glossaryData?.terms?.[fqn];
    }

    /**
     * @param {string} fqn
     * @return {string}
     */
    function typeSimpleName(fqn) {
        return fqn.substring(fqn.lastIndexOf('.') + 1);
    }

    function getPackageTerm(fqn) {
        const term = findTerm(fqn);
        if (term) return term;
        return {title: typeSimpleName(fqn) || fqn, description: ""};
    }

    function getTypeTerm(fqn) {
        const term = findTerm(fqn);
        if (term) return term;
        return {title: typeSimpleName(fqn) || fqn, description: ""};
    }

    function getFieldTerm(fqn) {
        const term = findTerm(fqn);
        if (term) return term;
        return {title: fqn.substring(fqn.lastIndexOf('#') + 1) || fqn, description: ""};
    }

    /**
     * @param {string} fqn `com.example.Foo#bar(java.lang.String)` のような文字列
     * @param fallbackNameOnly
     * @return {{title: string, simpleText: string, kind: string, description: string, shortDeclaration: string}}
     */
    function getMethodTerm(fqn, fallbackNameOnly = false) {
        if (!fqn) throw Error("method fqn is required: " + fqn);

        const hashIdx = fqn.lastIndexOf('#');
        const parenIdx = fqn.indexOf('(', hashIdx);
        const closeParenIdx = fqn.lastIndexOf(')');
        if (hashIdx < 0 || parenIdx < 0 || closeParenIdx < 0 || hashIdx >= parenIdx || parenIdx >= closeParenIdx)
            throw Error("fqn is not a method?: " + fqn);

        // shortDeclaration構築
        const paramsStr = fqn.substring(parenIdx + 1, closeParenIdx);
        const paramsShortName = paramsStr.split(',').map(arg => typeSimpleName(arg)).join(',');
        const typeShortName = typeSimpleName(fqn.substring(0, hashIdx));
        const methodName = fqn.substring(hashIdx + 1, parenIdx);

        const shortDeclaration = `${typeShortName}#${methodName}(${paramsShortName})`;

        const term = findTerm(fqn);
        if (term) {
            return {...term, shortDeclaration: shortDeclaration};
        }

        // 引数を単純名に変換した FQN で再検索
        // 辞書の引数は実装依存なのでFQNの場合と両方ある。
        // TODO これだと複数引数で入り混じっている場合は対応できない。
        const mayBeFqn = fqn.substring(0, parenIdx + 1) + paramsShortName + ')';
        const term2 = findTerm(mayBeFqn);
        if (term2) {
            return {...term2, shortDeclaration: shortDeclaration};
        }

        // 辞書にない
        // フォールバック: methodName 形式
        if (fallbackNameOnly) {
            return {
                title: methodName,
                simpleText: methodName,
                kind: "メソッド",
                description: "",
                shortDeclaration: shortDeclaration
            };
        }
        // フォールバック: methodName(simpleArgs) 形式
        return {
            title: `${methodName}(${paramsShortName})`,
            simpleText: methodName,
            kind: "メソッド",
            description: "",
            shortDeclaration: shortDeclaration
        };
    }

    return {
        getPackageTerm,
        getTypeTerm,
        getFieldTerm,
        getMethodTerm,
        findTerm,
        typeSimpleName,
    };
})();

if (typeof module !== "undefined" && module.exports) {
    module.exports = globalThis.Jig.glossary;
}

// Source: jig-dom.js
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

        const navigationData = globalThis.navigationData;
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
        },
    };
})();

if (typeof document !== 'undefined') {
    document.addEventListener("DOMContentLoaded", function () {
        globalThis.Jig.dom.initCommonUi();
    });
}

// Source: jig-mermaid.js
globalThis.Jig ??= {};

globalThis.Jig.mermaid = (() => {
    const builder = (() => {
        const nodeStyleDefs = {
            inbound: "fill:#E8F0FE,stroke:#2E5C8A",
            usecase: "fill:#E6F8F0,stroke:#2D7A4A",
            outbound: "fill:#FFF0E6,stroke:#CC6600",
            inactive: "fill:#e0e0e0,stroke:#aaa",
            domain: "fill:#FEF9E7,stroke:#B7950B"
        };

        const nodeShapes = {
            method: '(["$LABEL"])',
            class: '["$LABEL"]',
            package: '@{shape: st-rect, label: "$LABEL"}',
            database: '[("$LABEL")]',
            external: '(("$LABEL"))'
        };

        function escapeId(id) {
            return (id || "").replace(/\./g, '_');
        }

        function escapeLabel(label) {
            return `"${(label || "").replace(/"/g, '#quot;')}"`;
        }

        function escapeMermaidText(text) {
            return (text || "").replace(/"/g, '\\"');
        }

        function getNodeDefinition(id, label, shapeKey = 'class') {
            const shape = nodeShapes[shapeKey] || shapeKey;
            const escapedLabel = escapeMermaidText(label);
            return `${id}${shape.replace('$LABEL', escapedLabel)}`;
        }

        function edgeTypeForLength(dotted = false, length = 1) {
            if (dotted) return "-.->";
            const safeLength = Math.max(1, Number(length) || 1);
            return "--" + "-".repeat(safeLength - 1) + ">";
        }

// Mermaid diagram builder
        class MermaidBuilder {
            constructor() {
                this.nodes = [];
                this.edges = [];
                this.subgraphs = [];
                this.styles = [];
                this.clicks = [];
                this.edgeSet = new Set();
            }

            sanitize(id) {
                return (id || "").replace(/[^a-zA-Z0-9]/g, '_');
            }

            addNode(id, label, shape = 'class') {
                const nodeLine = getNodeDefinition(id, label, shape);
                if (!this.nodes.includes(nodeLine)) {
                    this.nodes.push(nodeLine);
                }
                return id;
            }

            addEdge(from, to, label = "", dotted = false, length = 1) {
                const edgeType = edgeTypeForLength(dotted, length);
                const edgeKey = `${from}--${label}--${edgeType}-->${to}`;
                if (!this.edgeSet.has(edgeKey)) {
                    this.edgeSet.add(edgeKey);
                    const edgeLine = label ? `  ${from} -- "${label}" ${edgeType} ${to}` : `  ${from} ${edgeType} ${to}`;
                    this.edges.push(edgeLine);
                }
            }

            addStyle(id, style) {
                if (!id || !style) return;
                this.styles.push(`style ${id} ${style}`);
            }

            addClick(id, url) {
                if (!id || !url) return;
                this.clicks.push(`click ${id} "${url}"`);
            }

            addClass(id, className) {
                if (!id || !className) return;
                this.styles.push(`class ${id} ${className}`);
            }

            addClassDef(className, style) {
                if (!className || !style) return;
                this.styles.push(`classDef ${className} ${style}`);
            }

            applyThemeClassDefs() {
                Object.entries(nodeStyleDefs).forEach(([name, style]) => {
                    this.addClassDef(name, style);
                });
            }

            startSubgraph(id, label = id, direction = null) {
                const subgraph = {id, label, lines: []};
                if (direction) {
                    const safeDirection = (direction === 'TD') ? 'TB' : direction;
                    subgraph.lines.push(`direction ${safeDirection}`);
                }
                this.subgraphs.push(subgraph);
                return subgraph;
            }

            ensureSubgraph(map, key, label, direction = null) {
                if (!map.has(key)) {
                    map.set(key, this.startSubgraph(key, label, direction));
                }
                return map.get(key);
            }

            addNodeToSubgraph(subgraph, id, label, shape = 'class') {
                const nodeLine = `    ${getNodeDefinition(id, label, shape)}`;
                if (!subgraph.lines.includes(nodeLine)) {
                    subgraph.lines.push(nodeLine);
                }
                return id;
            }

            build(direction = "LR") {
                let code = `graph ${direction}\n`;
                this.subgraphs.forEach(sg => {
                    code += `  subgraph ${sg.id} ["${sg.label}"]\n`;
                    sg.lines.forEach(line => {
                        code += `    ${line.trim()}\n`;
                    });
                    code += `  end\n`;
                });
                this.nodes.forEach(node => {
                    code += `  ${node.trim()}\n`;
                });
                this.edges.forEach(edge => {
                    code += `${edge}\n`;
                });
                this.styles.forEach(styleLine => {
                    code += `${styleLine}\n`;
                });
                this.clicks.forEach(clickLine => {
                    code += `${clickLine}\n`;
                });
                return code;
            }

            isEmpty() {
                return this.nodes.length === 0 && this.edges.length === 0 && this.subgraphs.length === 0;
            }
        }

        /**
         * 親パッケージセットから、実際に関連を持つ親パッケージのみを抽出
         *
         * @param {Set<string>} parentFqns - 親パッケージFQNのセット
         * @param {Relation[]} uniqueRelations - 関連の配列
         * @returns {Set<string>} 関連を持つ親パッケージのセット
         */
        function filterParentFqnsWithRelations(parentFqns, uniqueRelations) {
            const parentFqnsWithRelations = new Set();

            parentFqns.forEach(parentFqn => {
                // 親パッケージが from または to として現れる関連を検索
                for (const relation of uniqueRelations) {
                    if (relation.from === parentFqn || relation.to === parentFqn) {
                        parentFqnsWithRelations.add(parentFqn);
                        break;
                    }
                }
            });
            return parentFqnsWithRelations;
        }

        /**
         * @param {Set<string>} packageFqns
         * @param {Relation[]} uniqueRelations
         * @param {MermaidDiagramSourceOptions} options
         */
        function buildMermaidDiagramSource(packageFqns, uniqueRelations, options) {
            const {diagramDirection, focusedPackageFqn, clickHandlerName, nodeClickUrlCallback} = options;
            const escapeMermaidText = text => text.replace(/"/g, '\\"');

            // 親パッケージセットを構築し、関連を持つ親パッケージのみを抽出
            const allParentFqns = buildParentFqns(packageFqns);
            const parentFqnsWithRelations = filterParentFqnsWithRelations(allParentFqns, uniqueRelations);

            // 関連のない親パッケージを packageFqns から除外
            const packageFqnsToDisplay = new Set(Array.from(packageFqns).filter(fqn => {
                // 親パッケージの場合、関連を持つものだけを含める
                if (allParentFqns.has(fqn)) {
                    return parentFqnsWithRelations.has(fqn);
                }
                // 親パッケージでない場合は常に含める
                return true;
            }));

            const lines = [
                "---",
                "config:",
                "  theme: 'default'",
                "  themeVariables:",
                "    clusterBkg: '#ffffde'", // デフォルトと同じ色だがルートノードの色と合わせるために明示
                "---",
                `graph ${diagramDirection}`];
            const {nodeIdByFqn, nodeIdToFqn, nodeLabelById, ensureNodeId} = buildDiagramNodeMaps(packageFqnsToDisplay);
            const subgraphNodeIds = new Map();

            const nodeLines = buildDiagramNodeLines(
                packageFqnsToDisplay,
                nodeIdByFqn,
                {
                    nodeIdToFqn,
                    nodeLabelById,
                    escapeMermaidText,
                    clickHandlerName,
                    nodeClickUrlCallback,
                    parentFqnsWithRelations,
                    subgraphNodeIds
                }
            );
            const {
                edgeLines,
                linkStyles,
                mutualPairs
            } = buildDiagramEdgeLines(uniqueRelations, ensureNodeId, {subgraphNodeIds});

            nodeLines.forEach(line => lines.push(line));
            edgeLines.forEach(line => lines.push(line));
            linkStyles.forEach(styleLine => lines.push(styleLine));

            // ノードのスタイルを指定。どちらも存在しない場合もあるが、classDefに害はないので出力する。
            // ルートパッケージの色はサブグラフに合わせて少し濃くし、境界線を破線にする
            lines.push('classDef parentPackage fill:#ffffce,stroke:#aaaa00,stroke-dasharray:10 3');
            if (focusedPackageFqn && nodeIdByFqn.has(focusedPackageFqn)) {
                // 選択されたものがあれば強調表示する
                lines.push(`style ${nodeIdByFqn.get(focusedPackageFqn)} fill:#ffffce,stroke:#aaaa00,stroke-width:3px,font-weight:bold`);
            }

            return {source: lines.join('\n'), nodeIdToFqn, mutualPairs};
        }

        /**
         * ダイアグラムで使用する各種Mapを構築する
         * @param {Set<string>} packageFqns - 対象パッケージFQNセット
         * @returns {{nodeIdByFqn: Map<string, string>, nodeIdToFqn: Map<string, string>, nodeLabelById: Map<string, string>, ensureNodeId: function(string): string}} - ノードマップとノードID生成関数
         */
        function buildDiagramNodeMaps(packageFqns) {
            const nodeIdByFqn = new Map();
            const nodeIdToFqn = new Map();
            const nodeLabelById = new Map();
            let nodeIndex = 0;
            const ensureNodeId = fqn => {
                if (nodeIdByFqn.has(fqn)) return nodeIdByFqn.get(fqn);
                const nodeId = `P${nodeIndex++}`;
                nodeIdByFqn.set(fqn, nodeId);
                nodeIdToFqn.set(nodeId, fqn);
                const label = globalThis.Jig.glossary.getPackageTerm(fqn).title;
                nodeLabelById.set(nodeId, label);
                return nodeId;
            };
            Array.from(packageFqns).sort().forEach(ensureNodeId);
            return {nodeIdByFqn, nodeIdToFqn, nodeLabelById, ensureNodeId};
        }

        function buildDiagramEdgeLines(uniqueRelations, ensureNodeId, options = {}) {
            const subgraphNodeIds = options.subgraphNodeIds;
            const mutualPairs = buildMutualDependencyPairs(uniqueRelations);
            const linkStyles = [];
            let linkIndex = 0;
            const edgeDefs = [];
            uniqueRelations.forEach(relation => {
                const fromId = ensureNodeId(relation.from);
                const toId = ensureNodeId(relation.to);
                const pairKey = relation.from < relation.to
                    ? `${relation.from}::${relation.to}`
                    : `${relation.to}::${relation.from}`;
                if (mutualPairs.has(pairKey)) {
                    if (relation.from > relation.to) {
                        return;
                    }
                    edgeDefs.push({fromId, toId, isMutual: true});
                    linkStyles.push(`linkStyle ${linkIndex} stroke:red,stroke-width:2px`);
                    linkIndex += 1;
                    return;
                }
                edgeDefs.push({fromId, toId, isMutual: false, key: `${fromId}::${toId}`});
                linkIndex += 1;
            });
            const edgeLengthByKey = new Map();
            if (subgraphNodeIds && subgraphNodeIds.size > 0) {
                const singleEdges = edgeDefs
                    .filter(edge => !edge.isMutual)
                    .map(edge => ({from: edge.fromId, to: edge.toId}));
                subgraphNodeIds.forEach(nodesInSubgraph => {
                    const {edgeLengthByKey: lengths} = graph.computeOutboundEdgeLengths({
                        nodesInSubgraph,
                        edges: singleEdges
                    });
                    lengths.forEach((length, key) => {
                        const current = edgeLengthByKey.get(key) || 1;
                        if (length > current) edgeLengthByKey.set(key, length);
                    });
                });
            }
            const edgeLines = edgeDefs.map(edge => {
                if (edge.isMutual) return `${edge.fromId} <--> ${edge.toId}`;
                const length = edgeLengthByKey.get(edge.key) || 1;
                const edgeType = edgeTypeForLength(false, length);
                return `${edge.fromId} ${edgeType} ${edge.toId}`;
            });
            return {edgeLines, linkStyles, mutualPairs};
        }

        /**
         * @param {Set<string>} packageFqns
         * @param {Map<string, string>} nodeIdByFqn
         * @param {DiagramNodeLinesOptions} options
         */
        function buildDiagramNodeLines(packageFqns, nodeIdByFqn, options) {
            const {nodeIdToFqn, nodeLabelById, escapeMermaidText, clickHandlerName, nodeClickUrlCallback} = options;

            const packageFqnList = Array.from(packageFqns).sort();
            const parentFqns = buildParentFqns(packageFqns);
            const rootGroup = buildDiagramGroupTree(packageFqnList, nodeIdByFqn);
            const addNodeLines = (lines, nodeId, parentSubgraphFqn) => {
                const fqn = nodeIdToFqn.get(nodeId);
                const displayLabel = buildDiagramNodeLabel(nodeLabelById.get(nodeId), fqn, parentSubgraphFqn);
                const nodeDefinition = getNodeDefinition(nodeId, displayLabel, 'package');
                lines.push(nodeDefinition);
                if (clickHandlerName) {
                    const tooltip = escapeMermaidText(buildDiagramNodeTooltip(fqn));
                    lines.push(`click ${nodeId} ${clickHandlerName} "${tooltip}"`);
                }
                if (nodeClickUrlCallback && fqn) {
                    const url = escapeMermaidText(nodeClickUrlCallback(fqn));
                    lines.push(`click ${nodeId} href "${url}"`);
                }
                if (fqn && parentFqns.has(fqn)) {
                    lines.push(`class ${nodeId} parentPackage`);
                }
            };
            return buildSubgraphLines(rootGroup, addNodeLines, escapeMermaidText, options.subgraphNodeIds);
        }

        function buildDiagramNodeLabel(displayLabel, fqn, parentSubgraphFqn) {
            if (!fqn) return displayLabel ?? '';
            if (displayLabel === fqn && parentSubgraphFqn && fqn.startsWith(`${parentSubgraphFqn}.`)) {
                return fqn.substring(parentSubgraphFqn.length + 1);
            }
            return displayLabel ?? '';
        }

        function buildDiagramSubgraphLabel(subgraphFqn, parentSubgraphFqn) {
            if (!subgraphFqn) return '';
            if (parentSubgraphFqn && subgraphFqn.startsWith(`${parentSubgraphFqn}.`)) {
                return subgraphFqn.substring(parentSubgraphFqn.length + 1);
            }
            return subgraphFqn;
        }

        function buildDiagramNodeTooltip(fqn) {
            return fqn ?? '';
        }

        function buildDiagramGroupTree(packageFqnList, nodeIdByFqn) {
            const prefixDepth = Jig.util.getCommonPrefixDepth(packageFqnList);
            const baseDepth = Math.max(prefixDepth - 1, 0);
            const createGroupNode = key => ({key, children: new Map(), nodes: []});
            const rootGroup = createGroupNode('');
            packageFqnList.forEach(fqn => {
                const parts = fqn.split('.');
                const maxDepth = parts.length;
                let current = rootGroup;
                for (let depth = baseDepth + 1; depth <= maxDepth; depth += 1) {
                    const key = parts.slice(0, depth).join('.');
                    if (!current.children.has(key)) {
                        current.children.set(key, createGroupNode(key));
                    }
                    current = current.children.get(key);
                }
                current.nodes.push(nodeIdByFqn.get(fqn));
            });
            return rootGroup;
        }

        function buildSubgraphLines(rootGroup, addNodeLines, escapeMermaidText, subgraphNodeIds = null) {
            const lines = [];
            let groupIndex = 0;
            const collectNodeIds = group => {
                const ids = [...group.nodes];
                group.children.forEach(child => {
                    ids.push(...collectNodeIds(child));
                });
                return ids;
            };
            const renderGroup = (group, isRoot, parentSubgraphFqnForNodes) => {
                group.nodes.forEach(nodeId => addNodeLines(lines, nodeId, parentSubgraphFqnForNodes));
                const childKeys = Array.from(group.children.keys()).sort();
                if (isRoot && group.nodes.length === 0 && childKeys.length === 1) {
                    renderGroup(group.children.get(childKeys[0]), false, parentSubgraphFqnForNodes);
                    return;
                }
                childKeys.forEach(key => {
                    const child = group.children.get(key);
                    const childNodeCount = child.nodes.length + child.children.size;
                    if (childNodeCount <= 1) {
                        renderGroup(child, false, parentSubgraphFqnForNodes);
                        return;
                    }
                    const groupId = `G${groupIndex++}`;
                    const label = buildDiagramSubgraphLabel(child.key, parentSubgraphFqnForNodes);
                    if (subgraphNodeIds) {
                        subgraphNodeIds.set(groupId, new Set(collectNodeIds(child)));
                    }
                    lines.push(`subgraph ${groupId}["${escapeMermaidText(label)}"]`);
                    renderGroup(child, false, child.key);
                    lines.push('end');
                });
            };
            renderGroup(rootGroup, true, rootGroup.key);
            return lines;
        }


        /**
         * 関連を深さで切り詰めてユニークにする
         *
         * @param {Relation[]} relations
         * @param {number} aggregationDepth 切り詰める深さ
         * @return {Relation[]}
         */
        function aggregationRelations(relations, aggregationDepth) {
            const uniqueRelationMap = new Map();
            relations
                .map(relation => ({
                    from: Jig.util.getAggregatedFqn(relation.from, aggregationDepth),
                    to: Jig.util.getAggregatedFqn(relation.to, aggregationDepth),
                }))
                .filter(relation => relation.from !== relation.to)
                .forEach(relation => {
                    uniqueRelationMap.set(`${relation.from}::${relation.to}`, relation);
                });
            return Array.from(uniqueRelationMap.values());
        }

        /**
         * パッケージフィルタを適用して表示対象の関連とパッケージセットを構築
         * 表示対象のパッケージ・関係・因果関係エビデンスを絞り込んで返す。
         *
         * @param {Package[]} packages - 全パッケージの一覧
         * @param {Relation[]} relations - 全関係の一覧 ({from, to})
         * @param {Relation[]} causeRelationEvidence - 全因果関係エビデンスの一覧 ({from, to})
         * @param {{packageFilterFqn: string[], aggregationDepth: number, transitiveReductionEnabled: boolean}} options
         *      packageFilterFqn - 表示対象に絞り込むパッケージFQNのリスト（空の場合は全件）
         *      aggregationDepth - FQNを集約するセグメント深さ
         *      transitiveReductionEnabled - 推移的縮約を行うかどうか
         * @returns {{ uniqueRelations: Relation[], packageFqns: Set<string>, filteredCauseRelationEvidence: Relation[] }}
         */
        function buildVisibleDiagramRelations(packages, relations, causeRelationEvidence, options) {
            const {packageFilterFqn, aggregationDepth, transitiveReductionEnabled} = options;

            const visiblePackages = packageFilterFqn.length > 0
                ? packages.filter(item => Jig.util.isWithinPackageFilters(item.fqn, packageFilterFqn))
                : packages;
            const packageFqns = new Set(visiblePackages.map(item => Jig.util.getAggregatedFqn(item.fqn, aggregationDepth)));
            const filteredRelations = packageFilterFqn.length > 0
                ? relations.filter(relation => Jig.util.isWithinPackageFilters(relation.from, packageFilterFqn) && Jig.util.isWithinPackageFilters(relation.to, packageFilterFqn))
                : relations;
            const filteredCauseRelationEvidence = packageFilterFqn.length > 0
                ? causeRelationEvidence.filter(relation => {
                    const fromPackage = Jig.util.getPackageFqnFromTypeFqn(relation.from);
                    const toPackage = Jig.util.getPackageFqnFromTypeFqn(relation.to);
                    return Jig.util.isWithinPackageFilters(fromPackage, packageFilterFqn) && Jig.util.isWithinPackageFilters(toPackage, packageFilterFqn);
                })
                : causeRelationEvidence;

            let uniqueRelations = aggregationRelations(filteredRelations, aggregationDepth);
            if (transitiveReductionEnabled) {
                uniqueRelations = graph.transitiveReduction(uniqueRelations);
            }

            return {uniqueRelations, packageFqns, filteredCauseRelationEvidence};
        }

        // Mermaid 図生成
        function buildMutualDependencyPairs(relations) {
            const relationKey = (from, to) => `${from}::${to}`;
            const canonicalPairKey = (from, to) => (from < to ? `${from}::${to}` : `${to}::${from}`);
            const relationSet = new Set(relations.map(relation => relationKey(relation.from, relation.to)));
            const mutualPairs = new Set();
            relations.forEach(relation => {
                if (relationSet.has(relationKey(relation.to, relation.from))) {
                    mutualPairs.add(canonicalPairKey(relation.from, relation.to));
                }
            });
            return mutualPairs;
        }

        function buildParentFqns(packageFqns) {
            const parentFqns = new Set();
            Array.from(packageFqns).sort().forEach(fqn => {
                const parts = fqn.split('.');
                for (let i = 1; i < parts.length; i += 1) {
                    const prefix = parts.slice(0, i).join('.');
                    if (packageFqns.has(prefix)) parentFqns.add(prefix);
                }
            });
            return parentFqns;
        }

        return {
            MermaidBuilder,
            nodeStyleDefs,
            nodeShapes,
            escapeId,
            escapeLabel,
            escapeMermaidText,
            getNodeDefinition,
            edgeTypeForLength,
            buildMermaidDiagramSource,
            buildDiagramNodeMaps,
            buildDiagramEdgeLines,
            buildDiagramNodeLines,
            buildDiagramNodeLabel,
            buildDiagramSubgraphLabel,
            buildDiagramNodeTooltip,
            buildDiagramGroupTree,
            buildSubgraphLines,
            buildVisibleDiagramRelations,
            buildMutualDependencyPairs,
            buildParentFqns,
        };
    })();

    // グラフ関連のユーティリティ
    const graph = (() => {
        /**
         * 強連結成分(SCC)を抽出する (Tarjan's algorithm)
         * @param {Map<string, string[]>} graph
         * @returns {string[][]}
         */
        function detectStronglyConnectedComponents(graph) {
            const indices = new Map();
            const lowLink = new Map();
            const stack = [];
            const onStack = new Set();
            const result = [];
            const index = {value: 0};

            function strongConnect(node) {
                indices.set(node, index.value);
                lowLink.set(node, index.value);
                index.value++;
                stack.push(node);
                onStack.add(node);

                (graph.get(node) || []).forEach(neighbor => {
                    if (!indices.has(neighbor)) {
                        strongConnect(neighbor);
                        lowLink.set(node, Math.min(lowLink.get(node), lowLink.get(neighbor)));
                    } else if (onStack.has(neighbor)) {
                        lowLink.set(node, Math.min(lowLink.get(node), indices.get(neighbor)));
                    }
                });

                if (lowLink.get(node) === indices.get(node)) {
                    const scc = [];
                    let current;
                    do {
                        current = stack.pop();
                        onStack.delete(current);
                        scc.push(current);
                    } while (current !== node);
                    result.push(scc);
                }
            }

            for (const node of graph.keys()) {
                if (!indices.has(node)) {
                    strongConnect(node);
                }
            }
            return result;
        }

        /**
         * 推移的簡約(Transitive Reduction)を行う。
         * 直接の依存関係がある場合、他の経路でも到達可能ならその直接の依存を削除する。
         * ただし、サイクル（強連結成分内）の関連は削除しない。
         * @param {{from: string, to: string}[]} relations
         * @returns {{from: string, to: string}[]}
         */
        function transitiveReduction(relations) {
            const graph = new Map();
            relations.forEach(relation => {
                if (!graph.has(relation.from)) graph.set(relation.from, []);
                graph.get(relation.from).push(relation.to);
            });

            const sccs = detectStronglyConnectedComponents(graph);
            const cyclicNodes = new Set(sccs.filter(scc => scc.length > 1).flat());
            const cyclicEdges = new Set(
                relations
                    .filter(edge => cyclicNodes.has(edge.from) && cyclicNodes.has(edge.to))
                    .map(edge => `${edge.from}::${edge.to}`)
            );

            const acyclicGraph = new Map();
            relations.forEach(edge => {
                if (cyclicEdges.has(`${edge.from}::${edge.to}`)) return;
                if (!acyclicGraph.has(edge.from)) acyclicGraph.set(edge.from, []);
                acyclicGraph.get(edge.from).push(edge.to);
            });

            function isReachableWithoutDirect(start, end) {
                const visited = new Set();

                function dfs(current, target, skipDirect) {
                    if (current === target) return true;
                    visited.add(current);
                    const neighbors = acyclicGraph.get(current) || [];
                    for (const neighbor of neighbors) {
                        if (skipDirect && neighbor === target) continue;
                        if (visited.has(neighbor)) continue;
                        if (dfs(neighbor, target, false)) return true;
                    }
                    return false;
                }

                return dfs(start, end, true);
            }

            const toRemove = new Set();
            relations.forEach(edge => {
                if (cyclicEdges.has(`${edge.from}::${edge.to}`)) return;
                if (isReachableWithoutDirect(edge.from, edge.to)) {
                    toRemove.add(`${edge.from}::${edge.to}`);
                }
            });

            return relations.filter(edge => !toRemove.has(`${edge.from}::${edge.to}`));
        }

        /**
         * subgraph 内部のエッジのみを使ってノード深さを計算する。
         * 深さの起点は内部入次数0ノード（なければ全ノード）を 1 とする。
         * @param {{nodesInSubgraph: Iterable<string>, edges: {from: string, to: string}[]}} params
         * @returns {{depthMap: Map<string, number>, maxDepth: number}}
         */
        function computeSubgraphDepthMap(params) {
            const nodes = new Set(params?.nodesInSubgraph || []);
            const edges = Array.isArray(params?.edges) ? params.edges : [];
            const depthMap = new Map();
            if (nodes.size === 0) return {depthMap, maxDepth: 1};

            const inDegree = new Map();
            nodes.forEach(node => inDegree.set(node, 0));

            const internalEdges = [];
            edges.forEach(edge => {
                if (!edge) return;
                if (!nodes.has(edge.from) || !nodes.has(edge.to)) return;
                internalEdges.push(edge);
                inDegree.set(edge.to, (inDegree.get(edge.to) || 0) + 1);
            });

            const roots = [];
            nodes.forEach(node => {
                if ((inDegree.get(node) || 0) === 0) roots.push(node);
            });

            if (roots.length === 0) {
                nodes.forEach(node => depthMap.set(node, 1));
            } else {
                roots.forEach(node => depthMap.set(node, 1));
            }

            let changed = true;
            let iteration = 0;
            const maxIterations = Math.max(internalEdges.length * Math.max(nodes.size, 1), nodes.size);
            while (changed && iteration < maxIterations) {
                changed = false;
                iteration += 1;
                internalEdges.forEach(edge => {
                    const fromDepth = depthMap.get(edge.from) || 0;
                    const toDepth = depthMap.get(edge.to) || 0;
                    if (fromDepth > 0 && fromDepth + 1 > toDepth) {
                        depthMap.set(edge.to, fromDepth + 1);
                        changed = true;
                    }
                });
            }

            nodes.forEach(node => {
                if (!depthMap.has(node)) depthMap.set(node, 1);
            });
            const maxDepth = depthMap.size > 0 ? Math.max(...depthMap.values()) : 1;
            return {depthMap, maxDepth};
        }

        /**
         * subgraph 内部ノードから外部ノードへのエッジ長を計算する。
         * @param {{nodesInSubgraph: Iterable<string>, edges: {from: string, to: string}[], minLength?: number}} params
         * @returns {{edgeLengthByKey: Map<string, number>, depthMap: Map<string, number>, maxDepth: number}}
         */
        function computeOutboundEdgeLengths(params) {
            const nodes = new Set(params?.nodesInSubgraph || []);
            const edges = Array.isArray(params?.edges) ? params.edges : [];
            const minLength = Math.max(1, Number(params?.minLength) || 1);
            const {depthMap, maxDepth} = computeSubgraphDepthMap({
                nodesInSubgraph: nodes,
                edges: edges
            });
            const edgeLengthByKey = new Map();

            edges.forEach(edge => {
                if (!edge) return;
                const key = `${edge.from}::${edge.to}`;
                let length = minLength;
                if (nodes.has(edge.from) && !nodes.has(edge.to)) {
                    const fromDepth = depthMap.get(edge.from) || 1;
                    length = Math.max(minLength, maxDepth - fromDepth + 1);
                }
                edgeLengthByKey.set(key, length);
            });
            return {edgeLengthByKey, depthMap, maxDepth};
        }

        return {
            computeOutboundEdgeLengths,
            computeSubgraphDepthMap,
            transitiveReduction,
            detectStronglyConnectedComponents,
        }
    })();

    const render = (() => {
        const DEFAULT_MAX_TEXT_SIZE = 50000;
        const EXTENDED_MAX_TEXT_SIZE = 200000;
        const DEFAULT_MAX_EDGES = 500;

        function isTooLarge(source) {
            const text = source != null ? String(source) : "";
            return text.length > DEFAULT_MAX_TEXT_SIZE;
        }

        function estimateEdgeCount(source) {
            const text = source != null ? String(source) : "";
            if (!text) return 0;
            const matches = text.match(/<-->|<-\.-?>|-\.-?>|--?>|==?>|---/g);
            return matches ? matches.length : 0;
        }

        function fallbackCopyText(source, button) {
            const textarea = document.createElement("textarea");
            textarea.value = source;
            textarea.style.position = "fixed";
            textarea.style.top = "-1000px";
            textarea.style.left = "-1000px";
            document.body.appendChild(textarea);
            textarea.focus();
            textarea.select();
            try {
                document.execCommand("copy");
                flashButtonLabel(button, "Copied!!");
            } catch (e) {
                flashButtonLabel(button, "Copy failed...");
                console.error("Failed to copy text:", e);
            } finally {
                document.body.removeChild(textarea);
            }
        }

        function copyMermaidText(source, button) {
            if (!source) return;
            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(source).then(() => {
                    flashButtonLabel(button, "Copied!");
                }).catch(() => {
                    fallbackCopyText(source, button);
                });
                return;
            }
            fallbackCopyText(source, button);
        }

        function flashButtonLabel(button, text) {
            if (!button) return;
            if (button.dataset && button.dataset.iconButton === "true") {
                const originalTitle = button.getAttribute("title") || "";
                const originalTooltip = button.dataset.tooltip || "";
                button.setAttribute("title", text);
                button.dataset.tooltip = text;
                window.setTimeout(() => {
                    button.setAttribute("title", originalTitle);
                    button.dataset.tooltip = originalTooltip;
                }, 1500);
                return;
            }
            const original = button.textContent;
            button.textContent = text;
            window.setTimeout(() => {
                button.textContent = original;
            }, 1500);
        }

        function renderWithExtendedLimit(diagram, source, button) {
            if (!diagram || !source) return;
            if (source.length > EXTENDED_MAX_TEXT_SIZE) {
                flashButtonLabel(button, "さらに大きいため描画できません");
                return;
            }

            globalThis.mermaid.initialize({
                startOnLoad: false,
                securityLevel: "loose",
                maxTextSize: EXTENDED_MAX_TEXT_SIZE, // 初期のinitializeとの差分。initializeでやるの？
                maxEdges: DEFAULT_MAX_EDGES
            });

            diagram.classList.remove("too-large");
            diagram.innerHTML = source;

            const renderResult = globalThis.mermaid.run({nodes: [diagram]});
            if (renderResult && typeof renderResult.catch === "function") {
                renderResult.catch(() => {
                    flashButtonLabel(button, "描画に失敗しました");
                });
            }
        }

        function renderTooLargeDiagram(diagram, source) {
            if (!diagram) return;
            diagram.classList.add("too-large");
            diagram.textContent = "";

            const container = document.createElement("div");
            container.className = "mermaid-too-large";

            const message = document.createElement("p");
            message.className = "mermaid-too-large__message";
            message.textContent = "図の内容が大きすぎるため描画を省略しました。";
            container.appendChild(message);

            const actions = document.createElement("div");
            actions.className = "mermaid-too-large__actions";

            const renderButton = document.createElement("button");
            renderButton.type = "button";
            renderButton.textContent = "上限を上げて描画する";
            renderButton.addEventListener("click", () => {
                renderWithExtendedLimit(diagram, source, renderButton);
            });
            actions.appendChild(renderButton);

            const copyButton = document.createElement("button");
            copyButton.type = "button";
            copyButton.textContent = "図の内容をコピー";
            copyButton.addEventListener("click", () => {
                copyMermaidText(source, copyButton);
            });
            actions.appendChild(copyButton);

            container.appendChild(actions);
            diagram.appendChild(container);
        }

        function ensureMermaidDiagramContainer(targetEl) {
            if (!targetEl) return null;
            if (targetEl.classList && targetEl.classList.contains("mermaid-diagram")) return targetEl;

            const existing = targetEl.closest ? targetEl.closest(".mermaid-diagram") : null;
            if (existing) return existing;

            const container = document.createElement("div");
            container.className = "mermaid-diagram";

            const parent = targetEl.parentNode;
            if (!parent) return null;
            parent.insertBefore(container, targetEl);
            container.appendChild(targetEl);
            return container;
        }

        function ensureMermaidControlButton(container, className, label, icon) {
            if (!container) return null;
            let button = container.querySelector(`:scope > .${className}`);
            if (!button) {
                button = document.createElement("button");
                button.type = "button";
                button.className = className;
                container.insertBefore(button, container.firstChild);
            }
            button.textContent = icon != null ? String(icon) : label;
            button.setAttribute("aria-label", label);
            button.setAttribute("title", label);
            button.dataset.tooltip = label;
            button.dataset.iconButton = icon != null ? "true" : "false";
            return button;
        }

        function ensureCopySourceButton(container, source) {
            const button = ensureMermaidControlButton(container, "mermaid-copy-button", "Copy Source", "⧉");
            if (!button) return null;
            button.onclick = () => {
                const text = source != null ? String(source) : "";
                if (!text) return;
                copyMermaidText(text, button);
            };
            return button;
        }

        function findRenderedMermaidSvg(container) {
            if (!container) return null;
            return container.querySelector(":scope > .mermaid svg");
        }

        function downloadMermaidSvg(container, button) {
            const svg = findRenderedMermaidSvg(container);
            if (!svg) {
                flashButtonLabel(button, "SVG未生成");
                return;
            }

            const serializer = new XMLSerializer();
            const svgText = serializer.serializeToString(svg);
            const blob = new Blob([svgText], {type: "image/svg+xml;charset=utf-8"});
            const url = URL.createObjectURL(blob);
            const link = document.createElement("a");
            link.href = url;
            const htmlFile = (window.location.pathname.split("/").pop() || "diagram.html");
            const baseName = htmlFile.replace(/\.html?$/i, "");
            const safeName = baseName.toLowerCase().replace(/[^a-z0-9_-]+/g, "-").replace(/^-+|-+$/g, "");
            link.download = `jig-${safeName || "diagram"}.svg`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            URL.revokeObjectURL(url);
            flashButtonLabel(button, "Downloaded");
        }

        function ensureDownloadButton(container) {
            const button = ensureMermaidControlButton(container, "mermaid-download-button", "Download SVG", "⬇");
            if (!button) return null;
            button.onclick = () => downloadMermaidSvg(container, button);
            return button;
        }

        function ensureDirectionButton(container, currentDirection, onUpdate) {
            if (!container || !currentDirection) return null;
            const button = ensureMermaidControlButton(container, "mermaid-direction-button", "Switch Direction", "⇄");
            if (!button) return null;
            button.onclick = () => {
                const newDirection = (currentDirection === "LR") ? "TB" : "LR";
                onUpdate(newDirection);
            };
            return button;
        }

        function ensureEdgeWarningPanel(container) {
            if (!container) return null;
            let panel = container.querySelector(":scope > .mermaid-edge-warning");
            if (!panel) {
                panel = document.createElement("div");
                panel.className = "mermaid-edge-warning";
                panel.setAttribute("role", "alert");
                panel.style.display = "none";

                const message = document.createElement("pre");
                message.className = "mermaid-edge-warning__message";
                message.style.whiteSpace = "pre-wrap";
                message.style.margin = "0 0 8px 0";

                const action = document.createElement("button");
                action.type = "button";
                action.className = "mermaid-edge-warning__action";
                action.textContent = "描画する";
                action.style.display = "none";

                panel.appendChild(message);
                panel.appendChild(action);
                container.insertBefore(panel, container.firstChild);
            }
            return panel;
        }

        function setEdgeWarning(container, {visible, message, onAction} = {}) {
            const panel = ensureEdgeWarningPanel(container);
            if (!panel) return;
            const messageEl = panel.querySelector(".mermaid-edge-warning__message");
            const actionEl = panel.querySelector(".mermaid-edge-warning__action");
            if (messageEl) messageEl.textContent = message || "";

            const hasAction = typeof onAction === "function";
            if (actionEl) {
                actionEl.style.display = hasAction ? "" : "none";
                actionEl.onclick = hasAction ? onAction : null;
            }
            panel.style.display = visible ? "" : "none";
        }

        function baseMermaidConfig(maxEdges) {
            return {
                startOnLoad: false,
                securityLevel: "loose",
                maxTextSize: DEFAULT_MAX_TEXT_SIZE,
                maxEdges: maxEdges != null ? maxEdges : DEFAULT_MAX_EDGES
            };
        }

        function renderMermaidNode(diagramEl, source, maxEdges, container) {
            if (!diagramEl || !globalThis.mermaid || typeof globalThis.mermaid.run !== "function") return;

            const text = source != null ? String(source) : "";
            diagramEl.removeAttribute("data-processed");
            diagramEl.style.display = "";
            setEdgeWarning(container, {visible: false});

            if (isTooLarge(text)) {
                renderTooLargeDiagram(diagramEl, text);
                return;
            }

            diagramEl.textContent = text;
            if (typeof globalThis.mermaid.initialize === "function") {
                globalThis.mermaid.initialize(baseMermaidConfig(maxEdges));
            }

            try {
                const result = globalThis.mermaid.run({nodes: [diagramEl]});
                if (result && typeof result.catch === "function") {
                    result.catch((err) => {
                        const message = err && err.message ? err.message : String(err);
                        if (message.includes("Edge limit exceeded")) {
                            const edgeCount = estimateEdgeCount(text);
                            const actionEdges = Math.max(edgeCount, DEFAULT_MAX_EDGES * 2);
                            diagramEl.style.display = "none";
                            setEdgeWarning(container, {
                                visible: true,
                                message: [
                                    "関連数が多すぎるため描画を省略しました。",
                                    `エッジ数: ${edgeCount}（上限: ${DEFAULT_MAX_EDGES}）`,
                                    "描画する場合はボタンを押してください。"
                                ].join("\n"),
                                onAction: () => renderMermaidNode(diagramEl, text, actionEdges, container)
                            });
                        } else {
                            diagramEl.style.display = "none";
                            setEdgeWarning(container, {visible: true, message: `Mermaid error: ${message}`});
                        }
                    });
                }
            } catch (err) {
                const message = err && err.message ? err.message : String(err);
                diagramEl.style.display = "none";
                setEdgeWarning(container, {visible: true, message: `Mermaid error: ${message}`});
            }
        }

        function renderWithControls(targetEl, diagramFn, {direction} = {}) {
            if (!targetEl) return;

            let diagramEl = null;
            if (targetEl.classList && targetEl.classList.contains("mermaid")) {
                diagramEl = targetEl;
            } else {
                if (targetEl.classList && !targetEl.classList.contains("mermaid-diagram")) {
                    targetEl.classList.add("mermaid-diagram");
                }
                diagramEl = targetEl.querySelector(":scope > .mermaid");
                if (!diagramEl) {
                    diagramEl = document.createElement("pre");
                    diagramEl.className = "mermaid";
                    targetEl.appendChild(diagramEl);
                }
            }

            const container = ensureMermaidDiagramContainer(diagramEl) || targetEl;

            const renderDiagram = (newDirection) => {
                const currentSource = diagramFn(newDirection) ?? "";

                ensureCopySourceButton(container, currentSource);
                ensureDownloadButton(container);
                if (/^\s*(?:graph|flowchart)\s/m.test(currentSource)) {
                    ensureDirectionButton(container, newDirection, renderDiagram);
                }

                if (isTooLarge(currentSource)) {
                    diagramEl.style.display = "";
                    setEdgeWarning(container, {visible: false});
                    renderTooLargeDiagram(diagramEl, currentSource);
                    return;
                }

                const edgeCount = estimateEdgeCount(currentSource);
                if (edgeCount > DEFAULT_MAX_EDGES) {
                    diagramEl.style.display = "none";
                    setEdgeWarning(container, {
                        visible: true,
                        message: [
                            "関連数が多すぎるため描画を省略しました。",
                            `エッジ数: ${edgeCount}（上限: ${DEFAULT_MAX_EDGES}）`,
                            "描画する場合はボタンを押してください。"
                        ].join("\n"),
                        onAction: () => renderMermaidNode(diagramEl, currentSource, edgeCount, container)
                    });
                    return;
                }

                renderMermaidNode(diagramEl, currentSource, DEFAULT_MAX_EDGES, container);
            };

            let initialDirection = direction;
            if (!initialDirection) {
                const text = diagramFn("LR");
                const match = text?.match(/^(\s*(?:graph|flowchart)\s+)(TB|TD|LR)\b/m);
                initialDirection = match ? match[2] : "LR";
            }

            renderDiagram(initialDirection);
        }

        /**
         * IntersectionObserverを使用して遅延レンダリングを行う
         */
        function setupLazyMermaidRender() {
            if (typeof window === "undefined" || !window.mermaid) return;
            if (document.body.classList.contains("package-summary")) return;

            const diagrams = Array.from(document.querySelectorAll(".mermaid"));
            if (diagrams.length === 0) return;

            const sourceMap = new WeakMap();
            const rendered = new WeakSet();
            const queued = new WeakSet();
            const renderQueue = [];
            let isRendering = false;

            const processRenderQueue = () => {
                if (isRendering) return;
                const diagram = renderQueue.shift();
                if (!diagram) return;
                isRendering = true;

                if (rendered.has(diagram)) {
                    isRendering = false;
                    processRenderQueue();
                    return;
                }

                const source = sourceMap.get(diagram) || diagram.textContent;
                if (!source) {
                    isRendering = false;
                    processRenderQueue();
                    return;
                }

                sourceMap.set(diagram, source);
                if (isTooLarge(source)) {
                    renderTooLargeDiagram(diagram, source);
                    rendered.add(diagram);
                    queued.delete(diagram);
                    isRendering = false;
                    processRenderQueue();
                    return;
                }

                diagram.innerHTML = source;
                const renderResult = globalThis.mermaid.run({nodes: [diagram]});
                const handleFinish = () => {
                    rendered.add(diagram);
                    queued.delete(diagram);
                    isRendering = false;
                    processRenderQueue();
                };
                if (renderResult && typeof renderResult.then === "function") {
                    renderResult.then(handleFinish).catch(handleFinish);
                } else {
                    handleFinish();
                }
            };

            const enqueueRender = (diagram) => {
                if (!diagram) return;
                if (diagram.getAttribute("data-processed") === "true") {
                    rendered.add(diagram);
                    return;
                }
                if (rendered.has(diagram)) return;
                if (queued.has(diagram)) return;
                const source = sourceMap.get(diagram) || diagram.textContent;
                if (!source) return;
                if (isTooLarge(source)) {
                    renderTooLargeDiagram(diagram, source);
                    rendered.add(diagram);
                    return;
                }
                sourceMap.set(diagram, source);
                queued.add(diagram);
                renderQueue.push(diagram);
                processRenderQueue();
            };

            if (!("IntersectionObserver" in window)) {
                diagrams.forEach(enqueueRender);
                return;
            }

            const observer = new IntersectionObserver((entries, currentObserver) => {
                entries.forEach(entry => {
                    if (!entry.isIntersecting) return;
                    enqueueRender(entry.target);
                    currentObserver.unobserve(entry.target);
                });
            }, {rootMargin: "200px 0px"});

            diagrams.forEach(diagram => observer.observe(diagram));
        }

        function initializeMermaid() {
            if (typeof window !== "undefined" && window === globalThis && globalThis.mermaid) {
                globalThis.mermaid.initialize({
                    startOnLoad: false,
                    securityLevel: "loose",
                    maxTextSize: DEFAULT_MAX_TEXT_SIZE,
                    maxEdges: DEFAULT_MAX_EDGES
                });
            }
        }

        return {
            isTooLarge,
            estimateEdgeCount,
            flashButtonLabel,
            renderTooLargeDiagram,
            renderWithControls,
            setupLazyMermaidRender,
            initializeMermaid
        }
    })();

    /**
     * ダイアグラム管理（設定変更時の再レンダリング対応）
     */
    const diagram = (() => {
        const diagramRegistry = []; // [{container, renderFn}]
        const renderedContainers = new Set();
        const observerMap = new Map(); // コンテナごとに独立した observer

        function isVisible(element) {
            if (typeof element.getBoundingClientRect !== 'function') {
                return true; // テスト環境など、getBoundingClientRect が使えない場合は表示中と判断
            }
            const rect = element.getBoundingClientRect();
            return rect.top < window.innerHeight && rect.bottom > 0;
        }

        /**
         * ダイアグラムを登録（遅延レンダリング対応）
         * @param {HTMLElement} container
         * @param {Function} renderFn - レンダリング関数
         */
        function register(container, renderFn) {
            if (!container || typeof renderFn !== 'function') return;

            diagramRegistry.push({container, renderFn});

            // IntersectionObserver で自動レンダリング（各コンテナごとに独立した observer）
            if ('IntersectionObserver' in window) {
                const observer = new IntersectionObserver((entries) => {
                    entries.forEach(entry => {
                        if (entry.isIntersecting && !renderedContainers.has(entry.target)) {
                            renderedContainers.add(entry.target);
                            const d = diagramRegistry.find(d => d.container === entry.target);
                            if (d) {
                                d.renderFn();
                            }
                            observer.unobserve(entry.target); // 一度だけレンダリング
                        }
                    });
                }, {rootMargin: '100px'});

                observer.observe(container);
                observerMap.set(container, observer);
            } else {
                // IntersectionObserver 非サポート時は即座にレンダリング
                renderedContainers.add(container);
                renderFn();
            }
        }

        /**
         * 表示範囲内のダイアグラムのみ再レンダリング
         * @param {Function} [shouldRerender] - 再レンダリング判定関数（省略時は全て）
         */
        function rerenderVisible(shouldRerender) {
            diagramRegistry
                .filter(({container}) => renderedContainers.has(container))
                .forEach(({container, renderFn}) => {
                    // 表示範囲内のみ再レンダリング
                    if (isVisible(container)) {
                        if (!shouldRerender || shouldRerender(container)) {
                            renderFn();
                        }
                    } else {
                        // 表示範囲外は削除のみで、スクロール時に自動再レンダリング
                        container.innerHTML = "";
                        renderedContainers.delete(container);
                    }
                });
        }

        /**
         * ダイアグラムコンテナを作成して登録するヘルパー関数
         * コンテナの作成・追加・登録をまとめて処理
         *
         * renderFn は以下の2つのパターンをサポート：
         * 1. container パラメータを受け取り、自分で renderWithControls を呼ぶ
         * 2. mermaid定義またはコードを返す - この関数が renderWithControls を呼ぶ
         *
         * @param {HTMLElement|Array} parentContainer - 親要素（HTMLElement）またはコンテナ追加先（Array）
         * @param {Function} renderFn - (container?: HTMLElement) => mermaidDef|undefined
         * @param {Object} [options={}] - オプション
         * @param {string} [options.className="mermaid-diagram"] - コンテナのクラス名
         * @returns {HTMLElement} 作成されたコンテナ
         */
        function createAndRegister(parentContainer, renderFn, options = {}) {
            const {className = "mermaid-diagram"} = options;
            const container = Jig.dom.createElement("div", {className});

            // parentContainer が配列の場合は push、そうでなければ appendChild
            if (Array.isArray(parentContainer)) {
                parentContainer.push(container);
            } else {
                parentContainer.appendChild(container);
            }

            register(container, () => {
                const result = renderFn(container);
                // renderFn が mermaid定義を返した場合、自動でレンダリング
                if (result) {
                    container.innerHTML = "";
                    const diagramFn = typeof result === "function" ? result : () => result;
                    Jig.mermaid.render.renderWithControls(container, diagramFn);
                }
                // renderFn が void/undefined を返した場合は、renderFn 内で既に renderWithControls を呼んでいると仮定
            });

            return container;
        }

        return {
            register,
            rerenderVisible,
            createAndRegister
        };
    })();

    /**
     * パッケージダイアグラム作成
     * index.htmlおよびdomain.htmlで表示するもの。
     *
     * @param {Package} pkg - 対象パッケージ
     * @param {Package[]} allPackages - 全パッケージの一覧
     * @param {Relation[]} allPackageRelations - パッケージ間の全関連
     * @param {CreatePackageLevelDiagramOptions} options
     * @returns {string|null}
     */
    function createPackageLevelDiagram(pkg, allPackages, allPackageRelations, options) {
        const {transitiveReductionEnabled, diagramDirection, nodeClickUrlCallback, focusedPackageFqn} = options;
        const {uniqueRelations, packageFqns} = builder.buildVisibleDiagramRelations(
            allPackages,
            allPackageRelations,
            [],
            {
                packageFilterFqn: [pkg.fqn],
                aggregationDepth: pkg.fqn.split('.').length + 1, // 自身の一つ下でグルーピング
                transitiveReductionEnabled: transitiveReductionEnabled
            }
        );
        // パッケージ数が1つだったり関連が0なら表示しない
        if (packageFqns.size <= 1 || uniqueRelations.length === 0) return null;

        const {source} = builder.buildMermaidDiagramSource(
            packageFqns, uniqueRelations,
            {diagramDirection, nodeClickUrlCallback, focusedPackageFqn}
        );
        return source;
    }

    return {
        builder,
        graph,
        render,
        diagram,
        // 高レベルAPI
        createPackageLevelDiagram,
        Builder: builder.MermaidBuilder,
    };
})();

if (typeof document !== "undefined") {

    document.addEventListener("DOMContentLoaded", function () {
        globalThis.Jig.mermaid.render.initializeMermaid();
        globalThis.Jig.mermaid.render.setupLazyMermaidRender();
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = globalThis.Jig.mermaid;
}

