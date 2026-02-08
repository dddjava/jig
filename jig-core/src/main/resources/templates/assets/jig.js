Array.from(document.getElementsByClassName("markdown")).forEach(x => x.innerHTML = marked.parse(x.innerHTML))

if (window.mermaid) {
    mermaid.initialize({startOnLoad: false, securityLevel: "loose"});
}

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

// ページ読み込み時のイベント
// リスナーの登録はそのページだけでやる
document.addEventListener("DOMContentLoaded", function () {
    if (document.body.classList.contains("repository")) {
        setupSortableTables();
    }
});

function setupLazyMermaidRender() {
    if (!window.mermaid) return;
    if (document.body.classList.contains("package-list")) return;

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
