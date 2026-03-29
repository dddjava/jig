/* ===== 共通 ===== */

// 共通ユーティリティ名前空間
// ページ固有JSから参照されるため、衝突しない名前空間に集約する
globalThis.Jig ??= {};

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

/* ===== テーブルソート ===== */
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

function setupSortableTables() {
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

function updateRelativeTime() {
    const element = document.getElementById("jig-timestamp");
    if (!element) return;

    const timestampStr = element.getAttribute("data-jig-timestamp");
    if (!timestampStr) return;

    const timestamp = new Date(timestampStr);
    if (isNaN(timestamp.getTime())) return;

    const now = new Date();
    const diffMs = now - timestamp;
    const diffSec = Math.floor(diffMs / 1000);
    const diffMin = Math.floor(diffSec / 60);
    const diffHour = Math.floor(diffMin / 60);
    const diffDay = Math.floor(diffHour / 24);

    let relativeTime = "";
    if (diffDay > 0) {
        relativeTime = `${diffDay}日前`;
    } else if (diffHour > 0) {
        relativeTime = `${diffHour}時間前`;
    } else if (diffMin > 0) {
        relativeTime = `${diffMin}分前`;
    } else {
        relativeTime = "たった今";
    }

    element.textContent = `${element.textContent.split(' (')[0]} (${relativeTime})`;
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

// ページ読み込み時のイベント
// リスナーの登録はそのページだけでやる
document.addEventListener("DOMContentLoaded", function () {
    if (document.body.classList.contains("outbound")) {
        setupSortableTables();
    }
    updateRelativeTime();
    setupHeaderNavigation();
});

/* ===== marked ===== */
Array.from(document.getElementsByClassName("markdown")).forEach(x => x.innerHTML = globalThis.Jig.markdown.parse(x.innerHTML))

/* ===== Mermaid ===== */
const DEFAULT_MAX_TEXT_SIZE = 50000;
const EXTENDED_MAX_TEXT_SIZE = 200000;
const DEFAULT_MAX_EDGES = 500;

// Helper functions that delegate to jig-common.js (loaded before this script)
function isTooLarge(source) {
    return source.length > DEFAULT_MAX_TEXT_SIZE;
}

function estimateEdgeCount(source) {
    const text = source != null ? String(source) : "";
    if (!text) return 0;
    const matches = text.match(/<-->|<-\.-?>|-\.-?>|--?>|==?>|---/g);
    return matches ? matches.length : 0;
}

if (window.mermaid) {
    mermaid.initialize({
        startOnLoad: false,
        securityLevel: "loose",
        maxTextSize: DEFAULT_MAX_TEXT_SIZE,
        maxEdges: DEFAULT_MAX_EDGES
    });
}

function setupLazyMermaidRender() {
    if (!window.mermaid) return;
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

        const renderResult = mermaid.run({nodes: [diagram]});
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

document.addEventListener("DOMContentLoaded", function () {
    setupLazyMermaidRender();
});


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

    if (globalThis.mermaid && typeof globalThis.mermaid.initialize === "function") {
        globalThis.mermaid.initialize({
            startOnLoad: false,
            securityLevel: "loose",
            maxTextSize: EXTENDED_MAX_TEXT_SIZE,
            maxEdges: DEFAULT_MAX_EDGES
        });
    }
    diagram.classList.remove("too-large");
    diagram.innerHTML = source;

    const renderResult = mermaid.run({nodes: [diagram]});
    if (renderResult && typeof renderResult.catch === "function") {
        renderResult.catch(() => {
            flashButtonLabel(button, "描画に失敗しました");
        });
    }
}

/* ===== 共通ユーティリティ (Jig.*) ===== */

globalThis.Jig.dom ??= {};
globalThis.Jig.dom.typeLinkResolver = null;
globalThis.Jig.observe ??= {};
globalThis.Jig.sidebar ??= {};
globalThis.Jig.markdown ??= {};
globalThis.Jig.mermaid ??= {};

globalThis.Jig.dom.createElement = function createElement(tagName, options = {}) {
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
};


/**
 * TypeRefを表現する要素を返す。
 * typeLinkResolver が設定されている場合はリンク付きの要素を返す。
 *
 * @param {TypeRef} typeRef
 * @param {string | undefined} className
 * @returns {HTMLElement}
 */
globalThis.Jig.dom.createElementForTypeRef = function createTypeRefLink(typeRef, className= undefined) {
    if (typeRef.typeArgumentRefs && typeRef.typeArgumentRefs.length) {
        const typeElements= createTypeLink(typeRef.fqn);
        const argumentElements = typeRef.typeArgumentRefs
            .map(typeRef => createTypeRefLink(typeRef))
            // カンマを挟む。HTML Elementが文字列になってしまうのでjoinは使えない。
            .flatMap((v, i) => i ? [', ', v] : [v]);

        return globalThis.Jig.dom.createElement("span", {
            className: className,
            children: [typeElements, '<', ...argumentElements, '>']
        })
    }

    // 型引数なし
    return createTypeLink(typeRef.fqn, className);
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

    const resolved = globalThis.Jig.dom.typeLinkResolver?.(baseFqn);
    const title = (resolved?.text ?? globalThis.Jig.glossary.getTypeTerm(baseFqn).title) + arraySuffix;
    const classes = [className, resolved?.className].filter(Boolean).join(' ') || undefined;
    if (resolved?.href) {
        return globalThis.Jig.dom.createElement('a', {
            className: classes,
            attributes: {href: resolved.href},
            textContent: title
        });
    }
    return globalThis.Jig.dom.createElement('span', {
        className: classes,
        textContent: title
    });
}

globalThis.Jig.dom.downloadCsv = function downloadCsv(text, filename) {
    const blob = new Blob([text], {type: "text/csv;charset=utf-8;"});
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = filename;
    document.body.appendChild(anchor);
    anchor.click();
    anchor.remove();
    URL.revokeObjectURL(url);
};

const KIND_BADGE = { "パッケージ": "P", "クラス": "C", "メソッド": "M", "フィールド": "F" };

globalThis.Jig.dom.kindBadgeElement = function kindBadgeElement(kind) {
    return globalThis.Jig.dom.createElement("span", {
        className: "kind-badge",
        attributes: { "data-kind": kind },
        textContent: KIND_BADGE[kind] ?? (kind ? kind.charAt(0).toUpperCase() : "?"),
    });
};

/**
 * @param {Array} fields
 * @param {Function} [createTypeRefFn]
 * @returns {HTMLElement | null}
 */
globalThis.Jig.dom.createFieldsList = function createFieldsList(fields, createTypeRefFn) {
    const fn = createTypeRefFn || globalThis.Jig.dom.createElementForTypeRef;
    if (fields.length === 0) return null;

    const createElement = globalThis.Jig.dom.createElement;
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
};

/**
 * @param {Object} method
 * @param {Function} [createTypeRefFn]
 * @returns {HTMLElement}
 */
globalThis.Jig.dom.createMethodItem = function createMethodItem(method, createTypeRefFn) {
    const fn = createTypeRefFn || globalThis.Jig.dom.createElementForTypeRef;
    const createElement = globalThis.Jig.dom.createElement;
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
        children.push(createElement("div", {
            className: "markdown",
            innerHTML: globalThis.Jig.markdown.parse(methodTerm.description)
        }));
    }

    return createElement("div", {
        className: "method-item",
        children
    });
};

/**
 * @param {string} kind
 * @param {Array} methods
 * @param {Function} [createTypeRefFn]
 * @returns {HTMLElement | null}
 */
globalThis.Jig.dom.createMethodsList = function createMethodsList(kind, methods, createTypeRefFn) {
    if (methods.length === 0) return null;

    const createElement = globalThis.Jig.dom.createElement;
    return createElement("section", {
        className: "methods-section jig-card jig-card--item",
        children: [
            createElement("h4", {textContent: kind}),
            ...methods.map(method => globalThis.Jig.dom.createMethodItem(method, createTypeRefFn))
        ]
    });
};

globalThis.Jig.observe.lazyRender = function lazyRender(container, renderFn, {rootMargin = "200px"} = {}) {
    if (typeof IntersectionObserver === "undefined") {
        renderFn();
        return;
    }

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                renderFn();
                observer.unobserve(container);
            }
        });
    }, {rootMargin});
    observer.observe(container);
};

