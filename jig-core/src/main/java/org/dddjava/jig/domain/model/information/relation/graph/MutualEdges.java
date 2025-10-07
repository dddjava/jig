package org.dddjava.jig.domain.model.information.relation.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

/**
 * 双方向（相互）なEdge集合。
 * この集合には a->b と b->a の両方が含まれる。自己参照 a-> a は含まれない。
 */
public record MutualEdges<T extends Comparable<T>>(Set<Edge<T>> edges) {

    public static <T extends Comparable<T>> MutualEdges<T> from(Collection<Edge<T>> edges) {
        Set<Edge<T>> set = new HashSet<>(edges);
        Set<Edge<T>> mutual = edges.stream()
                // 自己参照を除く
                .filter(e -> !e.from().equals(e.to()))
                .filter(e -> set.contains(Edge.of(e.to(), e.from())))
                .collect(toSet());
        return new MutualEdges<>(mutual);
    }

    /**
     * 相互なEdgeのうち、片方向（a->b のみ）に正規化して返す。
     * a と b の順序は Edge#compareTo と同様に、ノードの文字列表現で比較し、
     * from.toString() < to.toString() となる方のみを残す。
     */
    public Edges<T> singleDirection() {
        Set<Edge<T>> filtered = edges.stream()
                .filter(e -> e.from().toString().compareTo(e.to().toString()) < 0)
                .collect(Collectors.toSet());
        return new Edges<>(filtered);
    }
}
