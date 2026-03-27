/* ===== Pure utility functions (no DOM side effects) ===== */

// Namespace
globalThis.Jig ??= {};
globalThis.Jig.glossary ??= {};
globalThis.Jig.mermaid ??= {};
globalThis.Jig.graph ??= {};

// Estimate Mermaid edge count from source
function estimateEdgeCount(source) {
    const text = source != null ? String(source) : "";
    if (!text) return 0;
    const matches = text.match(/<-->|<-\.-?>|-\.-?>|--?>|==?>|---/g);
    return matches ? matches.length : 0;
}

// 用語集ユーティリティ

/**
 * @typedef {Object} Term
 * @property {string} title
 * @property {string} simpleText TODO いる？？
 * @property {string} kind
 * @property {string} description
 */

/**
 * @param {string} fqn
 * @return {Term | undefined}
 */
globalThis.Jig.glossary.findTerm = function findTerm(fqn) {
    return globalThis.glossaryData?.[fqn];
};

/**
 * @param {string} fqn
 * @return {string}
 */
globalThis.Jig.glossary.typeSimpleName = function typeSimpleName(fqn) {
    return fqn.substring(fqn.lastIndexOf('.') + 1);
}

// 型FQNから Term{title, description} を取得。登録がなければフォールバック
globalThis.Jig.glossary.getTypeTerm = function getTypeTerm(fqn) {
    const term = globalThis.Jig.glossary.findTerm(fqn);
    if (term) return term;
    return { title: globalThis.Jig.glossary.typeSimpleName(fqn) || fqn, description: "" };
};


globalThis.Jig.glossary.getFieldTerm = function getFieldTerm(fqn) {
    var term = globalThis.Jig.glossary.findTerm(fqn);
    if (term) return term;
    return { title: fqn.substring(fqn.lastIndexOf('#') + 1) || fqn, description: "" };
}

/**
 * @param {string} fqn `com.example.Foo#bar(java.lang.String)` のような文字列
 * @param fallbackNameOnly
 * @return {{title: string, simpleText: string, kind: string, description: string, shortDeclaration: string}}
 */
globalThis.Jig.glossary.getMethodTerm = function getMethodTerm(fqn, fallbackNameOnly = false) {
    if (!fqn) throw Error("method fqn is required: " + fqn);

    const hashIdx = fqn.lastIndexOf('#');
    const parenIdx = fqn.indexOf('(', hashIdx);
    const closeParenIdx = fqn.lastIndexOf(')');
    if (hashIdx < 0 || parenIdx < 0 || closeParenIdx < 0 || hashIdx >= parenIdx || parenIdx >= closeParenIdx)
        throw Error("fqn is not a method?: " + fqn);

    // shortDeclaration構築
    const paramsStr = fqn.substring(parenIdx + 1, closeParenIdx);
    const paramsShortName = paramsStr.split(',').map(arg => globalThis.Jig.glossary.typeSimpleName(arg)).join(',');
    const typeShortName = globalThis.Jig.glossary.typeSimpleName(fqn.substring(0, hashIdx));
    const methodName = fqn.substring(hashIdx + 1, parenIdx);

    const shortDeclaration = `${typeShortName}#${methodName}(${paramsShortName})`;

    const term = globalThis.Jig.glossary.findTerm(fqn);
    if (term) {
        return { ...term, shortDeclaration: shortDeclaration };
    }

    // 引数を単純名に変換した FQN で再検索
    // 辞書の引数は実装依存なのでFQNの場合と両方ある。
    // TODO これだと複数引数で入り混じっている場合は対応できない。
    const mayBeFqn = fqn.substring(0, parenIdx + 1) + paramsShortName + ')';
    const term2 = globalThis.Jig.glossary.findTerm(mayBeFqn);
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
};

// FQNから一意なHTML IDを生成する
// HTMLおよびMermaidで使用する
globalThis.Jig.fqnToId = function fqnToId(prefix, fqn) {
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
    const sanitized = fqn.replace(/[^a-zA-Z0-9]/g, '_').substring(0, 10);
    return `${prefix}_${sanitized}_${hashStr}`;
};

// グラフ関連のユーティリティ

/**
 * 強連結成分(SCC)を抽出する (Tarjan's algorithm)
 * @param {Map<string, string[]>} graph
 * @returns {string[][]}
 */
globalThis.Jig.graph.detectStronglyConnectedComponents = function detectStronglyConnectedComponents(graph) {
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
};

/**
 * 推移的簡約(Transitive Reduction)を行う。
 * 直接の依存関係がある場合、他の経路でも到達可能ならその直接の依存を削除する。
 * ただし、サイクル（強連結成分内）の関連は削除しない。
 * @param {{from: string, to: string}[]} relations
 * @returns {{from: string, to: string}[]}
 */
globalThis.Jig.graph.transitiveReduction = function transitiveReduction(relations) {
    const graph = new Map();
    relations.forEach(relation => {
        if (!graph.has(relation.from)) graph.set(relation.from, []);
        graph.get(relation.from).push(relation.to);
    });

    const sccs = globalThis.Jig.graph.detectStronglyConnectedComponents(graph);
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
};

// Mermaid diagram builder
globalThis.Jig.mermaid.Builder = class MermaidBuilder {
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

    addNode(id, label, shape = '["$LABEL"]') {
        const escapedLabel = (label || "").replace(/"/g, '\\"');
        const nodeLine = `${id}${shape.replace('$LABEL', escapedLabel)}`;
        if (!this.nodes.includes(nodeLine)) {
            this.nodes.push(nodeLine);
        }
        return id;
    }

    addEdge(from, to, label = "", dotted = false) {
        const edgeType = dotted ? "-.->" : "-->";
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

    startSubgraph(id, label = id) {
        const subgraph = {id, label, lines: []};
        this.subgraphs.push(subgraph);
        return subgraph;
    }

    ensureSubgraph(map, key, label) {
        if (!map.has(key)) {
            map.set(key, this.startSubgraph(key, label));
        }
        return map.get(key);
    }

    addNodeToSubgraph(subgraph, id, label, shape = '["$LABEL"]') {
        const escapedLabel = (label || "").replace(/"/g, '\\"');
        const nodeLine = `    ${id}${shape.replace('$LABEL', escapedLabel)}`;
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
};

// Test-only exports for Node; no-op in browsers.
if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        estimateEdgeCount,
        fqnToId: globalThis.Jig.fqnToId,
        getTypeTerm: globalThis.Jig.glossary.getTypeTerm,
        getMethodTerm: globalThis.Jig.glossary.getMethodTerm,
        getFieldTerm: globalThis.Jig.glossary.getFieldTerm,
        findTerm: globalThis.Jig.glossary.findTerm,
        MermaidBuilder: globalThis.Jig.mermaid.Builder,
        detectStronglyConnectedComponents: globalThis.Jig.graph.detectStronglyConnectedComponents,
        transitiveReduction: globalThis.Jig.graph.transitiveReduction,
    };
}
