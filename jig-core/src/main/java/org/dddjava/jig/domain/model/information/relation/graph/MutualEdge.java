package org.dddjava.jig.domain.model.information.relation.graph;

/**
 * 双方向のEdge
 *
 * from/toの関係ではないのでa,bという名称にする。
 *
 * @param <NODE> ノードの型
 */
public record MutualEdge<NODE>(NODE a, NODE b) implements Comparable<MutualEdge<NODE>> {

    public static <T> MutualEdge<T> from(Edge<T> edge) {
        var a = edge.from();
        var b = edge.to();
        if (a.toString().compareTo(b.toString()) <= 0) {
            return new MutualEdge<>(a, b);
        } else {
            return new MutualEdge<>(b, a);
        }
    }

    /**
     * ソートのための他のEdgeとの比較処理。
     * NODEにインタフェースを持たせていないので、ノードの文字列表現で比較する。
     * NODEをComparableに制限してもいいかもしれない。
     */
    @Override
    public int compareTo(MutualEdge<NODE> o) {
        int fromComparison = this.a.toString().compareTo(o.a.toString());
        return fromComparison != 0 ? fromComparison : this.b.toString().compareTo(o.b.toString());
    }
}
