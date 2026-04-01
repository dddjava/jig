/* ===== Pure utility functions (no DOM side effects) ===== */

// Namespace
globalThis.Jig ??= {};
globalThis.Jig.glossary ??= {};
globalThis.Jig.graph ??= {};

// Estimate Mermaid edge count from source
function estimateEdgeCount(source) {
    const text = source != null ? String(source) : "";
    if (!text) return 0;
    const matches = text.match(/<-->|<-\.-?>|-\.-?>|--?>|==?>|---/g);
    return matches ? matches.length : 0;
}


/**
 * @typedef {Object} Term
 * @property {string} title
 * @property {string} simpleText TODO いる？？
 * @property {string} kind
 * @property {string} description
 */

// 用語集ユーティリティ
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
        return { title: typeSimpleName(fqn) || fqn, description: "" };
    }

    function getTypeTerm(fqn) {
        const term = findTerm(fqn);
        if (term) return term;
        return { title: typeSimpleName(fqn) || fqn, description: "" };
    }

    function getFieldTerm(fqn) {
        const term = findTerm(fqn);
        if (term) return term;
        return { title: fqn.substring(fqn.lastIndexOf('#') + 1) || fqn, description: "" };
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
            return { ...term, shortDeclaration: shortDeclaration };
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
    const reversed = fqn.split('.').reverse().join('_');
    const sanitized = reversed.replace(/[^a-zA-Z0-9]/g, '_').substring(0, 10);
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


/**
 * subgraph 内部のエッジのみを使ってノード深さを計算する。
 * 深さの起点は内部入次数0ノード（なければ全ノード）を 1 とする。
 * @param {{nodesInSubgraph: Iterable<string>, edges: {from: string, to: string}[]}} params
 * @returns {{depthMap: Map<string, number>, maxDepth: number}}
 */
globalThis.Jig.graph.computeSubgraphDepthMap = function computeSubgraphDepthMap(params) {
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
};

/**
 * subgraph 内部ノードから外部ノードへのエッジ長を計算する。
 * @param {{nodesInSubgraph: Iterable<string>, edges: {from: string, to: string}[], minLength?: number}} params
 * @returns {{edgeLengthByKey: Map<string, number>, depthMap: Map<string, number>, maxDepth: number}}
 */
globalThis.Jig.graph.computeOutboundEdgeLengths = function computeOutboundEdgeLengths(params) {
    const nodes = new Set(params?.nodesInSubgraph || []);
    const edges = Array.isArray(params?.edges) ? params.edges : [];
    const minLength = Math.max(1, Number(params?.minLength) || 1);
    const {depthMap, maxDepth} = globalThis.Jig.graph.computeSubgraphDepthMap({
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
};

// Test-only exports for Node; no-op in browsers.
if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        estimateEdgeCount,
        fqnToId: globalThis.Jig.fqnToId,
        detectStronglyConnectedComponents: globalThis.Jig.graph.detectStronglyConnectedComponents,
        transitiveReduction: globalThis.Jig.graph.transitiveReduction,
        computeSubgraphDepthMap: globalThis.Jig.graph.computeSubgraphDepthMap,
        computeOutboundEdgeLengths: globalThis.Jig.graph.computeOutboundEdgeLengths,
        ...globalThis.Jig.glossary,
    };
}
