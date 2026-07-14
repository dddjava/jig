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

    /**
     * buildPackageTree のツリーをDFS前順で平坦化する。
     * itemsを持つノードは常に含み、itemsのないノードは includeEmptyNode(fqn) が真の場合のみ含む。
     *
     * @template T
     * @param {T[]} items
     * @param {function(T): string} getFqn - アイテムの型FQNを返す関数
     * @param {function(string): boolean} [includeEmptyNode] - itemsのない中間パッケージを含めるかの述語
     * @returns {{fqn: string, items: T[]}[]} パッケージFQNとアイテム配列の配列（fqn昇順・親が子孫より先）
     */
    function flattenPackageTree(items, getFqn, includeEmptyNode = () => false) {
        const result = [];
        const visit = (node) => {
            if (node.items.length > 0 || includeEmptyNode(node.fqn)) {
                result.push({fqn: node.fqn, items: node.items});
            }
            node.children.forEach(visit);
        };
        buildPackageTree(items, getFqn).forEach(visit);
        return result;
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
        buildPackageTree,
        flattenPackageTree,
    }
})();

if (typeof module !== "undefined" && module.exports) {
    module.exports = globalThis.Jig.util;
}