globalThis.Jig.sidebar.createSection = function createSidebarSection(title, items) {
    if (!items || items.length === 0) return null;

    const createElement = globalThis.Jig.dom.createElement;
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
};

globalThis.Jig.sidebar.renderSection = function renderSidebarSection(container, title, items) {
    if (!container) return;
    const section = globalThis.Jig.sidebar.createSection(title, items);
    if (section) {
        container.appendChild(section);
    }
};

globalThis.Jig.markdown.parse = function parseMarkdown(markdown) {
    const source = markdown != null ? String(markdown) : "";
    if (globalThis.marked && typeof globalThis.marked.parse === "function") {
        return globalThis.marked.parse(source);
    }
    return source;
};


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

function ensureDownloadButton(container, source) {
    const button = ensureMermaidControlButton(container, "mermaid-download-button", "Download SVG", "⬇");
    if (!button) return null;
    button.onclick = () => downloadMermaidSvg(container, button);
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


globalThis.Jig.mermaid.renderWithControls = function renderWithControls(targetEl, source, {edgeCount} = {}) {
    if (!targetEl) return;
    const text = source != null ? String(source) : "";

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
    ensureCopySourceButton(container, text);
    ensureDownloadButton(container, text);

    if (isTooLarge(text)) {
        diagramEl.style.display = "";
        setEdgeWarning(container, {visible: false});
        renderTooLargeDiagram(diagramEl, text);
        return;
    }

    const resolvedEdgeCount = edgeCount != null ? edgeCount : estimateEdgeCount(text);
    if (resolvedEdgeCount > DEFAULT_MAX_EDGES) {
        diagramEl.style.display = "none";
        setEdgeWarning(container, {
            visible: true,
            message: [
                "関連数が多すぎるため描画を省略しました。",
                `エッジ数: ${resolvedEdgeCount}（上限: ${DEFAULT_MAX_EDGES}）`,
                "描画する場合はボタンを押してください。"
            ].join("\n"),
            onAction: () => renderMermaidNode(diagramEl, text, resolvedEdgeCount, container)
        });
        return;
    }

    renderMermaidNode(diagramEl, text, DEFAULT_MAX_EDGES, container);
};

// 用語集ユーティリティは jig-common.js に移動
globalThis.Jig.glossary ??= {};

// Test-only exports for Node; no-op in browsers.
// Pure functions (isTooLarge, estimateEdgeCount) are local copies
// (fqnToId, glossary, MermaidBuilder) are in jig-common.js but re-exported here for backward compatibility
if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        renderTooLargeDiagram,
        flashButtonLabel,
        isTooLarge,
        estimateEdgeCount,
        fqnToId: globalThis.Jig?.fqnToId,
        getTypeTerm: globalThis.Jig?.glossary?.getTypeTerm,
        getMethodTerm: globalThis.Jig?.glossary?.getMethodTerm,
        kindBadgeElement: globalThis.Jig?.dom?.kindBadgeElement,
    };
}
