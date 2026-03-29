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
     * @typedef {Object} Relation
     * @property {string} from - 依存元のFQN
     * @property {string} to - 依存先のFQN
     */
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
     * @typedef {Object} Package
     * @property {string} fqn - パッケージの完全修飾名
     */
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
        const relationSet = new Set(uniqueRelations.map(relation => `${relation.from}::${relation.to}`));
        
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
     * パッケージカード用のパッケージ関連図ソースを生成する（domain.js での使用を想定）
     *
     * @typedef {Object} CreatePackageLevelDiagramOptions
     * @property {boolean} [transitiveReductionEnabled] - 推移的縮約を行うかどうか
     * @property {string} diagramDirection - 図の向き ('TB' または 'LR')
     */
    /**
     * @param {Package} pkg - 対象パッケージ
     * @param {Package[]} allPackages - 全パッケージの一覧
     * @param {Relation[]} allPackageRelations - パッケージ間の全関連
     * @param {CreatePackageLevelDiagramOptions} options
     * @returns {string|null}
     */
    function createPackageLevelDiagram(pkg, allPackages, allPackageRelations, options) {
        const {transitiveReductionEnabled, diagramDirection} = options;
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
        if (packageFqns.size <= 1 && uniqueRelations.length === 0) return null;

        const { source } = buildMermaidDiagramSource(
            packageFqns, uniqueRelations,
            { diagramDirection }
        );
        return source;
    }

    /**
     * @typedef {Object} MermaidDiagramSourceOptions
     * @property {string} diagramDirection - 図の向き ('TD' または 'LR')
     * @property {string|null} [focusedPackageFqn] - フォーカスされたパッケージ
     * @property {string|null} [clickHandlerName] - クリックハンドラ関数名
     */
    /**
     * @param {Set<string>} packageFqns
     * @param {Relation[]} uniqueRelations
     * @param {MermaidDiagramSourceOptions} options
     */
    function buildMermaidDiagramSource(packageFqns, uniqueRelations, options) {
        const {diagramDirection, focusedPackageFqn, clickHandlerName} = options;
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
        const {edgeLines, linkStyles, mutualPairs} = buildDiagramEdgeLines(uniqueRelations, ensureNodeId);
        
        const nodeLines = buildDiagramNodeLines(
            packageFqnsToDisplay,
            nodeIdByFqn,
            {
                nodeIdToFqn,
                nodeLabelById,
                escapeMermaidText,
                clickHandlerName,
                parentFqnsWithRelations
            }
        );

        nodeLines.forEach(line => lines.push(line));
        edgeLines.forEach(line => lines.push(line));
        linkStyles.forEach(styleLine => lines.push(styleLine));

        // ノードのスタイルを指定。どちらも存在しない場合もあるが、classDefに害はないので出力する。
        // ルートパッケージの色はサブグラフに合わせて少し濃くし、境界線を破線にする
        lines.push('classDef parentPackage fill:#ffffce,stroke:#aaaa00,stroke-dasharray:10 3');
        if (focusedPackageFqn) {
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

    function buildDiagramEdgeLines(uniqueRelations, ensureNodeId) {
        const mutualPairs = buildMutualDependencyPairs(uniqueRelations);
        const linkStyles = [];
        let linkIndex = 0;
        const edgeLines = [];
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
                edgeLines.push(`${fromId} <--> ${toId}`);
                linkStyles.push(`linkStyle ${linkIndex} stroke:red,stroke-width:2px`);
                linkIndex += 1;
                return;
            }
            edgeLines.push(`${fromId} --> ${toId}`);
            linkIndex += 1;
        });
        return {edgeLines, linkStyles, mutualPairs};
    }

    /**
     * @typedef {Object} DiagramNodeLinesOptions
     * @property {Map<string, string>} nodeIdToFqn
     * @property {Map<string, string>} nodeLabelById
     * @property {Function} escapeMermaidText
     * @property {string|null} [clickHandlerName]
     * @property {Set<string>} parentFqnsWithRelations
     */
    function buildDiagramNodeLines(packageFqns, nodeIdByFqn, options) {
        const {nodeIdToFqn, nodeLabelById, escapeMermaidText, clickHandlerName, parentFqnsWithRelations} = options;
        
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
            if (fqn && parentFqns.has(fqn)) {
                lines.push(`class ${nodeId} parentPackage`);
            }
        };
        return buildSubgraphLines(rootGroup, addNodeLines, escapeMermaidText);
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

    function buildSubgraphLines(rootGroup, addNodeLines, escapeMermaidText) {
        const lines = [];
        let groupIndex = 0;
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
};

// Test-only exports for Node; no-op in browsers.
// テスト用エクスポート
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        ...PackageDiagramModule,
        createPackageLevelDiagram: PackageDiagramModule.createPackageLevelDiagram,
    };
}
