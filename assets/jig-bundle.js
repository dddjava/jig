// Source: jig-util.js
globalThis.Jig ??= {};

globalThis.Jig.util = (() => {

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
     * @param {string} fqn
     * @return {number} 深さ
     */
    function getPackageDepth(fqn) {
        if (!fqn || fqn === '(default)') return 0;
        return fqn.split('.').length;
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
     * FQNから一意なHTML IDを生成する
     * HTMLおよびMermaidで使用する
     *
     * @param prefix
     * @param fqn
     * @return {string}
     */
    function fqnToId(prefix, fqn) {
        // DJB2 系の 32bit ハッシュ。衝突した場合は同一画面内で別の FQN が同じ ID を持ち、
        // ページ内アンカー (`#id`) や Mermaid のクリック先が混線する。FQN は通常重複しないため
        // 実害は出ていないが、衝突が観測されたらハッシュ幅拡張または完全な FQN を ID 化に切替える。
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
    }

    /**
     * TypeRef から FQN を再帰的に収集する
     * コレクション型（List<Order> など）の場合、typeArgumentRefs に含まれる型も収集する
     *
     * @param {Object|null} typeRef - TypeRef オブジェクト（{fqn: string, typeArgumentRefs?: TypeRef[]}）
     * @returns {string[]} 収集された FQN の配列（重複あり）
     *
     * @example
     * collectTypeRefFqns({fqn: "java.util.List", typeArgumentRefs: [{fqn: "com.example.Order"}]});
     * // => ["java.util.List", "com.example.Order"]
     */
    function collectTypeRefFqns(typeRef) {
        if (!typeRef) return [];
        const fqns = [typeRef.fqn];
        (typeRef.typeArgumentRefs || []).forEach(argRef => {
            fqns.push(...collectTypeRefFqns(argRef));
        });
        return fqns;
    }

    function pushToMap(map, key, value) {
        if (!map.has(key)) map.set(key, []);
        map.get(key).push(value);
    }

    function addToSetMap(map, key, value) {
        if (!map.has(key)) map.set(key, new Set());
        map.get(key).add(value);
    }

    /**
     * items をパッケージFQN単位でグループ化する。
     * パッケージのないFQN（ドットなし）は "(default)" にグループ化される。
     * @template T
     * @param {T[]} items
     * @param {function(T): string} getFqn - アイテムのFQNを返す関数
     * @returns {Map<string, T[]>} パッケージFQN → アイテム配列
     */
    function groupByPackageFqn(items, getFqn) {
        const byPackage = new Map();
        items.forEach(item => {
            pushToMap(byPackage, getPackageFqnFromTypeFqn(getFqn(item)), item);
        });
        return byPackage;
    }

    /**
     * items の型FQNからパッケージ階層ツリーを構築する。
     * 各itemのパッケージFQNから最上位セグメントまでの中間パッケージノードを補完して親子連結する。
     * パッケージのないFQN（ドットなし）は "(default)" ノードに属する。
     *
     * @template T
     * @param {T[]} items
     * @param {function(T): string} getFqn - アイテムの型FQNを返す関数
     * @returns {{fqn: string, items: T[], children: Object[]}[]} ルートノード配列（fqn昇順、childrenもfqn昇順）
     */
    function buildPackageTree(items, getFqn) {
        const nodes = new Map();
        const ensureNode = (fqn) => {
            if (!nodes.has(fqn)) nodes.set(fqn, {fqn, items: [], children: []});
            return nodes.get(fqn);
        };
        const roots = new Map();
        items.forEach(item => {
            const fqn = getFqn(item);
            const packageFqn = getPackageFqnFromTypeFqn(fqn);
            ensureNode(packageFqn).items.push(item);

            let currentFqn = packageFqn;
            while (true) {
                const parentIdx = currentFqn.lastIndexOf('.');
                if (parentIdx === -1) {
                    roots.set(currentFqn, nodes.get(currentFqn));
                    break;
                }
                const parentFqn = currentFqn.slice(0, parentIdx);
                const parentExisted = nodes.has(parentFqn);
                const parent = ensureNode(parentFqn);
                const child = nodes.get(currentFqn);
                if (parent.children.includes(child)) break;
                parent.children.push(child);
                // 親が既存なら祖先への連結は完了している
                if (parentExisted) break;
                currentFqn = parentFqn;
            }
        });
        const sortByFqn = nodeList => nodeList.sort((a, b) => a.fqn.localeCompare(b.fqn));
        nodes.forEach(node => sortByFqn(node.children));
        return sortByFqn([...roots.values()]);
    }

    return {
        fqnToId,
        getCommonPrefix,
        getCommonPrefixDepth,
        getPackageDepth,
        getPackageFqnFromTypeFqn,
        isWithinPackageFilters,
        getAggregatedFqn,
        collectTypeRefFqns,
        pushToMap,
        addToSetMap,
        groupByPackageFqn,
        buildPackageTree,
    }
})();

if (typeof module !== "undefined" && module.exports) {
    module.exports = globalThis.Jig.util;
}

// Source: jig-data.js
globalThis.Jig ??= {};

/**
 * globalThis.<xxx>Data への唯一の窓口。
 *
 * 各 asset スクリプト（domain.js / usecase.js / ...）は globalThis を直接参照せず、
 * この名前空間を経由してデータを取得する。派生キャッシュ（FQN→type の Map など）も
 * 元データを汚さず、このモジュール内部のクロージャに保持する。
 */
globalThis.Jig.data = (() => {

    /** @type {Map<string, object>|null} */
    let domainTypesMap = null;
    /** @type {Set<string>|null} */
    let domainFqnSet = null;
    /** @type {object[]|null} */
    let domainPackages = null;
    /** @type {Map<string, object[]>|null} */
    let domainChildPackagesMap = null;
    /** @type {Map<string, object>|null} */
    let usecaseTypesMap = null;

    function buildDomainPackages(domainPackageRoots, types) {
        const packageTypesMap = new Map();

        for (const type of types) {
            const lastDot = type.fqn.lastIndexOf('.');
            if (lastDot < 0) continue;
            const pkgFqn = type.fqn.substring(0, lastDot);

            globalThis.Jig.util.pushToMap(packageTypesMap, pkgFqn, {fqn: type.fqn});

            // 子パッケージのみを持つ中間パッケージのナビゲーションを成立させるため、
            // pkgFqn から domainPackageRoots に到達するまで空の親パッケージも Map に登録する
            let current = pkgFqn;
            while (!domainPackageRoots.includes(current)) {
                const parentDot = current.lastIndexOf('.');
                if (parentDot < 0) break;
                const parent = current.substring(0, parentDot);
                const isUnderRoot = domainPackageRoots.some(
                    root => parent === root || parent.startsWith(root + '.'));
                if (!isUnderRoot) break;
                if (!packageTypesMap.has(parent)) packageTypesMap.set(parent, []);
                current = parent;
            }
        }

        return Array.from(packageTypesMap.entries())
            .map(([fqn, pkgTypes]) => ({
                fqn,
                types: pkgTypes.sort((a, b) => a.fqn.localeCompare(b.fqn))
            }))
            .sort((a, b) => a.fqn.localeCompare(b.fqn));
    }

    function buildChildPackagesMap(packages) {
        const map = new Map(packages.map(p => [p.fqn, []]));
        packages.forEach(p => {
            const parentFqn = p.fqn.substring(0, p.fqn.lastIndexOf('.'));
            if (map.has(parentFqn)) map.get(parentFqn).push(p);
        });
        return map;
    }

    const domain = {
        get() {
            return globalThis.domainData;
        },
        has() {
            return !!globalThis.domainData;
        },
        getTypes() {
            return globalThis.domainData?.types ?? [];
        },
        getTypesMap() {
            if (!domainTypesMap) {
                domainTypesMap = new Map(domain.getTypes().map(t => [t.fqn, t]));
            }
            return domainTypesMap;
        },
        getType(fqn) {
            return domain.getTypesMap().get(fqn);
        },
        getDomainFqnSet() {
            if (!domainFqnSet) {
                domainFqnSet = new Set(domain.getTypes().map(t => t.fqn));
            }
            return domainFqnSet;
        },
        getPackages() {
            if (!domainPackages) {
                const data = globalThis.domainData;
                domainPackages = data ? buildDomainPackages(data.domainPackageRoots, data.types) : [];
            }
            return domainPackages;
        },
        getChildPackagesMap() {
            if (!domainChildPackagesMap) {
                domainChildPackagesMap = buildChildPackagesMap(domain.getPackages());
            }
            return domainChildPackagesMap;
        },
    };

    const glossary = {
        get() {
            return globalThis.glossaryData;
        },
        has() {
            return !!globalThis.glossaryData;
        },
        getTerm(fqn) {
            return globalThis.glossaryData?.terms?.[fqn];
        },
        getSourcePath(fqn) {
            return globalThis.glossaryData?.sourcePaths?.[fqn];
        },
    };

    const usecase = {
        get() {
            return globalThis.usecaseData;
        },
        has() {
            return !!globalThis.usecaseData;
        },
        getTypesMap() {
            if (!usecaseTypesMap) {
                usecaseTypesMap = new Map(
                    (globalThis.usecaseData?.usecases ?? []).map(t => [t.fqn, t])
                );
            }
            return usecaseTypesMap;
        },
        getType(fqn) {
            return usecase.getTypesMap().get(fqn);
        },
    };

    const inbound = {
        get() {
            return globalThis.inboundData ?? null;
        },
        getControllers() {
            return globalThis.inboundData?.inboundAdapters ?? [];
        },
    };

    const outbound = {
        get() {
            return globalThis.outboundData;
        },
    };

    const pkg = {
        get() {
            return globalThis.packageData;
        },
    };

    const insight = {
        get() {
            return globalThis.insightData ?? null;
        },
    };

    const list = {
        get() {
            return globalThis.listData;
        },
    };

    const library = {
        get() {
            return globalThis.libraryDependencyData;
        },
    };

    const navigation = {
        get() {
            return globalThis.navigationData;
        },
        getLinks() {
            return globalThis.navigationData?.links ?? [];
        },
    };

    const summary = {
        get() {
            return globalThis.summaryData;
        },
        getGit() {
            return globalThis.summaryData?.git ?? null;
        },
    };

    const typeRelations = {
        get() {
            return globalThis.typeRelationsData;
        },
        getRelations() {
            return globalThis.typeRelationsData?.relations ?? [];
        },
    };

    /**
     * domain の派生キャッシュ（typesMap / fqnSet / packages / childPackagesMap）を破棄する。
     * データが入れ替わる際（init の再実行など）に呼び出す。
     */
    function resetCache() {
        domainTypesMap = null;
        domainFqnSet = null;
        domainPackages = null;
        domainChildPackagesMap = null;
        usecaseTypesMap = null;
    }

    /**
     * 利用可能なデータに基づいて型リンクリゾルバーを生成する。
     * domain型があれば domain.html へ、usecase型があれば usecase.html へリンクする。
     * 現在のページと同じドキュメントへのリンクはページ内アンカー（#）になる。
     *
     * @returns {(fqn: string) => {href?: string, className?: string, text?: string} | null}
     */
    function createTypeLinkResolver() {
        if (!domain.has() && !usecase.has() && !glossary.has()) return null;

        const currentPage = (typeof location !== 'undefined')
            ? location.pathname.split('/').pop()
            : '';

        // 解決順は配列の順序に従う。各 entry は対象データの保有判定・型取得・遷移先ページ等を表す。
        const resolvers = [
            {has: domain.has, find: domain.getType, page: 'domain.html', idPrefix: 'domain', deprecatedAware: true},
            {has: usecase.has, find: usecase.getType, page: 'usecase.html', idPrefix: 'type', deprecatedAware: true},
            {has: glossary.has, find: glossary.getTerm, page: 'glossary.html', idPrefix: 'term', deprecatedAware: false},
        ];

        return function (fqn) {
            for (const r of resolvers) {
                if (!r.has()) continue;
                const found = r.find(fqn);
                if (!found) continue;
                const prefix = (currentPage === r.page) ? '#' : `${r.page}#`;
                const result = {href: prefix + globalThis.Jig.util.fqnToId(r.idPrefix, fqn)};
                if (r.deprecatedAware && found.isDeprecated) result.className = 'deprecated';
                return result;
            }
            return {
                className: 'weak',
                text: fqn.substring(fqn.lastIndexOf('.') + 1)
            };
        };
    }

    return {
        domain,
        glossary,
        usecase,
        inbound,
        outbound,
        package: pkg,
        insight,
        list,
        library,
        navigation,
        summary,
        typeRelations,
        resetCache,
        createTypeLinkResolver,
    };
})();

if (typeof module !== "undefined" && module.exports) {
    module.exports = globalThis.Jig.data;
}

// Source: jig-glossary.js
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
     * メソッドFQNは型FQNにフォールバックして解決する。
     * @param {string} fqn
     * @return {HTMLElement | null}
     */
    function sourceLink(fqn) {
        const blobUrlPrefix = globalThis.Jig.data.summary.getGit()?.blobUrlPrefix;
        if (!blobUrlPrefix) return null;
        const getSourcePath = globalThis.Jig.data.glossary.getSourcePath;
        const sourcePath = getSourcePath(fqn) ?? getSourcePath(fqn.split('#')[0]);
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

// Source: jig-dom.js
globalThis.Jig ??= {};

// ブラウザバックなどで該当要素に移動する
// Safariなどではブラウザバックでも移動するが、ChromeやEdgeだと移動しない。
// なのでpopstateイベントでlocationからhashを取得し、hashがある場合はその要素に移動する
if (typeof window !== 'undefined') {
    window.addEventListener("popstate", function (event) {
        const hash = event.target.location.hash;

        if (hash) {
            const anchor = document.getElementById(hash.substring(1))
            if (anchor) {
                anchor.scrollIntoView();
            }
        }
    });
}

globalThis.Jig.dom = (() => {

    // --- Base DOM utility ---

    function createElement(tagName, options = {}) {
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
        // i18n: true で data-i18n マーカーを付与（textContent をキーとして翻訳対象にする）
        if (options.i18n) {
            element.setAttribute("data-i18n", options.i18n === true ? "" : String(options.i18n));
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
    }

    function createCell(text, className) {
        return createElement("td", {className, textContent: text});
    }

    /**
     * textContent を i18n キー（日本語）として翻訳対象にする要素を生成する高レベル API。
     * createElement(tag, {textContent: key, i18n: true, ...options}) のショートカット。
     * children を伴う複合要素や label の mixed content では createElement に i18n: true を直接渡す。
     */
    function i18nText(tagName, key, options = {}) {
        const el = createElement(tagName, {...options, textContent: key, i18n: true});
        el.dataset.i18nOriginal = key;
        return el;
    }

    // --- Markdown ---

    function parseMarkdown(markdown) {
        const source = markdown != null ? String(markdown) : "";
        if (globalThis.marked && typeof globalThis.marked.parse === "function") {
            return globalThis.marked.parse(source);
        }
        return source;
    }

    function createMarkdownElement(markdown) {
        const element = createElement("div", {
            className: "markdown",
            innerHTML: parseMarkdown(markdown)
        });
        // Javadoc に書いた ```mermaid コードブロックをダイアグラムとして描画する
        globalThis.Jig?.mermaid?.renderMarkdownDiagrams?.(element);
        return element;
    }

    // --- CSV utility ---

    function escapeCsvValue(value) {
        const text = String(value ?? "").replace(/\r\n|\r/g, "\n");
        return `"${text.replace(/"/g, "\"\"")}"`;
    }

    function buildCsv(header, rows) {
        return [header, ...rows].map(row => row.map(escapeCsvValue).join(",")).join("\r\n");
    }

    function downloadCsv(text, filename) {
        const blob = new Blob([text], {type: "text/csv;charset=utf-8;"});
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement("a");
        anchor.href = url;
        anchor.download = filename;
        document.body.appendChild(anchor);
        anchor.click();
        anchor.remove();
        URL.revokeObjectURL(url);
    }

    // --- Kind badge ---

    const KIND_BADGE = {"パッケージ": "P", "クラス": "C", "メソッド": "M", "フィールド": "F"};

    function kindBadgeChar(kind) {
        return KIND_BADGE[kind] ?? (kind ? kind.charAt(0).toUpperCase() : "?");
    }

    function kindBadgeElement(kind) {
        return createElement("span", {
            className: "kind-badge",
            attributes: {"data-kind": kind},
            textContent: kindBadgeChar(kind),
        });
    }

    // --- Type link ---

    let typeLinkResolver = null;

    function setTypeLinkResolver(resolver) {
        typeLinkResolver = (typeof resolver === "function") ? resolver : null;
    }

    function clearTypeLinkResolver() {
        typeLinkResolver = null;
    }

    function getTypeLinkResolver() {
        return typeLinkResolver;
    }

    // Jig.glossary (jig-glossary.js) のロードが必要
    function createTypeLink(fqn, className = undefined) {
        // 配列型（例: Hoge[], Hoge[][]）はベース型で解決し、[] を付け直す
        const arraySuffix = fqn.match(/(\[\])+$/)?.[0] ?? '';
        const baseFqn = arraySuffix ? fqn.slice(0, -arraySuffix.length) : fqn;

        const resolved = typeLinkResolver?.(baseFqn);
        const title = (resolved?.text ?? globalThis.Jig.glossary.getTypeTerm(baseFqn).title) + arraySuffix;
        const classes = [className, resolved?.className].filter(Boolean).join(' ') || undefined;
        if (resolved?.href) {
            return createElement('a', {
                className: classes,
                attributes: {href: resolved.href},
                textContent: title
            });
        }
        return createElement('span', {
            className: classes,
            textContent: title
        });
    }

    function createElementForTypeRef(typeRef, className = undefined) {
        if (typeRef.typeArgumentRefs && typeRef.typeArgumentRefs.length) {
            const typeElements = createTypeLink(typeRef.fqn);
            const argumentElements = typeRef.typeArgumentRefs
                .map(argumentTypeRef => createElementForTypeRef(argumentTypeRef))
                // カンマを挟む。HTML Elementが文字列になってしまうのでjoinは使えない。
                .flatMap((v, i) => i ? [', ', v] : [v]);

            return createElement("span", {
                className: className,
                children: [typeElements, '<', ...argumentElements, '>']
            });
        }

        return createTypeLink(typeRef.fqn, className);
    }

    // --- Type detail builders ---

    function createParameterElement(param) {
        return createElement("span", {
            children: param.nameSource === 'METHOD_PARAMETERS'
                ? [param.name + ': ', createElementForTypeRef(param.typeRef)]
                : [createElementForTypeRef(param.typeRef)]
        });
    }

    function createMethodItem(method) {
        const methodTerm = globalThis.Jig.glossary.getMethodTerm(method.fqn, true);

        const paramElements = method.parameters
            .map(param => createParameterElement(param))
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
                createElementForTypeRef(method.returnTypeRef)
            ]
        });

        const children = [signatureEl];
        if (methodTerm.description) {
            children.push(createMarkdownElement(methodTerm.description));
        }

        return createElement("div", {
            className: "method-item",
            children
        });
    }

    function createMethodIOSection(parameters, returnTypeRef) {
        const inputDd = parameters.length > 0
            ? createElement("dd", {
                className: "entrypoint-item__params",
                children: parameters.flatMap(param => [
                    createElementForTypeRef(param.typeRef),
                    createElement("span", {
                        className: "entrypoint-item__param-name",
                        textContent: param.nameSource === 'METHOD_PARAMETERS' ? param.name : ''
                    })
                ])
            })
            : createElement("dd", {className: "entrypoint-item__empty", textContent: "-"});

        return createElement("dl", {
            className: "entrypoint-item__io",
            children: [
                i18nText("dt", "入力"),
                inputDd,
                i18nText("dt", "出力"),
                createElement("dd", {children: [createElementForTypeRef(returnTypeRef)]})
            ]
        });
    }

    function createFieldItem(field) {
        return createElement("div", {
            className: "field-item",
            children: [
                createElement("div", {
                    className: "field-signature",
                    children: [
                        createElement("span", {
                            className: "field-name" + (field.isDeprecated ? " deprecated" : ""),
                            textContent: field.name
                        }),
                        createElement("span", {className: "field-type-sep", textContent: ":"}),
                        createElementForTypeRef(field.typeRef)
                    ]
                })
            ]
        });
    }

    function createMemberSection(items, title, className, createItem) {
        return createElement("section", {
            className,
            children: [
                ...(title !== undefined ? [i18nText("h4", title)] : []),
                ...items.map(createItem)
            ]
        });
    }

    function createFieldsList(fields, options = {}) {
        if (fields.length === 0) return null;
        const title = options.showTitle !== false ? "フィールド" : undefined;
        return createMemberSection(fields, title, "methods-section fields", createFieldItem);
    }

    function createMethodsList(kind, methods, options = {}) {
        if (methods.length === 0) return null;
        const title = options.showTitle !== false ? kind : undefined;
        return createMemberSection(methods, title, "methods-section", createMethodItem);
    }

    // --- Card builders ---

    function createItemCard({id, title, tagName = "section", extraClass} = {}) {
        return createElement(tagName, {
            id,
            className: ["jig-card", "jig-card--item", extraClass].filter(Boolean).join(" "),
            children: title !== undefined ? [i18nText("h4", title)] : []
        });
    }

    function createTypeCard({id, title, fqn, kind, attributes, titleSuffix, tagName = "section", extraClass} = {}) {
        const titleEl = typeof title === 'string' ? createElement("span", {textContent: title}) : title;
        const titleContent = id
            ? createElement("a", {className: "card-title-anchor", attributes: {href: `#${id}`}, children: [titleEl]})
            : titleEl;
        const h3Children = kind !== undefined ? [kindBadgeElement(kind), titleContent] : [titleContent];
        if (titleSuffix) h3Children.push(titleSuffix);

        const card = createElement(tagName, {
            id,
            className: ["jig-card", "jig-card--type", extraClass].filter(Boolean).join(" "),
            attributes,
            children: [createElement("h3", {children: h3Children})]
        });

        if (fqn != null) {
            card.appendChild(typeof fqn === 'string'
                ? createElement("div", {className: "fully-qualified-name", textContent: fqn})
                : fqn);
        }

        return card;
    }

    // --- Sidebar ---

    // 折りたたみ状態の表現（hiddenクラスとトグルのaria）を一箇所で扱う
    function setSidebarListExpanded(list, toggle, expanded) {
        list.classList.toggle("in-page-sidebar__links--hidden", !expanded);
        toggle.setAttribute("aria-expanded", String(expanded));
        toggle.setAttribute("aria-label", expanded ? "折りたたむ" : "展開");
    }

    function createSidebarToggle(targetEl) {
        const toggle = createElement("button", {
            className: "in-page-sidebar__toggle",
            attributes: {"aria-expanded": "true", "aria-label": "折りたたむ"}
        });
        toggle.addEventListener("click", () => {
            const collapsing = toggle.getAttribute("aria-expanded") === "true";
            setSidebarListExpanded(targetEl, toggle, !collapsing);
        });
        return toggle;
    }

    function buildCollapsibleTitle(title, links) {
        return createElement("p", {
            className: "in-page-sidebar__title in-page-sidebar__title--collapsible",
            children: [i18nText("span", title), createSidebarToggle(links)]
        });
    }

    /**
     * サイドバーのリーフ（リンク1つのli）を生成する
     * @param {string} href
     * @param {string} label
     */
    function createSidebarLeaf(href, label) {
        return createElement("li", {
            className: "in-page-sidebar__item",
            children: [
                createElement("a", {
                    className: "in-page-sidebar__link",
                    attributes: {href},
                    textContent: label
                })
            ]
        });
    }

    function createSection(items) {
        if (!items || items.length === 0) return null;

        const links = createElement("ul", {
            className: "in-page-sidebar__links",
            children: items.map(({id, label}) => createSidebarLeaf("#" + id, label))
        });

        return createElement("section", {
            className: "in-page-sidebar__section",
            children: [links]
        });
    }

    function renderSection(container, items) {
        if (!container) return;
        const section = createSection(items);
        if (section) {
            container.appendChild(section);
        }
    }

    /**
     * サイドバーのツリーセクションを描画する。3種類の要素で構成する:
     * - グループ: ページ内の大きな塊（例: リクエストハンドラ）。折りたたみ可能で、背景色で他と区別する
     * - パッケージ: FQNの1階層。折りたたみと、packageHref によるメインセクションへのリンクを持つ。
     *   用語のない単一子パッケージ（item無し）は省略し、最深のパッケージのみ表示する
     * - リーフ: renderLeaf で描画されるアイテム（クラスやメソッド）
     *
     * items が空の場合は何も描画しない。
     *
     * @template T
     * @param {Element} container
     * @param {Object} options
     * @param {string} options.title - グループ名
     * @param {T[]} options.items
     * @param {function(T): string} options.getFqn - itemの型FQNを返す関数
     * @param {function(T): Element} options.renderLeaf - リーフのli要素を生成する関数
     * @param {function({fqn: string, items: T[], children: Object[]}): (string|null)} [options.packageHref]
     *        パッケージノードのリンク先を返す関数。nullを返すとリンクなしのラベルになる
     * @param {boolean} [options.showTitle=true] - falseの場合、グループ見出し（背景色・積み重ね
     *        ピン留め含む）を描画せず、パッケージ階層のulを直接containerへ追加する。
     *        グループが1つしかなく見出しが冗長なページで使う
     */
    function renderTreeSection(container, {title, items, getFqn, renderLeaf, packageHref, showTitle = true}) {
        if (!container || !items || items.length === 0) return;
        const glossary = globalThis.Jig.glossary;
        const roots = globalThis.Jig.util.buildPackageTree(items, getFqn);

        function renderNode(node) {
            // 用語のない単一子パッケージ（item無し）は省略し、最深のパッケージのみ表示する
            let current = node;
            while (current.children.length === 1 && current.items.length === 0 && !glossary.findTerm(current.fqn)) {
                current = current.children[0];
            }

            const childList = createElement("ul", {className: "in-page-sidebar__links"});
            current.children.forEach(child => childList.appendChild(renderNode(child)));
            current.items.forEach(item => childList.appendChild(renderLeaf(item)));

            const labelText = glossary.getPackageTerm(current.fqn).title;
            const href = packageHref ? packageHref(current) : null;
            const label = href
                ? createElement("a", {
                    className: "in-page-sidebar__package-link",
                    attributes: {href},
                    textContent: labelText
                })
                : createElement("span", {textContent: labelText});

            return createElement("li", {
                className: "in-page-sidebar__item",
                children: [
                    createElement("div", {
                        className: "in-page-sidebar__item-header",
                        children: [label, createSidebarToggle(childList)]
                    }),
                    childList
                ]
            });
        }

        const list = createElement("ul", {className: "in-page-sidebar__links"});
        if (roots.length === 1 && roots[0].fqn === "(default)" && roots[0].children.length === 0) {
            // パッケージを持たない項目（テーブル名など）だけの場合は、階層を挟まずリーフを直接並べる
            roots[0].items.forEach(item => list.appendChild(renderLeaf(item)));
        } else {
            roots.forEach(root => list.appendChild(renderNode(root)));
        }

        if (!showTitle) {
            // グループが1つしかなく見出しが冗長な場合、見出し・背景色・ピン留めなしで
            // パッケージ階層のulをそのまま追加する
            container.appendChild(list);
            return;
        }

        const titleEl = buildCollapsibleTitle(title, list);
        titleEl.classList.add("in-page-sidebar__title--group");
        const toggle = titleEl.querySelector(".in-page-sidebar__toggle");

        container.appendChild(createElement("section", {
            className: "in-page-sidebar__section in-page-sidebar__section--group",
            children: [titleEl, list]
        }));

        const scroller = sidebarScrollerOf(container);
        recomputeGroupTitleOffsets(scroller);
        initGroupTitlePinning(scroller, titleEl, toggle, list);
    }

    /**
     * 開閉可能な子リストを持たない、単一リンクのグループをサイドバーに追加する。
     * 他のグループ（renderTreeSection）と同じ背景色・積み重ねピン留めの並びにしたい、
     * ツリー展開の必要がない項目（例: 単一ページへのリンクのみのセクション）で使う。
     *
     * @param {Element} container
     * @param {Object} options
     * @param {string} options.title - リンクのラベル
     * @param {string} options.href
     */
    function renderLinkGroup(container, {title, href}) {
        if (!container) return;

        const titleEl = createElement("p", {
            className: "in-page-sidebar__title in-page-sidebar__title--group",
            children: [createElement("a", {
                className: "in-page-sidebar__link",
                attributes: {href},
                textContent: title
            })]
        });

        container.appendChild(createElement("section", {
            className: "in-page-sidebar__section in-page-sidebar__section--group",
            children: [titleEl]
        }));

        recomputeGroupTitleOffsets(sidebarScrollerOf(container));
    }

    // グループ見出しがスクロール範囲外に押し出された場合も下部に積み重なって留まるよう、
    // スクロール領域内の全グループ見出しの積み重ねオフセットを再計算する
    function recomputeGroupTitleOffsets(scroller) {
        const groupTitles = [...scroller.querySelectorAll(".in-page-sidebar__title--group")];
        groupTitles.forEach((groupTitle, index) => {
            groupTitle.style.bottom = `calc(${groupTitles.length - 1 - index} * var(--group-title-height))`;
        });
    }

    const GROUP_TITLE_PINNED_CLASS = "in-page-sidebar__title--pinned";
    // ピン留めのscrollリスナーをスクロール領域ごとに1つに保つ
    const pinningInitializedScrollers = new WeakSet();

    // グループ見出しの sticky / ピン留めの基準となるスクロール領域。
    // 描画先がスクロール領域内のネストした要素でも動作するよう closest で解決する
    function sidebarScrollerOf(container) {
        return container.closest(".in-page-sidebar__list") ?? container;
    }

    function isContentBelowView(scroller, list) {
        if (typeof scroller.getBoundingClientRect !== "function") return false;
        const scrollerRect = scroller.getBoundingClientRect();
        if (!scrollerRect.height) return false;
        if (!list || list.classList.contains("in-page-sidebar__links--hidden")) return false;
        return list.getBoundingClientRect().top >= scrollerRect.bottom - 1;
    }

    // スクロール領域内の全グループ見出しのピン留め状態を更新する。
    // 見出しの次の要素がそのグループの内容リスト（renderTreeSectionの構造）
    function updatePinnedStates(scroller) {
        scroller.querySelectorAll(".in-page-sidebar__title--group").forEach(titleEl => {
            titleEl.classList.toggle(GROUP_TITLE_PINNED_CLASS, isContentBelowView(scroller, titleEl.nextElementSibling));
        });
    }

    /**
     * グループ内容がスクロール範囲外（下）にあり見出しだけが下部に留まっている状態を
     * 「閉じている」扱いにする。閉じた見た目のクラスを付け、クリックで展開して
     * グループが見える位置までスクロールする。
     */
    function initGroupTitlePinning(scroller, titleEl, toggle, list) {
        // ピン留め中はトグルの折りたたみ動作より優先するため、captureで処理する
        titleEl.addEventListener("click", (e) => {
            const collapsed = list.classList.contains("in-page-sidebar__links--hidden");
            const pinned = titleEl.classList.contains(GROUP_TITLE_PINNED_CLASS);
            e.stopPropagation();

            if (!collapsed && !pinned) {
                // 内容が見えている状態でのクリックは折りたたむ（展開時のクリックと対称にする）
                setSidebarListExpanded(list, toggle, false);
                updatePinnedStates(scroller);
                return;
            }

            if (collapsed) {
                setSidebarListExpanded(list, toggle, true);
            }

            // グループ見出しが上部に来る位置までスクロールして内容を見せる
            if (typeof scroller.getBoundingClientRect === "function") {
                const scrollerRect = scroller.getBoundingClientRect();
                const titleRect = titleEl.getBoundingClientRect();
                const listGap = typeof globalThis.getComputedStyle === "function"
                    ? parseFloat(globalThis.getComputedStyle(scroller).rowGap) || 0
                    : 0;
                scroller.scrollTop += list.getBoundingClientRect().top
                    - scrollerRect.top - titleRect.height - listGap;
            }
            updatePinnedStates(scroller);
        }, true);

        if (!pinningInitializedScrollers.has(scroller)) {
            pinningInitializedScrollers.add(scroller);
            scroller.addEventListener("scroll", () => updatePinnedStates(scroller));
        }
        updatePinnedStates(scroller);
    }

    function initSidebarTextFilter(inputId, onChange) {
        const input = document.getElementById(inputId);
        if (!input) return;
        input.addEventListener('input', () => onChange(input.value.trim()));
    }

    // 折りたたまれた祖先（in-page-sidebar__links--hidden）を展開し、対応するトグルの状態も合わせる
    function expandSidebarAncestors(sidebar, link) {
        let el = link.parentElement;
        while (el && el !== sidebar) {
            if (el.classList.contains("in-page-sidebar__links--hidden")) {
                const toggle = el.previousElementSibling?.querySelector(".in-page-sidebar__toggle");
                if (toggle) {
                    setSidebarListExpanded(el, toggle, true);
                } else {
                    el.classList.remove("in-page-sidebar__links--hidden");
                }
            }
            el = el.parentElement;
        }
    }

    // サイドバーのスクロール領域内で該当リンクを表示する。メインコンテンツのスクロール位置は動かさない
    function scrollSidebarLinkIntoView(sidebar, link) {
        const container = sidebar.querySelector(".in-page-sidebar__list") || sidebar;
        const containerRect = container.getBoundingClientRect();
        const linkRect = link.getBoundingClientRect();
        if (linkRect.top >= containerRect.top && linkRect.bottom <= containerRect.bottom) return;
        container.scrollTop += (linkRect.top + linkRect.bottom) / 2 - (containerRect.top + containerRect.bottom) / 2;
    }

    // 指定したhashに対応するサイドバーリンクをハイライトし、折りたたまれた祖先を展開する。
    // scrollIntoSidebar が true なら該当リンクをサイドバー内に表示する。
    function applyActiveSidebarLink(sidebar, hash, scrollIntoSidebar = false) {
        sidebar.querySelectorAll(".in-page-sidebar__link--active")
            .forEach(el => el.classList.remove("in-page-sidebar__link--active"));

        if (!hash || hash === "#") return;

        // 同一アンカーへのリンクが複数グループに現れることがあるため、全て同期する
        const links = [
            ...sidebar.querySelectorAll(".in-page-sidebar__link"),
            ...sidebar.querySelectorAll(".in-page-sidebar__package-link"),
        ].filter(a => a.getAttribute("href") === hash);
        if (links.length === 0) return;

        links.forEach(link => {
            link.classList.add("in-page-sidebar__link--active");
            expandSidebarAncestors(sidebar, link);
        });
        if (scrollIntoSidebar) scrollSidebarLinkIntoView(sidebar, links[0]);
    }

    // location.hash に対応するサイドバーリンクをハイライトする。hashchangeやブラウザの戻る/進み、
    // 初回ロード時の同期に使う
    function syncActiveSidebarLink(scrollIntoSidebar = false) {
        if (typeof document === "undefined") return;
        const sidebar = document.querySelector(".in-page-sidebar");
        if (!sidebar) return;
        applyActiveSidebarLink(sidebar, location.hash, scrollIntoSidebar);
    }

    // サイドバー内のリンククリックを即座にハイライトする。移動先のレンダリングが重い場合、
    // hashchangeの発火（≒描画完了後）を待つとハイライトが遅れて見えるため、
    // クリック時点でリンク自身のhrefを元に先行してハイライトする。
    // hashchangeによる同期（syncActiveSidebarLink）はブラウザの戻る/進み用に引き続き動作する
    function initSidebarClickHighlight(sidebar) {
        if (!sidebar || sidebar.dataset.clickHighlightInitialized) return;
        sidebar.dataset.clickHighlightInitialized = "true";
        sidebar.addEventListener("click", (e) => {
            const link = e.target.closest("a[href^='#']");
            if (!link) return;
            applyActiveSidebarLink(sidebar, link.getAttribute("href"));
        });
    }

    function initSidebarCollapseBtn() {
        const collapseBtn = document.getElementById('sidebar-collapse-btn');
        if (!collapseBtn || collapseBtn.dataset.initialized) return;
        collapseBtn.dataset.initialized = 'true';
        const nav = collapseBtn.closest('nav');
        if (!nav) return;
        collapseBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            const collapsed = nav.classList.toggle('sidebar--collapsed');
            collapseBtn.setAttribute('aria-expanded', String(!collapsed));
        });
        nav.addEventListener('click', () => {
            if (nav.classList.contains('sidebar--collapsed')) {
                nav.classList.remove('sidebar--collapsed');
                collapseBtn.setAttribute('aria-expanded', 'true');
            }
        });
    }

    // --- Tab section ---

    function buildTabSection(tabDefs, options = {}) {
        const {className, initialActiveId, onTabChange} = options;
        const tabsBar = createElement("div", {className: "jig-tabs"});
        const panels = {};
        const buttons = [];

        const activeId = initialActiveId ?? tabDefs[0]?.id;

        tabDefs.forEach(tab => {
            panels[tab.id] = createElement("div", {
                className: ["jig-tab-panel", tab.id !== activeId ? "hidden" : null].filter(Boolean).join(" ")
            });
            const btn = i18nText("button", tab.label, {
                className: ["jig-tab", tab.id === activeId ? "active" : null].filter(Boolean).join(" "),
            });
            buttons.push(btn);
            btn.addEventListener('click', () => {
                buttons.forEach(b => b.classList.remove('active'));
                Object.values(panels).forEach(p => p.classList.add('hidden'));
                btn.classList.add('active');
                panels[tab.id].classList.remove('hidden');
                if (onTabChange) onTabChange(tab.id);
            });
            tabsBar.appendChild(btn);
        });

        const panelEls = Object.values(panels);
        const section = createElement("div", {
            className,
            children: [tabsBar, ...panelEls],
        });
        return {panels, section};
    }

    // --- Table ---

    function renderTableRows(tableId, items, buildRow, {clear = false} = {}) {
        const tableBody = document.querySelector(`#${tableId} tbody`);
        if (!tableBody) return;
        if (clear) tableBody.innerHTML = "";
        const fragment = document.createDocumentFragment();
        items.forEach(item => {
            const row = createElement("tr");
            buildRow(row, item);
            fragment.appendChild(row);
        });
        tableBody.appendChild(fragment);
    }

    function setupSortableTables() {
        function sortTable(event) {
            const headerColumn = event.target;
            const table = headerColumn.closest("table");
            const columnIndex = Array.from(headerColumn.parentNode.children).indexOf(headerColumn);

            const rows = Array.from(table.querySelectorAll("tbody tr"));

            const orderFlag = headerColumn.dataset.orderFlag === "true";

            let type = "string";
            const firstRow = rows[0];
            if (firstRow) {
                const cell = firstRow.cells[columnIndex];
                if (cell && cell.classList.contains("number")) {
                    type = "number";
                }
            }

            rows.sort(function (a, b) {
                const aValue = a.cells[columnIndex]?.textContent ?? "";
                const bValue = b.cells[columnIndex]?.textContent ?? "";

                // 数値は降順、文字は昇順
                if (type === "number") {
                    const aNumber = parseFloat(aValue) || 0;
                    const bNumber = parseFloat(bValue) || 0;
                    return (aNumber - bNumber) * (orderFlag ? 1 : -1);
                }
                return (aValue.localeCompare(bValue)) * (orderFlag ? -1 : 1);
            });

            rows.forEach(row => table.querySelector("tbody").appendChild(row));

            headerColumn.dataset.orderFlag = (!orderFlag).toString();
        }

        document.querySelectorAll("table.sortable").forEach(table => {
            const headers = table.querySelectorAll("thead th");
            headers.forEach(header => {
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

    // --- Common UI setup ---

    function normalizeNavigationHref(href) {
        return String(href || "").replace(/^\.\//, "");
    }

    /**
     * hover で開閉するヘッダ用ドロップダウンナビ (.jig-header-nav) の構造を作る共通ヘルパ。
     * setupHeaderNavigation / setupLanguageSwitcher の両方から使う。
     *
     * @param {HTMLElement} triggerEl 既に組み立て済みのトリガー要素 (.jig-header-nav__trigger)
     * @param {string} [modifierClass] バリエーション用の追加クラス名（例: "jig-lang-switcher"）
     * @returns {{wrapper: HTMLElement, dropdown: HTMLElement}}
     */
    function createDropdownNav(triggerEl, modifierClass) {
        const dropdown = createElement("ul", {
            className: ["jig-header-nav__dropdown", modifierClass && `${modifierClass}__dropdown`].filter(Boolean).join(" "),
            attributes: {role: "list"}
        });
        const wrapper = createElement("div", {
            className: ["jig-header-nav", modifierClass].filter(Boolean).join(" "),
            children: [triggerEl, dropdown]
        });
        return {wrapper, dropdown};
    }

    function appendDropdownItem(dropdown, child, isCurrent) {
        dropdown.appendChild(createElement("li", {
            className: "jig-header-nav__item" + (isCurrent ? " jig-header-nav__item--current" : ""),
            children: [child]
        }));
    }

    function setupHeaderNavigation() {
        if (document.body.classList.contains("index")) return;

        const navigationData = globalThis.Jig.data.navigation.get();
        if (!navigationData || !Array.isArray(navigationData.links) || navigationData.links.length === 0) return;

        const header = document.querySelector("header.top") || document.querySelector("header");
        if (!header) return;
        if (header.querySelector(".jig-header-nav")) return;

        const pageTitleEl = header.querySelector(".jig-page-title");
        if (!pageTitleEl) return;

        const currentFileName = (location.pathname.split("/").pop() || "");
        const normalizedCurrent = normalizeNavigationHref(currentFileName);

        // ナビゲーションデータの当該リンクのラベルが locale 対応済みなのでそれを使う。
        // 該当が見つからない場合はテンプレート静的テキストにフォールバック。
        const currentLink = navigationData.links.find(
            link => link && normalizeNavigationHref(link.href) === normalizedCurrent
        );
        const triggerLabel = (currentLink && currentLink.label != null)
            ? String(currentLink.label)
            : pageTitleEl.textContent;

        // ラベルは日本語キー（textContent）で描画し i18n マーカーで翻訳対象にする
        const trigger = i18nText("span", triggerLabel, {className: "jig-header-nav__trigger"});
        const {wrapper, dropdown} = createDropdownNav(trigger);

        navigationData.links.forEach(link => {
            if (!link) return;
            const href = normalizeNavigationHref(link.href);
            const label = link.label != null ? String(link.label) : href;
            if (!href) return;

            const isCurrent = (href === normalizedCurrent);
            const child = isCurrent
                ? i18nText("span", label)
                : i18nText("a", label, {attributes: {href}});
            appendDropdownItem(dropdown, child, isCurrent);
        });

        pageTitleEl.replaceWith(wrapper);
    }

    function setupLanguageSwitcher() {
        const i18n = globalThis.Jig?.i18n;
        if (!i18n || typeof i18n.setLanguage !== "function") return;

        const header = document.querySelector("header.top") || document.querySelector("header");
        if (!header) return;
        if (header.querySelector(".jig-lang-switcher")) return;

        const langs = i18n.availableLanguages();
        if (!Array.isArray(langs) || langs.length < 2) return;

        const labels = {ja: "日本語", en: "English"};
        const display = lang => labels[lang] || lang.toUpperCase();

        const trigger = createElement("span", {
            className: "jig-header-nav__trigger jig-lang-switcher__trigger",
            textContent: display(i18n.currentLanguage())
        });
        const {wrapper, dropdown} = createDropdownNav(trigger, "jig-lang-switcher");

        const renderItems = () => {
            dropdown.replaceChildren();
            const current = i18n.currentLanguage();
            langs.forEach(lang => {
                const isCurrent = (lang === current);
                const child = isCurrent
                    ? createElement("span", {textContent: display(lang)})
                    : createElement("a", {
                        textContent: display(lang),
                        attributes: {href: "#", "data-lang": lang}
                    });
                if (!isCurrent) {
                    child.addEventListener("click", event => {
                        event.preventDefault();
                        i18n.setLanguage(lang);
                    });
                }
                appendDropdownItem(dropdown, child, isCurrent);
            });
        };

        renderItems();

        document.addEventListener("jig:locale-change", event => {
            trigger.textContent = display(event?.detail?.lang || i18n.currentLanguage());
            renderItems();
        });

        header.appendChild(wrapper);
    }

    function setupDocumentHelp() {
        const helpContent = document.getElementById("jig-document-description");
        if (!helpContent || !helpContent.textContent) return;

        const header = document.querySelector("header.top") || document.querySelector("header");
        if (!header) return;

        const helpButton = createElement("button", {
            className: "jig-help-button",
            attributes: {"aria-label": "ドキュメントの説明を表示", "title": "ドキュメントの説明"},
            innerHTML: `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>`
        });

        const helpPanel = createElement("section", {
            id: "jig-document-help-panel",
            children: [
                createElement("div", {
                    className: "help-content",
                    children: [helpContent]
                })
            ]
        });

        helpButton.addEventListener("click", () => {
            helpPanel.classList.toggle("is-active");
        });

        header.appendChild(helpButton);
        header.after(helpPanel);
        helpContent.classList.remove("hidden");
    }

    function initCommonUi() {
        setupHeaderNavigation();
        setupLanguageSwitcher();
        setupDocumentHelp();
        if (globalThis.Jig?.data?.createTypeLinkResolver) {
            setTypeLinkResolver(globalThis.Jig.data.createTypeLinkResolver());
        }
        // 動的挿入された data-i18n 要素は jig-i18n.js の MutationObserver が初期 locale に追従させる

        if (typeof window !== "undefined") {
            // ブラウザの戻る/進みに合わせてサイドバーの該当箇所を表示する
            window.addEventListener("hashchange", () => syncActiveSidebarLink(true));
            // 初回ロード時のサイドバー描画はこのあとの DOMContentLoaded ハンドラで行われるため、
            // 描画完了後に同期する
            if (typeof window.requestAnimationFrame === "function") {
                window.requestAnimationFrame(() => syncActiveSidebarLink(true));
            } else {
                syncActiveSidebarLink(true);
            }
        }
        if (typeof document !== "undefined") {
            // サイドバー内リンクのクリックは、移動先の描画完了（hashchange）を待たずに即座にハイライトする
            initSidebarClickHighlight(document.querySelector(".in-page-sidebar"));
        }
    }

    return {
        createElement,
        createCell,
        i18nText,
        parseMarkdown,
        createMarkdownElement,
        escapeCsvValue,
        buildCsv,
        downloadCsv,
        renderTableRows,
        setupSortableTables,
        initCommonUi,

        card: {
            type: createTypeCard,
            item: createItemCard,
        },
        kind: {
            badgeChar: kindBadgeChar,
            badgeElement: kindBadgeElement,
        },
        type: {
            setResolver: setTypeLinkResolver,
            clearResolver: clearTypeLinkResolver,
            getResolver: getTypeLinkResolver,
            refElement: createElementForTypeRef,
            parameterElement: createParameterElement,
            methodIOSection: createMethodIOSection,
            fieldItem: createFieldItem,
            fieldsList: createFieldsList,
            methodItem: createMethodItem,
            methodsList: createMethodsList,
        },
        sidebar: {
            section: createSection,
            leaf: createSidebarLeaf,
            renderSection,
            renderTreeSection,
            renderLinkGroup,
            initTextFilter: initSidebarTextFilter,
            initCollapseBtn: initSidebarCollapseBtn,
            createToggle: createSidebarToggle,
            syncActiveLink: syncActiveSidebarLink,
            initClickHighlight: initSidebarClickHighlight,
        },
        tab: {
            buildSection: buildTabSection,
        },
    };
})();

if (typeof document !== 'undefined') {
    document.addEventListener("DOMContentLoaded", function () {
        globalThis.Jig.dom.initCommonUi();
    });
}

// Source: jig-mermaid.js
globalThis.Jig ??= {};

globalThis.Jig.mermaid = (() => {
    const builder = (() => {
        const nodeStyleDefs = {
            inbound: "fill:#E8F0FE,stroke:#2E5C8A",
            usecase: "fill:#E6F8F0,stroke:#2D7A4A",
            outbound: "fill:#FFF0E6,stroke:#CC6600",
            inactive: "fill:#e0e0e0,stroke:#aaa",
            domain: "fill:#FEF9E7,stroke:#B7950B"
        };

        const nodeShapes = {
            method: '(["$LABEL"])',
            class: '["$LABEL"]',
            package: '@{shape: st-rect, label: "$LABEL"}',
            database: '[("$LABEL")]',
            external: '(("$LABEL"))',
            request: '>"$LABEL"]',
            scheduler: '@{shape: delay, label: "$LABEL"}',
            queue: '@{shape: horizontal-cylinder, label: "$LABEL"}'
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
                this.nodeSet = new Set();
                this.edges = [];
                this.subgraphs = [];
                this.styles = [];
                this.clicks = [];
                this.edgeSet = new Set();
                this.clickSet = new Set();
            }

            sanitize(id) {
                return (id || "").replace(/[^a-zA-Z0-9]/g, '_');
            }

            addNode(id, label, shape = 'class') {
                this.nodeSet.add(getNodeDefinition(id, label, shape));
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

            addClick(id, url, tooltip) {
                if (!id || !url || this.clickSet.has(id)) return;
                this.clickSet.add(id);
                const tooltipPart = tooltip ? ` "${escapeMermaidText(tooltip)}"` : '';
                this.clicks.push(`click ${id} href "${url}"${tooltipPart}`);
            }

            addTooltip(id, tooltip) {
                if (!id || !tooltip || this.clickSet.has(id)) return;
                this.clickSet.add(id);
                this.clicks.push(`click ${id} _jigNoop "${escapeMermaidText(tooltip)}"`);
            }

            addCallbackClick(id, handlerName, tooltip) {
                if (!id || !handlerName || this.clickSet.has(id)) return;
                this.clickSet.add(id);
                const tooltipPart = tooltip ? ` "${escapeMermaidText(tooltip)}"` : '';
                this.clicks.push(`click ${id} ${handlerName}${tooltipPart}`);
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
                const subgraph = {id, label, lineSet: new Set()};
                if (direction) {
                    const safeDirection = (direction === 'TD') ? 'TB' : direction;
                    subgraph.lineSet.add(`direction ${safeDirection}`);
                }
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
                subgraph.lineSet.add(getNodeDefinition(id, label, shape));
                return id;
            }

            build(direction = "LR") {
                let code = `graph ${direction}\n`;
                this.subgraphs.forEach(sg => {
                    code += `  subgraph ${sg.id} ["${sg.label}"]\n`;
                    sg.lineSet.forEach(line => {
                        code += `    ${line}\n`;
                    });
                    code += `  end\n`;
                });
                this.nodeSet.forEach(node => {
                    code += `  ${node}\n`;
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
                return this.nodeSet.size === 0 && this.edges.length === 0 && this.subgraphs.length === 0;
            }
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
         * @param {Set<string>} packageFqns
         * @param {Relation[]} uniqueRelations
         * @param {MermaidDiagramSourceOptions} options
         */
        function buildMermaidDiagramSource(packageFqns, uniqueRelations, options) {
            const {diagramDirection, focusedPackageFqn, clickHandlerName, nodeClickUrlCallback} = options;

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
            const {nodeIdByFqn, nodeIdToFqn, nodeLabelById, ensureNodeId} = buildDiagramNodeMaps(packageFqnsToDisplay, {showPhysicalName: options.showPhysicalName});
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
                    subgraphNodeIds,
                    showPhysicalName: options.showPhysicalName,
                }
            );
            const {
                edgeLines,
                linkStyles,
                mutualPairs
            } = buildDiagramEdgeLines(uniqueRelations, ensureNodeId, {subgraphNodeIds});

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
         * 関連探索ダイアグラムのMermaidソースを生成する（ノード色分けあり）
         * @param {Set<string>} packageFqns - 表示するパッケージFQNセット
         * @param {Relation[]} uniqueRelations - 表示する関連
         * @param {{targetFqns: Set<string>, callerFqns: Set<string>, calleeFqns: Set<string>, diagramDirection: string, clickHandlerName: string}} options
         * @returns {{source: string, nodeIdToFqn: Map<string, string>}}
         */
        function buildExploreDiagramSource(packageFqns, uniqueRelations, options) {
            const {targetFqns, callerFqns, calleeFqns, diagramDirection, clickHandlerName} = options;

            const allParentFqns = buildParentFqns(packageFqns);
            const parentFqnsWithRelations = filterParentFqnsWithRelations(allParentFqns, uniqueRelations);

            const packageFqnsToDisplay = new Set(Array.from(packageFqns).filter(fqn => {
                if (allParentFqns.has(fqn)) {
                    // 明示的に選択されたターゲットは関連の有無によらず常に表示する
                    if (targetFqns.has(fqn)) return true;
                    return parentFqnsWithRelations.has(fqn);
                }
                return true;
            }));

            const lines = [
                "---",
                "config:",
                "  theme: 'default'",
                "  themeVariables:",
                "    clusterBkg: '#ffffde'",
                "---",
                `graph ${diagramDirection}`];
            const {nodeIdByFqn, nodeIdToFqn, nodeLabelById, ensureNodeId} = buildDiagramNodeMaps(packageFqnsToDisplay, {showPhysicalName: options.showPhysicalName});
            const subgraphNodeIds = new Map();

            const nodeLines = buildDiagramNodeLines(
                packageFqnsToDisplay,
                nodeIdByFqn,
                {
                    nodeIdToFqn,
                    nodeLabelById,
                    escapeMermaidText,
                    clickHandlerName,
                    parentFqnsWithRelations,
                    subgraphNodeIds,
                    showPhysicalName: options.showPhysicalName,
                }
            );
            const {edgeLines, linkStyles} = buildDiagramEdgeLines(uniqueRelations, ensureNodeId, {subgraphNodeIds});

            nodeLines.forEach(line => lines.push(line));
            edgeLines.forEach(line => lines.push(line));
            linkStyles.forEach(styleLine => lines.push(styleLine));

            lines.push('classDef parentPackage fill:#ffffce,stroke:#aaaa00,stroke-dasharray:10 3');
            lines.push('classDef exploreTarget fill:#ffffce,stroke:#aaaa00,stroke-width:3px,font-weight:bold');
            lines.push('classDef exploreCaller fill:#E8F0FE,stroke:#2E5C8A,stroke-width:2px');
            lines.push('classDef exploreCallee fill:#FFF0E6,stroke:#CC6600,stroke-width:2px');

            // 各ノードにクラスを適用（優先度: target > caller > callee）
            packageFqnsToDisplay.forEach(fqn => {
                if (!nodeIdByFqn.has(fqn)) return;
                const nodeId = nodeIdByFqn.get(fqn);
                if (targetFqns && targetFqns.has(fqn)) {
                    lines.push(`class ${nodeId} exploreTarget`);
                } else if (callerFqns && callerFqns.has(fqn)) {
                    lines.push(`class ${nodeId} exploreCaller`);
                } else if (calleeFqns && calleeFqns.has(fqn)) {
                    lines.push(`class ${nodeId} exploreCallee`);
                }
            });

            return {source: lines.join('\n'), nodeIdToFqn};
        }

        /**
         * ダイアグラムで使用する各種Mapを構築する
         * @param {Set<string>} packageFqns - 対象パッケージFQNセット
         * @param {{showPhysicalName?: boolean}} [options={}]
         * @returns {{nodeIdByFqn: Map<string, string>, nodeIdToFqn: Map<string, string>, nodeLabelById: Map<string, string>, ensureNodeId: function(string): string}} - ノードマップとノードID生成関数
         */
        function buildDiagramNodeMaps(packageFqns, options = {}) {
            const showPhysicalName = options.showPhysicalName ?? false;
            const nodeIdByFqn = new Map();
            const nodeIdToFqn = new Map();
            const nodeLabelById = new Map();
            let nodeIndex = 0;
            const ensureNodeId = fqn => {
                if (nodeIdByFqn.has(fqn)) return nodeIdByFqn.get(fqn);
                const nodeId = `P${nodeIndex++}`;
                nodeIdByFqn.set(fqn, nodeId);
                nodeIdToFqn.set(nodeId, fqn);
                const label = showPhysicalName ? fqn.split('.').pop() : globalThis.Jig.glossary.getPackageTerm(fqn).title;
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
                    const {edgeLengthByKey: lengths} = graph.computeOutboundEdgeLengths({
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
                const edgeType = edgeTypeForLength(false, length);
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
            const {nodeIdToFqn, nodeLabelById, escapeMermaidText, clickHandlerName, nodeClickUrlCallback, showPhysicalName} = options;

            const packageFqnList = Array.from(packageFqns).sort();
            const parentFqns = buildParentFqns(packageFqns);
            const rootGroup = buildDiagramGroupTree(packageFqnList, nodeIdByFqn);
            const addNodeLines = (lines, nodeId, parentSubgraphFqn) => {
                const fqn = nodeIdToFqn.get(nodeId);
                const displayLabel = buildDiagramNodeLabel(nodeLabelById.get(nodeId), fqn, parentSubgraphFqn);
                const nodeDefinition = getNodeDefinition(nodeId, displayLabel, 'package');
                lines.push(nodeDefinition);
                if (clickHandlerName) {
                    const tooltip = escapeMermaidText(buildDiagramNodeTooltip(fqn));
                    lines.push(`click ${nodeId} ${clickHandlerName} "${tooltip}"`);
                }
                if (nodeClickUrlCallback && fqn) {
                    const url = escapeMermaidText(nodeClickUrlCallback(fqn));
                    const tooltip = escapeMermaidText(buildDiagramNodeTooltip(fqn));
                    lines.push(`click ${nodeId} href "${url}" "${tooltip}"`);
                }
                if (fqn && parentFqns.has(fqn)) {
                    lines.push(`class ${nodeId} parentPackage`);
                }
            };
            return buildSubgraphLines(rootGroup, addNodeLines, escapeMermaidText, options.subgraphNodeIds, showPhysicalName);
        }

        function buildDiagramNodeLabel(displayLabel, fqn, parentSubgraphFqn) {
            if (!fqn) return displayLabel ?? '';
            if (displayLabel === fqn && parentSubgraphFqn && fqn.startsWith(`${parentSubgraphFqn}.`)) {
                return fqn.substring(parentSubgraphFqn.length + 1);
            }
            return displayLabel ?? '';
        }

        function buildDiagramSubgraphLabel(subgraphFqn, showPhysicalName = false) {
            if (!subgraphFqn) return '';
            if (showPhysicalName) return subgraphFqn.split('.').pop();
            return globalThis.Jig.glossary.getPackageTerm(subgraphFqn).title;
        }

        function buildDiagramNodeTooltip(fqn) {
            return fqn ?? '';
        }

        function buildDiagramGroupTree(packageFqnList, nodeIdByFqn) {
            const prefixDepth = Jig.util.getCommonPrefixDepth(packageFqnList);
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

        function buildSubgraphLines(rootGroup, addNodeLines, escapeMermaidText, subgraphNodeIds = null, showPhysicalName = false) {
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
                    const label = buildDiagramSubgraphLabel(child.key, showPhysicalName);
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
                    from: Jig.util.getAggregatedFqn(relation.from, aggregationDepth),
                    to: Jig.util.getAggregatedFqn(relation.to, aggregationDepth),
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
                ? packages.filter(item => Jig.util.isWithinPackageFilters(item.fqn, packageFilterFqn))
                : packages;
            const packageFqns = new Set(visiblePackages.map(item => Jig.util.getAggregatedFqn(item.fqn, aggregationDepth)));
            const filteredRelations = packageFilterFqn.length > 0
                ? relations.filter(relation => Jig.util.isWithinPackageFilters(relation.from, packageFilterFqn) && Jig.util.isWithinPackageFilters(relation.to, packageFilterFqn))
                : relations;
            const filteredCauseRelationEvidence = packageFilterFqn.length > 0
                ? causeRelationEvidence.filter(relation => {
                    const fromPackage = Jig.util.getPackageFqnFromTypeFqn(relation.from);
                    const toPackage = Jig.util.getPackageFqnFromTypeFqn(relation.to);
                    return Jig.util.isWithinPackageFilters(fromPackage, packageFilterFqn) && Jig.util.isWithinPackageFilters(toPackage, packageFilterFqn);
                })
                : causeRelationEvidence;

            let uniqueRelations = aggregationRelations(filteredRelations, aggregationDepth);
            if (transitiveReductionEnabled) {
                uniqueRelations = graph.transitiveReduction(uniqueRelations);
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

        function createBuilder() {
            const builder = new MermaidBuilder();
            builder.applyThemeClassDefs();
            return builder;
        }

        return {
            MermaidBuilder,
            createBuilder,
            nodeStyleDefs,
            nodeShapes,
            escapeId,
            escapeLabel,
            escapeMermaidText,
            getNodeDefinition,
            edgeTypeForLength,
            buildMermaidDiagramSource,
            buildExploreDiagramSource,
            buildDiagramNodeMaps,
            buildDiagramEdgeLines,
            buildDiagramNodeLines,
            buildDiagramNodeLabel,
            buildDiagramSubgraphLabel,
            buildDiagramNodeTooltip,
            buildDiagramGroupTree,
            buildSubgraphLines,
            buildVisibleDiagramRelations,
            buildMutualDependencyPairs,
            buildParentFqns,
        };
    })();

    // グラフ関連のユーティリティ
    const graph = (() => {
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
                Jig.util.pushToMap(graph, relation.from, relation.to);
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
                Jig.util.pushToMap(acyclicGraph, edge.from, edge.to);
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

        return {
            computeOutboundEdgeLengths,
            computeSubgraphDepthMap,
            transitiveReduction,
            detectStronglyConnectedComponents,
        }
    })();

    function renderMermaidDiagram(diagram) {
        const renderResult = globalThis.mermaid.run({nodes: [diagram]});
        if (typeof renderResult.catch === 'function') {
            renderResult.catch(error => {
                console.error('Mermaid rendering error:', error);
            });
        }
        return renderResult;
    }

    const render = (() => {
        const DEFAULT_MAX_TEXT_SIZE = 50000;
        const EXTENDED_MAX_TEXT_SIZE = 200000;
        const DEFAULT_MAX_EDGES = 500;

        // 描画済み SVG をコンテナ単位・ソース文字列キーでキャッシュする。
        // 同一ソースの再描画（チェック切替の往復・向き切替の戻しなど）で mermaid.run を回避する。
        const SVG_CACHE_LIMIT = 16;
        const svgCacheStore = new WeakMap(); // targetEl -> Map<source, svgHTML>

        function getSvgCache(targetEl) {
            let cache = svgCacheStore.get(targetEl);
            if (!cache) {
                cache = new Map();
                svgCacheStore.set(targetEl, cache);
            }
            return cache;
        }

        function putSvgCache(cache, source, svgHTML) {
            cache.set(source, svgHTML);
            while (cache.size > SVG_CACHE_LIMIT) {
                cache.delete(cache.keys().next().value); // 最古を破棄（挿入順）
            }
        }

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

            globalThis.mermaid.initialize({
                startOnLoad: false,
                securityLevel: "loose",
                maxTextSize: EXTENDED_MAX_TEXT_SIZE, // 初期のinitializeとの差分。initializeでやるの？
                maxEdges: DEFAULT_MAX_EDGES
            });

            diagram.classList.remove("too-large");
            diagram.innerHTML = source;

            const container = ensureMermaidDiagramContainer(diagram) || diagram;
            const renderResult = withRenderingIndicator(container, () => renderMermaidDiagram(diagram));
            if (renderResult && typeof renderResult.catch === "function") {
                renderResult.catch(() => flashButtonLabel(button, "描画に失敗しました"));
            }
        }

        function renderTooLargeDiagram(diagram, source, {messageText, onRender} = {}) {
            if (!diagram) return;
            diagram.classList.add("too-large");
            diagram.textContent = "";

            const container = document.createElement("div");
            container.className = "mermaid-too-large";

            const message = document.createElement("p");
            message.className = "mermaid-too-large__message";
            message.textContent = messageText ?? "図が大きいため表示を制限しています";
            container.appendChild(message);

            const actions = document.createElement("div");
            actions.className = "mermaid-too-large__actions";

            const renderButton = document.createElement("button");
            renderButton.type = "button";
            renderButton.textContent = "上限を上げて描画する";
            renderButton.addEventListener("click", () => {
                if (typeof onRender === "function") {
                    onRender(renderButton);
                } else {
                    renderWithExtendedLimit(diagram, source, renderButton);
                }
            });
            actions.appendChild(renderButton);

            const textButton = document.createElement("button");
            textButton.type = "button";
            textButton.textContent = "テキストで表示";
            textButton.addEventListener("click", () => {
                const pre = document.createElement("pre");
                pre.textContent = source;
                diagram.textContent = "";
                diagram.appendChild(pre);
            });
            actions.appendChild(textButton);

            container.appendChild(actions);
            diagram.appendChild(container);
        }

        function setRendering(container, isRendering) {
            if (!container || !container.classList) return;
            container.classList.toggle("is-rendering", isRendering);
        }

        /**
         * container の is-rendering を work() の実行中 true にし、成功・失敗・同期例外
         * いずれの場合も完了時に確実に false へ戻す。work() は同期値・undefined・Promise の
         * いずれを返してもよい。同期例外は is-rendering を戻したうえで呼び出し元へ再送出する。
         *
         * @param {Element} container
         * @param {function(): *} work
         * @param {function(): boolean} [isStale] - 完了時に true を返す場合、より新しい描画に
         *        置き換わっているとみなし is-rendering を外さない（世代管理との競合を避ける）
         */
        function withRenderingIndicator(container, work, isStale) {
            setRendering(container, true);
            const clear = () => {
                if (!isStale || !isStale()) setRendering(container, false);
            };
            let result;
            try {
                result = work();
            } catch (err) {
                clear();
                throw err;
            }
            if (result && typeof result.then === "function") {
                result.then(clear, clear);
            } else {
                clear();
            }
            return result;
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

        function openMermaidSvgInNewTab(container, button) {
            const svg = findRenderedMermaidSvg(container);
            if (!svg) {
                flashButtonLabel(button, "SVG未生成");
                return;
            }

            const serializer = new XMLSerializer();
            const svgText = serializer.serializeToString(svg);
            const blob = new Blob([svgText], {type: "image/svg+xml;charset=utf-8"});
            const url = URL.createObjectURL(blob);
            const newTab = window.open(url, "_blank");
            if (!newTab) {
                flashButtonLabel(button, "ポップアップがブロックされました");
            }
            // 新しいタブの読み込みには時間がかかるため、即時revokeせず遅延して解放する
            window.setTimeout(() => URL.revokeObjectURL(url), 60000);
        }

        function ensureZoomButton(container) {
            const button = ensureMermaidControlButton(container, "mermaid-zoom-button", "Open in New Tab", "⤢");
            if (!button) return null;
            button.onclick = () => openMermaidSvgInNewTab(container, button);
            return button;
        }

        function ensureDirectionButton(container, currentDirection, onUpdate) {
            if (!container || !currentDirection) return null;
            const button = ensureMermaidControlButton(container, "mermaid-direction-button", "Switch Direction", "⇄");
            if (!button) return null;
            button.onclick = () => {
                const newDirection = (currentDirection === "LR") ? "TB" : "LR";
                onUpdate(newDirection);
            };
            return button;
        }

        function ensureLabelToggleButton(container, showPhysicalName, onToggle) {
            if (!container) return null;
            const label = showPhysicalName ? "用語名を表示" : "物理名を表示";
            const button = ensureMermaidControlButton(container, "mermaid-label-toggle-button", label, "T");
            if (!button) return null;
            button.onclick = onToggle;
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
                maxEdges: maxEdges != null ? maxEdges : DEFAULT_MAX_EDGES,
                suppressErrorRendering: true
            };
        }

        // 「上限を上げて描画する」等のユーザー起因の再描画ボタンから使う。
        // isStale が与えられれば、完了時により新しい世代の描画に置き換わっていないか確認してから
        // is-rendering を外す（同一 container を共有する renderWithControls の描画と競合しうるため）。
        function renderMermaidNodeTracked(diagramEl, source, maxEdges, container, isStale) {
            return withRenderingIndicator(container, () => renderMermaidNode(diagramEl, source, maxEdges, container, isStale), isStale);
        }

        function renderMermaidNode(diagramEl, source, maxEdges, container, isStale) {
            if (!diagramEl || !globalThis.mermaid || typeof globalThis.mermaid.run !== "function") return;

            const text = source != null ? String(source) : "";
            diagramEl.removeAttribute("data-processed");
            diagramEl.style.display = "";
            diagramEl.classList.remove("too-large");
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
                const result = renderMermaidDiagram(diagramEl);
                if (result && typeof result.catch === "function") {
                    result.catch((err) => {
                        const message = err && err.message ? err.message : String(err);
                        if (message.includes("Edge limit exceeded")) {
                            const edgeCount = estimateEdgeCount(text);
                            const actionEdges = Math.max(edgeCount, DEFAULT_MAX_EDGES * 2);
                            renderTooLargeDiagram(diagramEl, text, {
                                messageText: `関連数が多いため表示を制限しています（エッジ数: ${edgeCount}）`,
                                onRender: () => renderMermaidNodeTracked(diagramEl, text, actionEdges, container, isStale)
                            });
                        } else {
                            diagramEl.style.display = "none";
                            setEdgeWarning(container, {visible: true, message: `Mermaid error: ${message}`});
                        }
                    });
                }
                return result;
            } catch (err) {
                const message = err && err.message ? err.message : String(err);
                diagramEl.style.display = "none";
                setEdgeWarning(container, {visible: true, message: `Mermaid error: ${message}`});
            }
        }

        function renderWithControls(targetEl, diagramFn, {direction, enableLabelToggle, showControls = true} = {}) {
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

            let showPhysicalName = false;
            // 描画は再入する（方向切替・ラベル切替・キャッシュ復元）。mermaid.run は非同期なので
            // 前の描画の解決コールバックが、後の描画で差し替わった diagramEl.innerHTML を
            // 旧ソースのキーで誤ってキャッシュしうる。世代カウンタで最新の描画以外は無視する。
            let renderGeneration = 0;

            const renderDiagram = (newDirection) => {
                const generation = ++renderGeneration;
                setRendering(container, true);
                const currentSource = diagramFn(newDirection, {showPhysicalName}) ?? "";

                if (showControls) {
                    ensureCopySourceButton(container, currentSource);
                    ensureDownloadButton(container);
                    ensureZoomButton(container);
                    if (/^\s*(?:graph|flowchart)\s/m.test(currentSource) || /^\s*classDiagram\b/m.test(currentSource)) {
                        ensureDirectionButton(container, newDirection, renderDiagram);
                    }
                    if (enableLabelToggle) {
                        ensureLabelToggleButton(container, showPhysicalName, () => {
                            showPhysicalName = !showPhysicalName;
                            renderDiagram(newDirection);
                        });
                    }
                }

                if (isTooLarge(currentSource)) {
                    diagramEl.style.display = "";
                    setEdgeWarning(container, {visible: false});
                    renderTooLargeDiagram(diagramEl, currentSource);
                    setRendering(container, false);
                    return;
                }

                const edgeCount = estimateEdgeCount(currentSource);
                if (edgeCount > DEFAULT_MAX_EDGES) {
                    setEdgeWarning(container, {visible: false});
                    renderTooLargeDiagram(diagramEl, currentSource, {
                        messageText: `関連数が多いため表示を制限しています（エッジ数: ${edgeCount}）`,
                        onRender: () => renderMermaidNodeTracked(diagramEl, currentSource, edgeCount, container, () => generation !== renderGeneration)
                    });
                    setRendering(container, false);
                    return;
                }

                // 同一ソースを以前描画済みなら mermaid.run を回避して SVG を復元する。
                // ただし click ディレクティブを含む図はキャッシュ復元後に Mermaid の
                // クリック・ツールチップイベントリスナーが失われるため、常に mermaid.run を通す。
                const svgCache = getSvgCache(targetEl);
                const cachedSvg = svgCache.get(currentSource);
                if (cachedSvg != null && !/^\s*click\s/m.test(currentSource)) {
                    diagramEl.style.display = "";
                    diagramEl.classList.remove("too-large");
                    setEdgeWarning(container, {visible: false});
                    diagramEl.innerHTML = cachedSvg;
                    diagramEl.setAttribute("data-processed", "true");
                    setRendering(container, false);
                    return;
                }

                const result = renderMermaidNode(diagramEl, currentSource, DEFAULT_MAX_EDGES, container, () => generation !== renderGeneration);
                const finishRendering = () => {
                    if (generation === renderGeneration) setRendering(container, false);
                };
                // 描画が成功した場合のみキャッシュする。失敗時にキャッシュすると、
                // 壊れた内容が data-processed="true" のまま保存され、次回以降その
                // ソースを警告なしに復元してしまう。
                const cacheRendered = () => {
                    finishRendering();
                    // 後続の描画が始まっていれば diagramEl.innerHTML は別ソースの SVG になっている。
                    // 旧ソースのキーで誤キャッシュしないよう、最新の描画でなければ何もしない。
                    if (generation !== renderGeneration) return;
                    if (diagramEl.getAttribute("data-processed") === "true") {
                        putSvgCache(svgCache, currentSource, diagramEl.innerHTML);
                    }
                };
                if (result && typeof result.then === "function") {
                    result.then(cacheRendered).catch(finishRendering);
                } else {
                    cacheRendered();
                }
            };

            let initialDirection = direction;
            if (!initialDirection) {
                const text = diagramFn("LR", {showPhysicalName: false});
                const graphMatch = text?.match(/^\s*(?:graph|flowchart)\s+(TB|TD|LR)\b/m);
                const classDiagMatch = text?.match(/^\s*direction\s+(TB|LR)\b/m);
                initialDirection = graphMatch?.[1] ?? classDiagMatch?.[1] ?? "LR";
            }

            renderDiagram(initialDirection);
        }

        /**
         * IntersectionObserverを使用して遅延レンダリングを行う
         */
        function setupLazyMermaidRender() {
            if (typeof window === "undefined" || !window.mermaid) return;

            // 一覧で図を表示しないドキュメントでは不要
            if (document.body.classList.contains("package-relation")) return;

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

                const diagramContainer = ensureMermaidDiagramContainer(diagram) || diagram;
                diagram.innerHTML = source;
                const handleFinish = () => {
                    rendered.add(diagram);
                    queued.delete(diagram);
                    isRendering = false;
                    processRenderQueue();
                };
                // mermaid.run が同期的に例外を投げても is-rendering が残留したままキューが
                // 永久に止まらないよう、例外時も handleFinish でキューを進める。
                let renderResult;
                try {
                    renderResult = withRenderingIndicator(diagramContainer, () => renderMermaidDiagram(diagram));
                } catch (err) {
                    console.error('Mermaid rendering error:', err);
                    handleFinish();
                    return;
                }
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

        function initializeMermaid() {
            if (typeof window !== "undefined" && window === globalThis && globalThis.mermaid) {
                globalThis.mermaid.initialize({
                    startOnLoad: false,
                    securityLevel: "loose",
                    maxTextSize: DEFAULT_MAX_TEXT_SIZE,
                    maxEdges: DEFAULT_MAX_EDGES
                });
            }
        }

        return {
            isTooLarge,
            estimateEdgeCount,
            flashButtonLabel,
            renderTooLargeDiagram,
            renderWithControls,
            setupLazyMermaidRender,
            initializeMermaid,
            setRendering
        }
    })();

    /**
     * ダイアグラム管理（設定変更時の再レンダリング対応）
     */
    const diagram = (() => {
        const diagramRegistry = []; // [{container, renderFn}]
        const renderedContainers = new Set();
        const observerMap = new Map(); // コンテナごとに独立した observer

        function isVisible(element) {
            if (typeof element.getBoundingClientRect !== 'function') {
                return true; // テスト環境など、getBoundingClientRect が使えない場合は表示中と判断
            }
            const rect = element.getBoundingClientRect();
            return rect.top < window.innerHeight && rect.bottom > 0;
        }

        function fixHashScrollAfterRender(renderedContainer) {
            if (typeof window === 'undefined' || !window.location.hash) return;
            const target = document.querySelector(window.location.hash);
            if (!target) return;

            // ターゲットより上のコンテナのみ補正対象（compareDocumentPosition で文書順を判定）
            if (!(renderedContainer.compareDocumentPosition(target) & Node.DOCUMENT_POSITION_FOLLOWING)) return;

            // renderFn() 後に同期生成済みの .mermaid 要素を直接監視し、SVG挿入（mermaid非同期完了）を待つ
            const mermaidEl = renderedContainer.querySelector('.mermaid');
            if (!mermaidEl) return;

            const obs = new MutationObserver(() => {
                obs.disconnect();
                requestAnimationFrame(() => {
                    const scrollMarginTop = parseFloat(getComputedStyle(target).scrollMarginTop) || 0;
                    const rect = target.getBoundingClientRect();
                    // scroll-margin-top 分だけ上に余白が生じる想定位置より大幅に下なら補正
                    if (rect.top > scrollMarginTop + 60) {
                        target.scrollIntoView({block: 'start'});
                    }
                });
            });
            obs.observe(mermaidEl, {childList: true});
            setTimeout(() => obs.disconnect(), 3000); // mermaid が完了しなかった場合の安全タイムアウト
        }

        /**
         * ダイアグラムを登録（遅延レンダリング対応）
         * @param {HTMLElement} container
         * @param {Function} renderFn - レンダリング関数
         */
        function register(container, renderFn) {
            if (!container || typeof renderFn !== 'function') return;

            diagramRegistry.push({container, renderFn});
            Jig.mermaid.render.setRendering(container, true);

            // IntersectionObserver で自動レンダリング（各コンテナごとに独立した observer）
            if ('IntersectionObserver' in window) {
                const observer = new IntersectionObserver((entries) => {
                    entries.forEach(entry => {
                        if (entry.isIntersecting && !renderedContainers.has(entry.target)) {
                            renderedContainers.add(entry.target);
                            const d = diagramRegistry.find(d => d.container === entry.target);
                            if (d) {
                                d.renderFn();
                                fixHashScrollAfterRender(entry.target);
                            }
                            observer.unobserve(entry.target); // 一度だけレンダリング
                        }
                    });
                }, {rootMargin: '100px'});

                observer.observe(container);
                observerMap.set(container, observer);
            } else {
                // IntersectionObserver 非サポート時は即座にレンダリング
                renderedContainers.add(container);
                renderFn();
            }
        }

        /**
         * 表示範囲内のダイアグラムのみ再レンダリング
         * @param {Function} [shouldRerender] - 再レンダリング判定関数（省略時は全て）
         */
        function rerenderVisible(shouldRerender) {
            diagramRegistry
                .filter(({container}) => renderedContainers.has(container))
                .forEach(({container, renderFn}) => {
                    // 表示範囲内のみ再レンダリング
                    if (isVisible(container)) {
                        if (!shouldRerender || shouldRerender(container)) {
                            renderFn();
                        }
                    } else {
                        // 表示範囲外は削除のみで、スクロール時に自動再レンダリング
                        container.innerHTML = "";
                        Jig.mermaid.render.setRendering(container, true);
                        renderedContainers.delete(container);
                    }
                });
        }

        /**
         * ダイアグラムコンテナを作成して登録するヘルパー関数
         * コンテナの作成・追加・登録をまとめて処理
         *
         * renderFn は以下の2つのパターンをサポート：
         * 1. container パラメータを受け取り、自分で renderWithControls を呼ぶ
         * 2. mermaid定義またはコードを返す - この関数が renderWithControls を呼ぶ
         *
         * @param {HTMLElement|Array} parentContainer - 親要素（HTMLElement）またはコンテナ追加先（Array）
         * @param {Function} renderFn - (container?: HTMLElement) => mermaidDef|undefined
         * @param {Object} [options={}] - オプション
         * @param {string} [options.className="mermaid-diagram"] - コンテナのクラス名
         * @returns {HTMLElement} 作成されたコンテナ
         */
        function createAndRegister(parentContainer, renderFn, options = {}) {
            const {className = "mermaid-diagram"} = options;
            const container = Jig.dom.createElement("div", {className});

            // parentContainer が配列の場合は push、そうでなければ appendChild
            if (Array.isArray(parentContainer)) {
                parentContainer.push(container);
            } else {
                parentContainer.appendChild(container);
            }

            register(container, () => {
                const result = renderFn(container);
                // renderFn が mermaid定義を返した場合、自動でレンダリング
                if (result) {
                    container.innerHTML = "";
                    const diagramFn = typeof result === "function" ? result : () => result;
                    Jig.mermaid.render.renderWithControls(container, diagramFn);
                }
                // renderFn が void/undefined を返した場合は、renderFn 内で既に renderWithControls を呼んでいると仮定
            });

            return container;
        }

        return {
            register,
            rerenderVisible,
            createAndRegister,
        };
    })();

    /**
     * Markdown 内の ```mermaid コードブロックをダイアグラムに変換する。
     * marked は ```mermaid を <pre><code class="language-mermaid"> に変換するので、
     * それを描画コンテナへ差し替えて遅延レンダリングに登録する。
     *
     * @param {HTMLElement} root - createMarkdownElement が生成した要素など、変換対象の親要素
     */
    function renderMarkdownDiagrams(root) {
        if (!root || typeof root.querySelectorAll !== "function") return;
        root.querySelectorAll("pre > code.language-mermaid").forEach(code => {
            const source = code.textContent || "";
            const container = Jig.dom.createElement("div", {className: "mermaid-diagram"});
            code.parentElement.replaceWith(container);
            diagram.register(container, () => {
                container.innerHTML = "";
                render.renderWithControls(container, () => source, {showControls: false});
            });
        });
    }

    /**
     * パッケージダイアグラム作成
     * index.htmlおよびdomain.htmlで表示するもの。
     *
     * @param {Package} pkg - 対象パッケージ
     * @param {Package[]} allPackages - 全パッケージの一覧
     * @param {Relation[]} allPackageRelations - パッケージ間の全関連
     * @param {CreatePackageLevelDiagramOptions} options
     * @returns {string|null}
     */
    function createPackageLevelDiagram(pkg, allPackages, allPackageRelations, options) {
        const {transitiveReductionEnabled, diagramDirection, nodeClickUrlCallback, focusedPackageFqn, showPhysicalName} = options;
        const {uniqueRelations, packageFqns} = builder.buildVisibleDiagramRelations(
            allPackages,
            allPackageRelations,
            [],
            {
                packageFilterFqn: [pkg.fqn],
                aggregationDepth: pkg.fqn.split('.').length + 1, // 自身の一つ下でグルーピング
                transitiveReductionEnabled: transitiveReductionEnabled
            }
        );
        // パッケージが1つしかなければ表示しない。関連が0でもパッケージが複数あればノードのみで表示する
        if (packageFqns.size <= 1) return null;

        const {source} = builder.buildMermaidDiagramSource(
            packageFqns, uniqueRelations,
            {diagramDirection, nodeClickUrlCallback, focusedPackageFqn, showPhysicalName}
        );
        return source;
    }

    const CLASS_DIAGRAM_ARROW_MAP = {association: '-->', realization: '..|>', dependency: '..>'};
    function edgeWithArrow (edge) {
        // 継承は矢印を逆転する
        if (edge.edgeType === 'inheritance') {
            return `${edge.to} <|-- ${edge.from}`;
        }
        const arrow = CLASS_DIAGRAM_ARROW_MAP[edge.edgeType] ?? '..>';
        if (edge.multiplicity) {
            return `${edge.from} ${arrow} "${edge.multiplicity}" ${edge.to}`;
        }
        return `${edge.from} ${arrow} ${edge.to}`;
    }

    // classDiagram ビルダー
    class ClassDiagramBuilder {
        constructor() {
            this._classes = new Map(); // id -> {label, members: string[]}
            this._edges = [];
            this._edgeSet = new Set();
            this._clicks = [];
        }

        _escape(text) {
            return (text || "").replace(/"/g, '\\"');
        }

        addClass(id, label) {
            if (!this._classes.has(id)) {
                this._classes.set(id, {label: this._escape(label), members: []});
            }
            return id;
        }

        addField(classId, typeName, fieldName) {
            const cls = this._classes.get(classId);
            if (cls) cls.members.push(`    ${typeName} ${fieldName}`);
        }

        addMethod(classId, visibility, methodName, params, returnType, isStatic = false) {
            const cls = this._classes.get(classId);
            if (!cls) return;
            const visChar = visibility === 'PUBLIC' ? '+' : visibility === 'PROTECTED' ? '#' : visibility === 'PRIVATE' ? '-' : '~';
            const staticMark = isStatic ? '$' : '';
            const ret = returnType ? ` ${returnType}` : '';
            cls.members.push(`    ${visChar}${methodName}(${params.join(', ')})${staticMark}${ret}`);
        }

        addEdge(from, to, edgeType = 'dependency', multiplicity = '') {
            const key = `${from}::${to}`;
            if (!this._edgeSet.has(key)) {
                this._edgeSet.add(key);
                this._edges.push({from, to, edgeType, multiplicity});
            }
        }

        addClick(id, url, tooltip) {
            if (!id || !url) return;
            const tooltipPart = tooltip ? ` "${this._escape(tooltip)}"` : ' _self';
            this._clicks.push(`  click ${id} href "${url}"${tooltipPart}`);
        }

        build(direction = 'LR') {
            const safeDirection = direction === 'TB' ? 'TB' : 'LR';
            const lines = [`classDiagram`, `direction ${safeDirection}`];
            this._classes.forEach((cls, id) => {
                if (cls.members.length > 0) {
                    lines.push(`  class ${id}["${cls.label}"] {`);
                    cls.members.forEach(m => lines.push(m));
                    lines.push(`  }`);
                } else {
                    lines.push(`  class ${id}["${cls.label}"]`);
                }
            });
            this._edges.forEach(e => lines.push(`  ${edgeWithArrow(e)}`));
            this._clicks.forEach(c => lines.push(c));
            return lines.join('\n');
        }
    }

    // クロスページナビゲーションURLの生成
    const navUrl = (page, idPrefix) => (fqn) => `./${page}.html#${Jig.util.fqnToId(idPrefix, fqn)}`;
    const nav = {
        usecaseMethodUrl:  navUrl("usecase", "method"),
        inboundAdapterUrl: navUrl("inbound", "adapter"),
        outboundPortUrl:   navUrl("outbound", "port"),
        domainTypeUrl:     navUrl("domain",   "domain"),
    };

    return {
        builder,
        graph,
        render,
        diagram,
        renderMarkdownDiagrams,
        // 高レベルAPI
        createPackageLevelDiagram,
        Builder: builder.MermaidBuilder,
        createBuilder: builder.createBuilder,
        ClassDiagramBuilder,
        nav,
    };
})();

if (typeof window !== "undefined") {
    window._jigNoop = () => {};
}

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", function () {
        globalThis.Jig.mermaid.render.initializeMermaid();
        globalThis.Jig.mermaid.render.setupLazyMermaidRender();
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = globalThis.Jig.mermaid;
}

// Source: jig-i18n.js
globalThis.Jig ??= {};

globalThis.Jig.i18n = (() => {

    // [data-i18n] マーカー付き要素の翻訳辞書（日本語キー→対象言語値）。
    // JigDocument のラベル（用語集 等）も UI 文言と区別せずここに集約する。
    const builtinDictionaries = {
        en: {
            // JigDocument ラベル（Java 側 JigDocument enum のキーと対応）
            "用語集": "Glossary",
            "パッケージ関連": "Package relations",
            "ユースケース": "Usecase",
            "入力インタフェース": "Inbound interface",
            "インサイト": "Insight",
            "一覧出力": "List output",
            "ライブラリ依存情報": "Library dependencies",
            // UI 文言
            "入力": "Input",
            "出力": "Output",
            "フィールド": "Fields",
            "折りたたむ": "Collapse",
            "展開": "Expand",
            "全て折りたたむ": "Collapse all",
            "全て展開": "Expand all",
            "パスで絞り込み": "Filter by path",
            "購読先で絞り込み": "Filter by subscription",
            "スケジュールで絞り込み": "Filter by schedule",
            "出力日時": "Generated at",
            "主要パッケージ関連図": "Key package diagram",
            "ドメインパッケージ": "Domain package",
            "最上位パッケージ": "Top-level package",
            // sidebar 表示設定
            "表示設定": "Display settings",
            "表示対象": "Show",
            "表示要素": "Elements",
            "表示内容": "Content",
            "表示項目": "Items",
            "表示種別": "Type",
            "種類": "Kind",
            "ダイアグラム": "Diagram",
            "ダイアグラム表示": "Show diagram",
            "Deprecated ノード": "Deprecated nodes",
            "依存関係の簡略表示": "Simplify dependencies",
            "個別ダイアグラムの表示対象": "Per-diagram elements",
            "簡略表示": "Simplified view",
            "すべて": "All",
            "パッケージ": "Package",
            "文字列": "String",
            "数値": "Number",
            "日付": "Date",
            "期間": "Period",
            "区分": "Category",
            "コレクション": "Collection",
            "説明": "Description",
            "メンバ": "Members",
            "ハンドラのみ": "Handlers only",
            "入力・出力": "Input/Output",
            "内部メソッド": "Internal methods",
            "出力インタフェース": "Outbound interface",
            "ドメインモデル": "Domain model",
            "ドメインのみ": "Domain only",
            "説明のない用語": "Terms without description",
            "永続化操作方法": "Persistence operations",
            "呼び出しユースケース": "Caller usecase",
            "ポート": "Port",
            "メソッド": "Method",
            "アダプタ": "Adapter",
            "外部アクセサ": "External accessor",
            "外部操作対象": "External target",
            "操作方法": "Operation",
            "リクエストハンドラ": "Request handler",
            "メッセージリスナー": "Message listener",
            "スケジューラー": "Scheduler",
            "その他": "Other",
            // glossary 追加項目
            "クラス": "Class",
            "属性情報を表示する": "Show attributes",
            "CSV出力": "Export CSV",
            "絞り込み": "Filter",
            // タブ見出し
            "パッケージ関連図": "Package relation diagram",
            "パッケージ内パッケージ関連図": "Inner package relation diagram",
            "パッケージ内クラス関連図": "Inner class relation diagram",
            "クラス関連図": "Class relation diagram",
            "クラス図": "Class diagram",
            "概要": "Overview",
            "テキスト": "Text",
            "シミュレーション": "Simulation",
            "ユースケース図": "Usecase diagram",
            "シーケンス図": "Sequence diagram",
            "staticメソッド": "Static methods",
            // テーブルヘッダ
            "名前": "Name",
            "名称": "Name",
            "完全修飾名": "FQN",
            "クラス数": "Classes",
            "メソッド数": "Methods",
            "使用クラス数": "Used classes",
            "使用クラス数(Ce)": "Used classes (Ce)",
            "被使用クラス数(Ca)": "Used by (Ca)",
            "不安定性(I)": "Instability (I)",
            "凝集度欠如(LCOM)": "LCOM",
            "循環的複雑度": "Cyclomatic complexity",
            "循環的複雑度合計": "Total cyclomatic complexity",
            "規模": "Size",
            "規模合計": "Total size",
            "使用メソッド数": "Used methods",
            "使用フィールド数": "Used fields",
            "自クラスフィールド使用数": "Own field uses",
            "自クラスメソッド呼び出し数": "Own method calls",
            "関連数（依存元）": "Dependents (in)",
            "関連数（依存先）": "Dependencies (out)",
            "定義名": "Declaration",
            "ライブラリ": "Library",
            "含まれるパッケージ": "Packages",
            "呼び出し元クラス": "Caller classes",
            "列挙定数名": "Enum constant",
            "パス": "Path",
            "エントリーポイント": "Entry point",
            "ハンドラ": "Handler",
            "購読先": "Subscription",
            "スケジュール": "Schedule",
            "出力ポート / 操作": "Outbound port / operation",
            "列挙値": "Enum values",
            // glossary attribute meta
            "属性情報": "Attributes",
            "単純名": "Simple name",
            "由来": "Origin",
            "関連ドキュメント": "Related document",
            "ソースコード": "Source code",
            "ソースを開く": "Open source",
            // library-dependency controls
            "階層集約:": "Aggregation level:",
            "Java 標準（java.*, javax.*）を表示": "Show Java standard (java.*, javax.*)",
            "ライブラリの選択を解除": "Clear library selection",
            "ライブラリ一覧": "Library list",
            // package controls
            "階層探索": "Hierarchy",
            "関連探索": "Explore",
            "依存関係の簡略表示:": "Simplify dependencies:",
            "有効": "Enabled",
            "Deprecatedのみの関連を除外:": "Exclude deprecated-only relations:",
            "選択を全解除": "Clear all selections",
            "選択をデフォルトに戻す": "Reset to defaults",
            "依存の表示": "Show dependencies",
            "依存元:": "Callers:",
            "依存先:": "Callees:",
            "なし": "None",
            "直接": "Direct",
            "相互依存分析": "Mutual dependency analysis",
            "表示する関連": "Relations to show",
            "関連": "Relations",
            // 他のサイドバー section タイトル
            "永続化操作対象": "Persistence targets",
            "外部型": "External types",
            "エントリーポイント一覧": "Entry points",
            "入出力オブジェクト一覧": "I/O objects",
        },
    };

    // セッション中のみ保持する現在言語（永続化しない）。
    let currentLang = null;

    // BCP47 タグ（"ja-JP" など）から先頭の言語コード（"ja"）だけを取り出す。
    function toLangCode(tag) {
        return String(tag || "").split('-')[0];
    }

    function resolveDictionary(lang) {
        return builtinDictionaries[lang] || null;
    }

    function resolveLanguage() {
        if (currentLang) return currentLang;
        // 初期言語は HTML の lang 属性から決定する（Java 側で {{lang}} を全テンプレートに置換済み）。
        return toLangCode(document.documentElement.lang || "ja");
    }

    // サポート言語は ja（カノニカルキー）と builtinDictionaries のキー集合から導出する。
    function availableLanguages() {
        return ["ja", ...Object.keys(builtinDictionaries)];
    }

    function originalText(el) {
        if (el.dataset.i18nOriginal == null) {
            el.dataset.i18nOriginal = el.textContent.trim();
        }
        return el.dataset.i18nOriginal;
    }

    // 子に要素ノードを含む場合は、直下のテキストノードのみ置換して子要素を保持する。
    // 例: <label><input> ラベル</label> でも <input> を壊さず " ラベル" のみ差し替える。
    function setTranslatedText(el, value) {
        let textNode = null;
        let hasChildElements = false;
        for (const node of el.childNodes) {
            if (node.nodeType === 1) hasChildElements = true;
            else if (node.nodeType === 3 && node.nodeValue.trim() && !textNode) textNode = node;
        }
        if (!hasChildElements) {
            el.textContent = value;
            return;
        }
        if (textNode) {
            const match = textNode.nodeValue.match(/^(\s*).*?(\s*)$/s);
            textNode.nodeValue = match[1] + value + match[2];
        } else {
            el.appendChild(document.createTextNode(value));
        }
    }

    function resolveKey(el) {
        const explicitKey = el.getAttribute("data-i18n");
        return (explicitKey && explicitKey.length > 0) ? explicitKey : originalText(el);
    }

    // dict が falsy なら原文（ja キー）に復元する。
    function translate(root, dict) {
        if (!root || typeof root.querySelectorAll !== "function") return;
        root.querySelectorAll("[data-i18n]").forEach(el => {
            const key = resolveKey(el);
            setTranslatedText(el, (dict && dict[key]) || key);
        });
        // 属性翻訳: data-i18n-attr="属性名" を持つ要素の対応属性を翻訳する
        // （現在の属性値を ja キーとして使う）。
        root.querySelectorAll("[data-i18n-attr]").forEach(el => translateAttribute(el, dict));
    }

    function translateAttribute(el, dict) {
        const attrName = el.getAttribute("data-i18n-attr");
        if (!attrName) return;
        const datasetKey = `i18nAttrOrig_${attrName}`;
        if (el.dataset[datasetKey] == null) {
            el.dataset[datasetKey] = el.getAttribute(attrName) || "";
        }
        const key = el.dataset[datasetKey];
        el.setAttribute(attrName, (dict && dict[key]) || key);
    }

    function t(key) {
        const lang = resolveLanguage();
        if (lang === 'ja') return key;
        const dict = resolveDictionary(lang);
        return (dict && dict[key]) || key;
    }

    function apply() {
        const lang = resolveLanguage();
        document.documentElement.lang = lang;
        const dict = lang === "ja" ? null : resolveDictionary(lang);
        // <title data-i18n> も document 全体のクエリで一緒に処理される
        translate(document, dict);
    }

    function currentLanguage() {
        return resolveLanguage();
    }

    function setLanguage(lang) {
        currentLang = toLangCode(lang);
        apply();
        document.dispatchEvent(new CustomEvent("jig:locale-change", {detail: {lang: currentLang}}));
    }

    return {
        apply,
        currentLanguage,
        setLanguage,
        availableLanguages,
        t,
        // テストが明示キー翻訳をセットアップするために参照する
        builtinDictionaries,
    };
})();

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", () => {
        globalThis.Jig.i18n.apply();
        // 動的に挿入された data-i18n / data-i18n-attr 要素も翻訳する。
        // 要素ノード追加に限定することで apply 自身が起こす text node 差し替えのループを避ける。
        if (typeof MutationObserver !== "undefined") {
            let scheduled = false;
            const observer = new MutationObserver(mutations => {
                const elementAdded = mutations.some(m =>
                    Array.from(m.addedNodes).some(n => n.nodeType === 1));
                if (!elementAdded || scheduled) return;
                scheduled = true;
                queueMicrotask(() => {
                    scheduled = false;
                    globalThis.Jig.i18n.apply();
                });
            });
            observer.observe(document.body, {childList: true, subtree: true});
        }
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = globalThis.Jig.i18n;
}

// Source: jig-bootstrap.js
globalThis.Jig ??= {};

globalThis.Jig.bootstrap = (() => {

    function register(bodyClass, initFn) {
        if (typeof document === "undefined") return;
        document.addEventListener("DOMContentLoaded", () => {
            if (document.body.classList.contains(bodyClass)) initFn();
        });
    }

    return {register};
})();

if (typeof module !== "undefined" && module.exports) {
    module.exports = globalThis.Jig.bootstrap;
}

