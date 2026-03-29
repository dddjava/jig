// パッケージ関連図生成の共通モジュール
// package.js と domain.js の両方で使用する純粋関数を提供する

// FQN ユーティリティ
function getAggregatedFqn(fqn, depth) {
    if (!depth || depth <= 0) return fqn;
    if (!fqn || fqn === '(default)') return fqn;
    const parts = fqn.split('.');
    if (parts.length <= depth) return fqn;
    return parts.slice(0, depth).join('.');
}

function getCommonPrefixDepth(fqns) {
    if (!fqns?.length) return 0;
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

// パッケージフィルタを適用して表示対象の関連とパッケージセットを構築
function buildVisibleDiagramRelations(packages, relations, causeRelationEvidence, packageFilterFqn, aggregationDepth, transitiveReductionEnabled) {
    const visiblePackages = packageFilterFqn.length > 0
        ? packages.filter(item => isWithinPackageFilters(item.fqn, packageFilterFqn))
        : packages;
    const visibleSet = new Set(visiblePackages.map(item => getAggregatedFqn(item.fqn, aggregationDepth)));
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
    const visibleRelations = filteredRelations
        .map(relation => ({
            from: getAggregatedFqn(relation.from, aggregationDepth),
            to: getAggregatedFqn(relation.to, aggregationDepth),
        }))
        .filter(relation => relation.from !== relation.to);
    const uniqueRelationMap = new Map();
    visibleRelations.forEach(relation => {
        uniqueRelationMap.set(`${relation.from}::${relation.to}`, relation);
    });
    let uniqueRelations = Array.from(uniqueRelationMap.values());

    if (transitiveReductionEnabled) {
        uniqueRelations = globalThis.Jig.graph.transitiveReduction(uniqueRelations);
    }

    return {uniqueRelations, visibleSet, filteredCauseRelationEvidence};
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

function buildParentFqns(visibleSet) {
    const parentFqns = new Set();
    Array.from(visibleSet).sort().forEach(fqn => {
        const parts = fqn.split('.');
        for (let i = 1; i < parts.length; i += 1) {
            const prefix = parts.slice(0, i).join('.');
            if (visibleSet.has(prefix)) parentFqns.add(prefix);
        }
    });
    return parentFqns;
}

/**
 * @param {Set<string>} visibleSet
 * @param {Array<{from: string, to: string}>} uniqueRelations
 * @param {Map<string, string>} nameByFqn
 * @param {string} diagramDirection
 * @param {string|null} focusedPackageFqn
 * @param {{clickHandlerName?: string|null}} options
 */
function buildMermaidDiagramSource(visibleSet, uniqueRelations, nameByFqn, diagramDirection, focusedPackageFqn, options = {}) {
    const escapeMermaidText = text => text.replace(/"/g, '\\"');
    const lines = [
        "---",
        "config:",
        "  theme: 'default'",
        "  themeVariables:",
        "    clusterBkg: '#ffffde'", // デフォルトと同じ色だがルートノードの色と合わせるために明示
        "---",
        `graph ${diagramDirection}`];
    const {nodeIdByFqn, nodeIdToFqn, nodeLabelById, ensureNodeId} = buildDiagramNodeMaps(visibleSet, nameByFqn);
    const {edgeLines, linkStyles, mutualPairs} = buildDiagramEdgeLines(uniqueRelations, ensureNodeId);
    const nodeLines = buildDiagramNodeLines(
        visibleSet,
        nodeIdByFqn,
        nodeIdToFqn,
        nodeLabelById,
        escapeMermaidText,
        focusedPackageFqn,
        options.clickHandlerName ?? null
    );

    nodeLines.forEach(line => lines.push(line));
    edgeLines.forEach(line => lines.push(line));
    linkStyles.forEach(styleLine => lines.push(styleLine));

    // ノードのスタイルを指定。どちらも存在しない場合もあるが、classDefに害はないので出力する。
    // ルートパッケージの色はサブグラフに合わせて少し濃くし、境界線を破線にする
    lines.push('classDef parentPackage fill:#ffffce,stroke:#aaaa00,stroke-dasharray:10 3');
    // 選択されたものを強調表示する
    lines.push('classDef focused-package-highlight stroke-width:3px,font-weight:bold');

    return {source: lines.join('\n'), nodeIdToFqn, mutualPairs};
}

function buildDiagramNodeMaps(visibleSet, nameByFqn) {
    const nodeIdByFqn = new Map();
    const nodeIdToFqn = new Map();
    const nodeLabelById = new Map();
    let nodeIndex = 0;
    const ensureNodeId = fqn => {
        if (nodeIdByFqn.has(fqn)) return nodeIdByFqn.get(fqn);
        const nodeId = `P${nodeIndex++}`;
        nodeIdByFqn.set(fqn, nodeId);
        nodeIdToFqn.set(nodeId, fqn);
        const label = nameByFqn.get(fqn) || fqn;
        nodeLabelById.set(nodeId, label);
        return nodeId;
    };
    Array.from(visibleSet).sort().forEach(ensureNodeId);
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

function buildDiagramNodeLines(visibleSet, nodeIdByFqn, nodeIdToFqn, nodeLabelById, escapeMermaidText, focusedPackageFqn, clickHandlerName) {
    const visibleFqns = Array.from(visibleSet).sort();
    const parentFqns = buildParentFqns(visibleSet);
    const rootGroup = buildDiagramGroupTree(visibleFqns, nodeIdByFqn);
    const addNodeLines = (lines, nodeId, parentSubgraphFqn) => {
        const fqn = nodeIdToFqn.get(nodeId);
        const displayLabel = buildDiagramNodeLabel(nodeLabelById.get(nodeId), fqn, parentSubgraphFqn);
        let nodeDefinition = globalThis.Jig.mermaid.getNodeDefinition(nodeId, displayLabel, 'package');
        if (fqn === focusedPackageFqn) {
            nodeDefinition += ':::focused-package-highlight';
        }
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

function buildDiagramGroupTree(visibleFqns, nodeIdByFqn) {
    const prefixDepth = getCommonPrefixDepth(visibleFqns);
    const baseDepth = Math.max(prefixDepth - 1, 0);
    const createGroupNode = key => ({key, children: new Map(), nodes: []});
    const rootGroup = createGroupNode('');
    visibleFqns.forEach(fqn => {
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

// グローバル公開
globalThis.Jig = globalThis.Jig ?? {};
globalThis.Jig.packageDiagram = {
    getAggregatedFqn,
    getPackageFqnFromTypeFqn,
    isWithinPackageFilters,
    buildVisibleDiagramRelations,
    buildMermaidDiagramSource,
};

// Test-only exports for Node; no-op in browsers.
// テスト用エクスポート
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        getAggregatedFqn,
        getCommonPrefixDepth,
        getPackageFqnFromTypeFqn,
        isWithinPackageFilters,
        buildVisibleDiagramRelations,
        buildMutualDependencyPairs,
        buildParentFqns,
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
}
