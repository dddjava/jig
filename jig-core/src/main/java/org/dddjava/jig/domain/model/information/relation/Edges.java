package org.dddjava.jig.domain.model.information.relation;

import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelation;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class Edges<T> {
    private final Collection<Edge<T>> relations;

    public static <T> Edge<T> edge(T from, T to) {
        return new Edge<>(from, to);
    }

    public Edges(Collection<Edge<T>> relations) {
        this.relations = relations;
    }

    public static Edges<PackageIdentifier> from(Collection<PackageRelation> packageRelations) {
        return new Edges<>(packageRelations.stream()
                .map(packageRelation -> new Edge<>(packageRelation.from(), packageRelation.to()))
                .toList());
    }

    /**
     * 推移簡約
     * `[a->b, b->c, a->c]` を `[a->b, b->c]` だけにする。
     */
    public Edges<T> transitiveReduction() {
        // とりあえずシステムプロパティを有効にしないと機能しないようにしておく
        if (!"true".equals(System.getProperty("transitiveReduction"))) {
            return this;
        }
        Map<T, List<T>> graph = relations.stream()
                .collect(Collectors.groupingBy(Edge::from, Collectors.mapping(Edge::to, Collectors.toList())));

        List<Edge<T>> toRemove = relations.stream()
                .filter(relation -> isReachableWithoutDirect(graph, relation))
                .toList();

        List<Edge<T>> newList = relations.stream().filter(relation -> !toRemove.contains(relation)).toList();
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
        for (T neighbor : graph.getOrDefault(current, List.of())) {
            // 最初に指定したedgeはスキップする。（これをスキップしないと全部到達可能と判断されてしまう）
            if (skipDirectEdge && neighbor.equals(target)) continue;
            // 判定済み=ここからは到達しないものなのでスキップ（効率化と循環時にstackoverflowになるののストッパー）
            if (visited.contains(neighbor)) continue;
            // currentをneighborとして探索する。これで到達可能なら到達可能。
            if (dfs(graph, neighbor, target, visited, false)) return true;
        }
        // currentを始点とする関連がなかった or 到達しなかった
        return false;
    }

    public <R> List<R> convert(BiFunction<T, T, R> converter) {
        return relations.stream()
                .map(edge -> converter.apply(edge.from(), edge.to()))
                .toList();
    }

    public List<Edge<T>> list() {
        return relations.stream().toList();
    }
}
