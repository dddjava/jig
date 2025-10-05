package org.dddjava.jig.domain.model.information.relation.graph;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * Edgeのまとまり。グラフ。
 * それぞれの関連をEdgeに単純化してまとめて操作するためのクラス。
 *
 * @param <T> Nodeの型
 */
public record Edges<T extends Comparable<T>>(Collection<Edge<T>> edges) {

    /**
     * 推移簡約
     * `[a->b, b->c, a->c]` を `[a->b, b->c]` だけにする。
     */
    public Edges<T> transitiveReduction() {
        Collection<Edges<T>> cyclicEdgesGroup = cyclicEdgesGroup();
        Set<Edge<T>> cyclicEdges = cyclicEdgesGroup.stream().flatMap(it -> it.edges.stream()).collect(toSet());

        // 循環依存を除いたgraphで到達可能かを判断する
        var graph = edges.stream()
                .filter(edge -> !cyclicEdges.contains(edge))
                .collect(groupingBy(Edge::from, mapping(Edge::to, toList())));

        List<Edge<T>> toRemove = edges.stream()
                // 循環依存は除外しない
                .filter(edge -> !cyclicEdges.contains(edge))
                // 循環依存を除いて到達可能かを判断する
                .filter(edge -> isReachableWithoutDirect(graph, edge))
                .toList();

        List<Edge<T>> newList = edges.stream().filter(relation -> !toRemove.contains(relation)).toList();
        return new Edges<>(newList);
    }

    private boolean isReachableWithoutDirect(Map<T, List<T>> graph, Edge<T> relation) {
        return dfs(graph, relation.from(), relation.to(), new HashSet<>(), true);
    }

    private boolean dfs(Map<T, List<T>> graph,
                        T current,
                        T target,
                        Set<T> visited,
                        boolean skipDirectEdge) {
        // currentとtargetが一致＝到達した
        if (current.equals(target)) return true;
        // ここを始点とした探索はしたことを記録しておく
        visited.add(current);
        // graphの中に到達可能なものがあるかを探す
        return graph.getOrDefault(current, List.of()).stream()
                // 最初に指定したedgeはスキップする。（これをスキップしないと全部到達可能と判断されてしまう）
                .filter(neighbor -> !(skipDirectEdge && neighbor.equals(target)))
                // 判定済み=ここからは到達しないものなのでスキップ（効率化と循環時にstackoverflowになるののストッパー）
                .filter(neighbor -> !visited.contains(neighbor))
                // currentをneighborとして探索して、見つかれば到達可能と判定。
                .anyMatch(neighbor -> dfs(graph, neighbor, target, visited, false));
    }

    /**
     * 相互および循環する関連を抽出する
     */
    public Collection<Edges<T>> cyclicEdgesGroup() {
        Map<T, List<T>> graph = edges.stream()
                .collect(groupingBy(Edge::from, mapping(Edge::to, toList())));

        List<List<T>> stronglyConnectedComponents = detectStronglyConnectedComponents(graph);

        return stronglyConnectedComponents.stream()
                // SCCの要素が2以上ない場合、自己参照を除いてedgeはないので除外する。
                .filter(scc -> scc.size() > 1)
                // relationsから両端が同じSCCに属するedgeを収集する
                .map(scc -> collectEdgesWithin(scc))
                // SCC単位でEdgesにまとめる
                .map(Edges::new)
                .toList();
    }

    private Collection<Edge<T>> collectEdgesWithin(Collection<T> nodes) {
        return edges.stream().filter(edge -> edge.bothEndpointsIn(nodes)).toList();
    }

    /**
     * SCCの抽出
     * Tarjan's Algorithm を使って強連結成分 (SCC: Strongly Connected Components) を抽出する
     */
    private List<List<T>> detectStronglyConnectedComponents(Map<T, List<T>> graph) {
        // Tarjan's Algorithm 用のデータ構造
        Map<T, Integer> indices = new HashMap<>();
        Map<T, Integer> lowLink = new HashMap<>();
        Stack<T> stack = new Stack<>();
        Set<T> onStack = new HashSet<>();
        List<List<T>> result = new ArrayList<>();
        int[] index = {0}; // 可変の訪問順序カウンタ

        // Tarjan の強連結成分ヘルパーメソッド
        for (T node : graph.keySet()) {
            if (!indices.containsKey(node)) {
                strongConnect(node, graph, indices, lowLink, stack, onStack, result, index);
            }
        }

        return result;
    }

    private void strongConnect(T node, Map<T, List<T>> graph,
                               Map<T, Integer> indices, Map<T, Integer> lowLink,
                               Stack<T> stack, Set<T> onStack, List<List<T>> result,
                               int[] index) {
        // ノードに訪問順序を設定
        indices.put(node, index[0]);
        lowLink.put(node, index[0]);
        index[0]++;
        stack.push(node);
        onStack.add(node);

        // 隣接ノードを探索
        for (T neighbor : graph.getOrDefault(node, List.of())) {
            if (!indices.containsKey(neighbor)) {
                // 未探索ノード -> 再帰で探索
                strongConnect(neighbor, graph, indices, lowLink, stack, onStack, result, index);
                lowLink.put(node, Math.min(lowLink.get(node), lowLink.get(neighbor)));
            } else if (onStack.contains(neighbor)) {
                // スタック上のノード -> 強連結の一部
                lowLink.put(node, Math.min(lowLink.get(node), indices.get(neighbor)));
            }
        }

        // 強連結成分の特定とスタックからの取り出し
        if (lowLink.get(node).equals(indices.get(node))) {
            List<T> scc = new ArrayList<>();
            T current;
            do {
                current = stack.pop();
                onStack.remove(current);
                scc.add(current);
            } while (!current.equals(node));
            result.add(scc);
        }
    }

    /**
     * 含まれるEdgeをソートしてリストにする
     */
    public List<Edge<T>> list() {
        return edges.stream().sorted().toList();
    }

    /**
     * 含まれるEdgeをソートしたのち、指定されたconverterで変換してリストにする
     */
    public <R> List<R> listSortedAndConverted(Function<Edge<T>, R> converter) {
        return edges.stream().sorted().map(converter).toList();
    }

    public Stream<Edge<T>> orderedUniqueStream() {
        return edges.stream().sorted().distinct();
    }

    public boolean isEmpty() {
        return edges.isEmpty();
    }

    public Stream<T> nodeStream() {
        return edges.stream()
                .flatMap(edge -> Stream.of(edge.from(), edge.to()));
    }

    /**
     * 双方向（相互）なEdgeだけを抽出する。
     * 例: a->b と b->a の両方が存在する場合、両方のEdgeを結果に含める。
     * 自己ループ（a->a）は対象外。
     */
    public MutualEdges<T> mutualEdges() {
        Set<Edge<T>> set = new HashSet<>(edges);
        Set<Edge<T>> mutual = edges.stream()
                .filter(e -> !e.from().equals(e.to()))
                // 反対にしたものが含まれている
                .filter(e -> set.contains(Edge.of(e.to(), e.from())))
                .collect(toSet());
        return new MutualEdges<>(mutual);
    }
}
