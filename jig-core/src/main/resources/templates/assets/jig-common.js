/* ===== Pure utility functions (no DOM side effects) ===== */

// Namespace
globalThis.Jig ??= {};
globalThis.Jig.glossary ??= {};

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

// Test-only exports for Node; no-op in browsers.
if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        estimateEdgeCount,
        fqnToId: globalThis.Jig.fqnToId,
        ...globalThis.Jig.glossary,
    };
}
