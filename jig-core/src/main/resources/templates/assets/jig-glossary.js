globalThis.Jig ??= {};

globalThis.Jig.glossary = (() => {

    /**
     * @param {string} fqn
     * @return {Term | undefined}
     */
    function findTerm(fqn) {
        return globalThis.Jig.data.glossary.getTerm(fqn);
    }

    /**
     * @param {string} fqn
     * @return {string}
     */
    function typeSimpleName(fqn) {
        return fqn.substring(fqn.lastIndexOf('.') + 1);
    }

    function methodSimpleName(fqn) {
        if (!fqn) return '';
        const hashIdx = fqn.indexOf('#');
        if (hashIdx === -1) return typeSimpleName(fqn);
        const parenIdx = fqn.indexOf('(', hashIdx);
        return parenIdx === -1 ? fqn.slice(hashIdx + 1) : fqn.slice(hashIdx + 1, parenIdx);
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

    /**
     * @param {boolean} showPhysicalName
     * @return {{type: (function(string): string), pkg: (function(string): string), method: (function(string): string)}}
     */
    function makeLabels(showPhysicalName) {
        return {
            type: (fqn) => showPhysicalName ? typeSimpleName(fqn) : getTypeTerm(fqn).title,
            pkg: (fqn) => showPhysicalName ? typeSimpleName(fqn) : getPackageTerm(fqn).title,
            method: (fqn) => showPhysicalName ? methodSimpleName(fqn) : getMethodTerm(fqn, true).title,
        };
    }

    return {
        getPackageTerm,
        getTypeTerm,
        getFieldTerm,
        getMethodTerm,
        findTerm,
        typeSimpleName,
        methodSimpleName,
        makeLabels,
    };
})();

if (typeof module !== "undefined" && module.exports) {
    module.exports = globalThis.Jig.glossary;
}
