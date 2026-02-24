/* ===== 共通 ===== */

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

// ページ読み込み時のイベント
// リスナーの登録はそのページだけでやる
document.addEventListener("DOMContentLoaded", function () {
    if (document.body.classList.contains("outputs")) {
        setupSortableTables();
    }
    updateRelativeTime();
});

/* ===== marked ===== */
Array.from(document.getElementsByClassName("markdown")).forEach(x => x.innerHTML = marked.parse(x.innerHTML))

/* ===== Mermaid ===== */
const DEFAULT_MAX_TEXT_SIZE = 50000;
const EXTENDED_MAX_TEXT_SIZE = 200000;

if (window.mermaid) {
    mermaid.initialize({
        startOnLoad: false,
        securityLevel: "loose",
        maxTextSize: DEFAULT_MAX_TEXT_SIZE
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

function isTooLarge(source) {
    return source.length > DEFAULT_MAX_TEXT_SIZE;
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

function copyMermaidText(source, button) {
    if (!source) return;
    if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(source).then(() => {
            flashButtonLabel(button, "コピーしました");
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
        flashButtonLabel(button, "コピーしました");
    } catch (e) {
        flashButtonLabel(button, "コピーに失敗しました");
    } finally {
        document.body.removeChild(textarea);
    }
}

function flashButtonLabel(button, text) {
    if (!button) return;
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

    mermaid.initialize({
        startOnLoad: false,
        securityLevel: "loose",
        maxTextSize: EXTENDED_MAX_TEXT_SIZE
    });
    diagram.classList.remove("too-large");
    diagram.innerHTML = source;

    const renderResult = mermaid.run({nodes: [diagram]});
    if (renderResult && typeof renderResult.catch === "function") {
        renderResult.catch(() => {
            flashButtonLabel(button, "描画に失敗しました");
        });
    }
}

// Test-only exports for Node; no-op in browsers.
if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        isTooLarge,
        renderTooLargeDiagram,
        flashButtonLabel,
    };
}
