/* ===== Pure utility functions (no DOM side effects) ===== */

// Namespace
globalThis.Jig ??= {};
globalThis.Jig.glossary ??= {};
globalThis.Jig.mermaid ??= {};

// Estimate Mermaid edge count from source
function estimateEdgeCount(source) {
    const text = source != null ? String(source) : "";
    if (!text) return 0;
    const matches = text.match(/<-->|<-\.-?>|-\.-?>|--?>|==?>|---/g);
    return matches ? matches.length : 0;
}

// 用語集ユーティリティ

// FQNから用語を検索。登録がなければ undefined
globalThis.Jig.glossary.findTerm = function findTerm(fqn) {
    return globalThis.glossaryData?.[fqn];
};

// 型FQNから Term{title, description} を取得。登録がなければフォールバック
globalThis.Jig.glossary.getTypeTerm = function getTypeTerm(fqn) {
    const term = globalThis.Jig.glossary.findTerm(fqn);
    if (term) return term;
    return { title: fqn.substring(fqn.lastIndexOf('.') + 1) || fqn, description: "" };
};

// メソッドFQN（"pkg.Class#method(args)"形式）から Term{title, description} を取得
globalThis.Jig.glossary.getMethodTerm = function getMethodTerm(fqn, fallbackNameOnly = false) {
    if (!fqn) return { title: "", description: "" };
    const term = globalThis.Jig.glossary.findTerm(fqn);
    if (term) return term;

    const hashIdx = fqn.lastIndexOf('#');
    const parenIdx = fqn.indexOf('(', hashIdx);
    const closeParenIdx = fqn.lastIndexOf(')');
    if (hashIdx >= 0 && parenIdx > hashIdx) {
        const methodName = fqn.substring(hashIdx + 1, parenIdx);
        const argsStr = closeParenIdx > parenIdx ? fqn.substring(parenIdx + 1, closeParenIdx) : '';

        // 引数を単純名に変換した FQN で再検索（例: Foo#bar(java.lang.String) → Foo#bar(String)）
        const simpleArgsFqn = fqn.substring(0, parenIdx + 1)
            + (argsStr ? argsStr.split(',').map(arg => {
                const trimmed = arg.trim();
                return trimmed.substring(trimmed.lastIndexOf('.') + 1);
            }).join(',') : '')
            + ')';
        const term2 = globalThis.Jig.glossary.findTerm(simpleArgsFqn);
        if (term2) return term2;

        // フォールバック: methodName 形式
        if (fallbackNameOnly) {
            return { title: methodName, description: "" };
        }

        // フォールバック: methodName(simpleArgs) 形式
        const simpleArgs = argsStr
            ? argsStr.split(',').map(arg => {
                const trimmed = arg.trim();
                return trimmed.substring(trimmed.lastIndexOf('.') + 1);
            }).join(', ')
            : '';
        return { title: `${methodName}(${simpleArgs})`, description: "" };
    }
    return { title: fqn, description: "" };
};

// FQNから一意なHTML IDを生成する
globalThis.Jig.fqnToId = function fqnToId(prefix, fqn) {
    // マルチバイト文字をハッシュ化して一意なIDを生成
    let hash = 0;
    for (let i = 0; i < fqn.length; i++) {
        const char = fqn.charCodeAt(i);
        hash = ((hash << 5) - hash) + char;
        hash = hash & hash; // Convert to 32bit integer
    }
    const hashStr = Math.abs(hash).toString(36); // 36進数で短くする
    const sanitized = fqn.replace(/[^a-zA-Z0-9]/g, '-').substring(0, 10);
    return `${prefix}-${sanitized}-${hashStr}`;
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
        findTerm: globalThis.Jig.glossary.findTerm,
        MermaidBuilder: globalThis.Jig.mermaid.Builder,
    };
}
