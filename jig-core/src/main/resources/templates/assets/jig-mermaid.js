// Mermaidダイアグラムのソース組み立てモジュール
// MermaidBuilderおよびすべてのMermaidダイアグラム記述をカプセル化する
// レンダリング制御もこのファイルで扱う

const MermaidDiagramModule = (() => {
// Mermaid theme colors
const nodeStyleDefs = {
    inbound:  "fill:#E8F0FE,stroke:#2E5C8A",
    usecase:  "fill:#E6F8F0,stroke:#2D7A4A",
    outbound: "fill:#FFF0E6,stroke:#CC6600",
    inactive: "fill:#e0e0e0,stroke:#aaa",
    domain:   "fill:#FEF9E7,stroke:#B7950B"
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
        if (direction) subgraph.lines.push(`direction ${direction}`);
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

// グラフ関連のユーティリティ
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

if (typeof window !== "undefined" && window.mermaid) {
    globalThis.mermaid.initialize({
        startOnLoad: false,
        securityLevel: "loose",
        maxTextSize: DEFAULT_MAX_TEXT_SIZE,
        maxEdges: DEFAULT_MAX_EDGES
    });
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
        const newDirection = (currentDirection === "LR") ? "TD" : "LR";
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

function renderWithControls(targetEl, source, {edgeCount, direction} = {}) {
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

    const render = (newDirection) => {
        const text = (typeof source === "function") ? source(newDirection) : source;
        const currentSource = text != null ? String(text) : "";

        ensureCopySourceButton(container, currentSource);
        ensureDownloadButton(container);
        if (typeof source === "function") {
            ensureDirectionButton(container, newDirection, render);
        }

        if (isTooLarge(currentSource)) {
            diagramEl.style.display = "";
            setEdgeWarning(container, {visible: false});
            renderTooLargeDiagram(diagramEl, currentSource);
            return;
        }

        const resolvedEdgeCount = edgeCount != null ? edgeCount : estimateEdgeCount(currentSource);
        if (resolvedEdgeCount > DEFAULT_MAX_EDGES) {
            diagramEl.style.display = "none";
            setEdgeWarning(container, {
                visible: true,
                message: [
                    "関連数が多すぎるため描画を省略しました。",
                    `エッジ数: ${resolvedEdgeCount}（上限: ${DEFAULT_MAX_EDGES}）`,
                    "描画する場合はボタンを押してください。"
                ].join("\n"),
                onAction: () => renderMermaidNode(diagramEl, currentSource, resolvedEdgeCount, container)
            });
            return;
        }

        renderMermaidNode(diagramEl, currentSource, DEFAULT_MAX_EDGES, container);
    };

    let initialDirection = direction;
    if (!initialDirection) {
        const text = (typeof source === "function") ? source("LR") : String(source);
        const match = text.match(/^(\s*(?:graph|flowchart)\s+)(TB|TD|LR)\b/m);
        initialDirection = match ? match[2] : "LR";
    }

    render(initialDirection);
}

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

    return {
        mermaid: {
            nodeStyleDefs,
            nodeShapes,
            escapeId,
            escapeLabel,
            escapeMermaidText,
            getNodeDefinition,
            edgeTypeForLength,
            Builder: MermaidBuilder,
            isTooLarge,
            estimateEdgeCount,
            flashButtonLabel,
            renderTooLargeDiagram,
            renderWithControls,
            setupLazyMermaidRender
        },
        graph: {
            detectStronglyConnectedComponents,
            transitiveReduction,
            computeSubgraphDepthMap,
            computeOutboundEdgeLengths
        }
    };
})();

globalThis.Jig ??= {};
Object.assign(globalThis.Jig.mermaid ??= {}, MermaidDiagramModule.mermaid);
Object.assign(globalThis.Jig.graph ??= {}, MermaidDiagramModule.graph);

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", function () {
        MermaidDiagramModule.mermaid.setupLazyMermaidRender();
    });
}

// パッケージ関連図生成の共通モジュール
// package.js と domain.js の両方で使用する純粋関数を提供する
// IIFE でスコープを閉じ込めて、グローバルスコープ汚染を防ぐ

