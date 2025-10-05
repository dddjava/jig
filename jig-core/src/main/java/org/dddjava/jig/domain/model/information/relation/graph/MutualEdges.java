package org.dddjava.jig.domain.model.information.relation.graph;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 双方向（相互）なEdge集合。
 * この集合には a->b と b->a の両方が含まれることを前提とする。
 */
public record MutualEdges<T extends Comparable<T>>(Set<Edge<T>> edges) {

    /**
     * 相互なEdgeのうち、片方向（a->b のみ）に正規化して返す。
     * a と b の順序は Edge#compareTo と同様に、ノードの文字列表現で比較し、
     * from.toString() < to.toString() となる方のみを残す。
     * 自己ループ（a->a）は除外対象外だが、mutualEdges 生成側で除かれている想定。
     */
    public Edges<T> singleDirection() {
        Set<Edge<T>> filtered = edges.stream()
                .filter(e -> e.from().toString().compareTo(e.to().toString()) < 0)
                .collect(Collectors.toSet());
        return new Edges<>(filtered);
    }
}
