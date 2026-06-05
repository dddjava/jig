globalThis.Jig ??= {};

globalThis.Jig.glossary = (() => {

    // GitHubアイコン（octicon mark-github）
    const SOURCE_ICON_SVG = '<svg viewBox="0 0 16 16" width="14" height="14" aria-hidden="true"><path fill="currentColor" d="M8 0c4.42 0 8 3.58 8 8a8.013 8.013 0 0 1-5.45 7.59c-.4.08-.55-.17-.55-.38 0-.27.01-1.13.01-2.2 0-.75-.25-1.23-.54-1.48 1.78-.2 3.65-.88 3.65-3.95 0-.88-.31-1.59-.82-2.15.08-.2.36-1.02-.08-2.12 0 0-.67-.22-2.2.82-.64-.18-1.32-.27-2-.27-.68 0-1.36.09-2 .27-1.53-1.03-2.2-.82-2.2-.82-.44 1.1-.16 1.92-.08 2.12-.51.56-.82 1.28-.82 2.15 0 3.06 1.86 3.75 3.64 3.95-.23.2-.44.55-.51 1.07-.46.21-1.61.55-2.33-.66-.15-.24-.6-.83-1.23-.82-.67.01-.27.38.01.53.34.19.73.9.82 1.13.16.45.68 1.31 2.69.94 0 .67.01 1.3.01 1.49 0 .21-.15.45-.55.38A7.995 7.995 0 0 1 0 8c0-4.42 3.58-8 8-8Z"></path></svg>';

    /**
     * @param {string} fqn
     * @return {Term | undefined}
     */
    function findTerm(fqn) {
        return globalThis.Jig.data.glossary.getTerm(fqn);
    }

    /**
     * 型/パッケージ/メソッド等のFQNからソース（GitHub等）へのリンク要素を生成する。
     * sourcePath と blobUrlPrefix が揃う場合のみ要素を返し、それ以外は null。
     * メソッドFQN等で用語が見つからない場合は型FQNにフォールバックして解決する。
     * @param {string} fqn
     * @return {HTMLElement | null}
     */
    function sourceLink(fqn) {
        const blobUrlPrefix = globalThis.Jig.data.summary.getGit()?.blobUrlPrefix;
        if (!blobUrlPrefix) return null;
        const sourcePath = findTerm(fqn)?.sourcePath ?? findTerm(fqn.split('#')[0])?.sourcePath;
        if (!sourcePath) return null;
        return globalThis.Jig.dom.createElement("a", {
            className: "source-link",
            innerHTML: SOURCE_ICON_SVG,
            attributes: {
                href: `${blobUrlPrefix}/${sourcePath}`,
                target: "_blank",
                rel: "noopener",
                title: "ソースを開く",
                "data-i18n-attr": "title",
            },
        });
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

    function getTermOrSimpleName(fqn) {
        const term = findTerm(fqn);
        if (term) return term;
        return {title: typeSimpleName(fqn) || fqn, description: ""};
    }

    function getPackageTerm(fqn) {
        return getTermOrSimpleName(fqn);
    }

    function getTypeTerm(fqn) {
        return getTermOrSimpleName(fqn);
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
            type: (fqn) => !fqn ? '' : showPhysicalName ? typeSimpleName(fqn) : getTypeTerm(fqn).title,
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
        sourceLink,
        typeSimpleName,
        methodSimpleName,
        makeLabels,
    };
})();

if (typeof module !== "undefined" && module.exports) {
    module.exports = globalThis.Jig.glossary;
}
