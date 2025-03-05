package org.dddjava.jig.domain.model.information.relation.graph;

import java.util.Collection;

/**
 * 有向グラフの辺
 *
 * @param from 始点
 * @param to 終点
 * @param <NODE> ノードの型
 */
public record Edge<NODE>(NODE from, NODE to) implements Comparable<Edge<NODE>> {

    /**
     * 指定されたコレクション内に両端のノードが含まれているかを確認する。
     */
    boolean bothEndpointsIn(Collection<NODE> nodes) {
        return nodes.contains(from) && nodes.contains(to);
    }

    /**
     * ソートのための他のEdgeとの比較処理。
     * NODEにインタフェースを持たせていないので、ノードの文字列表現で比較する。
     * NODEがComparableの場合はそっちを採用してもいいかもしれない。
     */
    @Override
    public int compareTo(Edge<NODE> o) {
        int fromComparison = this.from.toString().compareTo(o.from.toString());
        return fromComparison != 0 ? fromComparison : this.to.toString().compareTo(o.to.toString());
    }
}
