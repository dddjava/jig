package org.dddjava.jig.domain.model.information.relation.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * 双方向（相互）なEdge集合。
 * この集合には a->b と b->a の両方が含まれる。自己参照 a-> a は含まれない。
 */
public record MutualEdges<T extends Comparable<T>>(Set<MutualEdge<T>> edges) {

    public static <T extends Comparable<T>> MutualEdges<T> from(Collection<Edge<T>> edges) {
        Set<Edge<T>> set = new HashSet<>(edges);
        Set<MutualEdge<T>> mutual = set.stream()
                // 自己参照を除く
                .filter(e -> !e.from().equals(e.to()))
                .filter(e -> set.contains(Edge.of(e.to(), e.from())))
                .map(edge -> MutualEdge.from(edge))
                .collect(toSet());
        return new MutualEdges<>(mutual);
    }

    public boolean notContains(Edge<T> edge) {
        return edges.contains(MutualEdge.from(edge));
    }
}