const PackageDiagramModule = (() => {
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
                from: getAggregatedFqn(relation.from, aggregationDepth),
                to: getAggregatedFqn(relation.to, aggregationDepth),
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
            ? packages.filter(item => isWithinPackageFilters(item.fqn, packageFilterFqn))
            : packages;
        const packageFqns = new Set(visiblePackages.map(item => getAggregatedFqn(item.fqn, aggregationDepth)));
        const filteredRelations = packageFilterFqn.length > 0
            ? relations.filter(relation => isWithinPackageFilters(relation.from, packageFilterFqn) && isWithinPackageFilters(relation.to, packageFilterFqn))
            : relations;
        const filteredCauseRelationEvidence = packageFilterFqn.length > 0
            ? causeRelationEvidence.filter(relation => {
                const fromPackage = getPackageFqnFromTypeFqn(relation.from);
                const toPackage = getPackageFqnFromTypeFqn(relation.to);
                return isWithinPackageFilters(fromPackage, packageFilterFqn) && isWithinPackageFilters(toPackage, packageFilterFqn);
            })
            : causeRelationEvidence;

        let uniqueRelations = aggregationRelations(filteredRelations, aggregationDepth);
        if (transitiveReductionEnabled) {
            uniqueRelations = globalThis.Jig.graph.transitiveReduction(uniqueRelations);
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
     * @param {Package} pkg - 対象パッケージ
     * @param {Package[]} allPackages - 全パッケージの一覧
     * @param {Relation[]} allPackageRelations - パッケージ間の全関連
     * @param {CreatePackageLevelDiagramOptions} options
     * @returns {string|null}
     */
    function createPackageLevelDiagram(pkg, allPackages, allPackageRelations, options) {
        const {transitiveReductionEnabled, diagramDirection, nodeClickUrlCallback, focusedPackageFqn} = options;
        const { uniqueRelations, packageFqns } = buildVisibleDiagramRelations(
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

        const { source } = buildMermaidDiagramSource(
            packageFqns, uniqueRelations,
            { diagramDirection, nodeClickUrlCallback, focusedPackageFqn }
        );
        return source;
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
        const {edgeLines, linkStyles, mutualPairs} = buildDiagramEdgeLines(uniqueRelations, ensureNodeId, {subgraphNodeIds});

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
                const { edgeLengthByKey: lengths } = globalThis.Jig.graph.computeOutboundEdgeLengths({
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
            const edgeType = globalThis.Jig.mermaid.edgeTypeForLength(false, length);
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
            const nodeDefinition = globalThis.Jig.mermaid.getNodeDefinition(nodeId, displayLabel, 'package');
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
        const prefixDepth = getCommonPrefixDepth(packageFqnList);
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

    // IIFE から関数オブジェクトを return
    return {
        getAggregatedFqn,
        getCommonPrefix,
        getCommonPrefixDepth,
        getPackageFqnFromTypeFqn,
        isWithinPackageFilters,
        buildVisibleDiagramRelations,
        buildMutualDependencyPairs,
        buildParentFqns,
        filterParentFqnsWithRelations,
        createPackageLevelDiagram,
        buildMermaidDiagramSource,
        buildDiagramNodeMaps,
        buildDiagramEdgeLines,
        buildDiagramNodeLines,
        buildDiagramNodeLabel,
        buildDiagramSubgraphLabel,
        buildDiagramNodeTooltip,
        buildDiagramGroupTree,
        buildSubgraphLines,
    };
})();

// グローバル公開（ブラウザ用）
globalThis.Jig = globalThis.Jig ?? {};
globalThis.Jig.packageDiagram = {
    getAggregatedFqn: PackageDiagramModule.getAggregatedFqn,
    getPackageFqnFromTypeFqn: PackageDiagramModule.getPackageFqnFromTypeFqn,
    isWithinPackageFilters: PackageDiagramModule.isWithinPackageFilters,
    buildVisibleDiagramRelations: PackageDiagramModule.buildVisibleDiagramRelations,
    createPackageLevelDiagram: PackageDiagramModule.createPackageLevelDiagram,
    buildMermaidDiagramSource: PackageDiagramModule.buildMermaidDiagramSource,
    getCommonPrefix: PackageDiagramModule.getCommonPrefix,
    getCommonPrefixDepth: PackageDiagramModule.getCommonPrefixDepth,
    // Add missing functions for testing
    buildDiagramNodeMaps: PackageDiagramModule.buildDiagramNodeMaps,
    buildDiagramNodeLines: PackageDiagramModule.buildDiagramNodeLines,
    buildDiagramNodeLabel: PackageDiagramModule.buildDiagramNodeLabel,
    buildDiagramSubgraphLabel: PackageDiagramModule.buildDiagramSubgraphLabel,
    buildDiagramNodeTooltip: PackageDiagramModule.buildDiagramNodeTooltip,
    buildDiagramGroupTree: PackageDiagramModule.buildDiagramGroupTree,
    buildSubgraphLines: PackageDiagramModule.buildSubgraphLines,
    buildDiagramEdgeLines: PackageDiagramModule.buildDiagramEdgeLines,
};

// Test-only exports for Node; no-op in browsers.
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        detectStronglyConnectedComponents: MermaidDiagramModule.graph.detectStronglyConnectedComponents,
        transitiveReduction: MermaidDiagramModule.graph.transitiveReduction,
        computeSubgraphDepthMap: MermaidDiagramModule.graph.computeSubgraphDepthMap,
        computeOutboundEdgeLengths: MermaidDiagramModule.graph.computeOutboundEdgeLengths,
        flashButtonLabel: MermaidDiagramModule.mermaid.flashButtonLabel,
        renderTooLargeDiagram: MermaidDiagramModule.mermaid.renderTooLargeDiagram,
        renderWithControls: MermaidDiagramModule.mermaid.renderWithControls,
        MermaidBuilder: MermaidDiagramModule.mermaid.Builder,
        nodeStyleDefs: MermaidDiagramModule.mermaid.nodeStyleDefs,
        nodeShapes: MermaidDiagramModule.mermaid.nodeShapes,
        getNodeDefinition: MermaidDiagramModule.mermaid.getNodeDefinition,
        PackageDiagramModule,
        MermaidDiagramModule,
    };
}
