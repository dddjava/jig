package org.dddjava.jig.domain.model.information.relation.graph;

import java.util.Collection;

/**
 * Edgeのまとまり。グラフ。
 * それぞれの関連をEdgeに単純化してまとめて操作するためのクラス。
 *
 * @param <T> Nodeの型
 */
public record Edges<T extends Comparable<T>>(Collection<Edge<T>> edges) {


}
