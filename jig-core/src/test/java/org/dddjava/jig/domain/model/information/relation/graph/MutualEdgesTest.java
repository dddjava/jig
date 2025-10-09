package org.dddjava.jig.domain.model.information.relation.graph;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

class MutualEdgesTest {

    @MethodSource
    @ParameterizedTest
    void mutualEdgesが期待通り抽出されること(List<Edge<String>> relations, Set<Edge<String>> expected) {
        Edges<String> edges = new Edges<>(relations);
        MutualEdges<String> mutual = edges.mutualEdges();

        assertEquals(expected, mutual.edges());
    }

    static Stream<Arguments> mutualEdgesが期待通り抽出されること() {
        return Stream.of(
                argumentSet("相互なし", // 何も抽出されない
                        List.of(Edge.of("a", "b"), Edge.of("b", "c"), Edge.of("a", "c")),
                        Set.of()
                ),
                argumentSet("一組の相互", // a<->b のみ
                        List.of(Edge.of("a", "b"), Edge.of("b", "a"), Edge.of("b", "c")),
                        Set.of(new MutualEdge<>("a", "b"))
                ),
                argumentSet("自己ループは除外", // a->a は mutual にならない
                        List.of(Edge.of("a", "a"), Edge.of("a", "b")),
                        Set.of()
                ),
                argumentSet("複数の相互", // a<->b と b<->c の両方
                        List.of(Edge.of("a", "b"), Edge.of("b", "a"),
                                Edge.of("b", "c"), Edge.of("c", "b"),
                                Edge.of("x", "y") // 無関係
                        ),
                        Set.of(new MutualEdge<>("a", "b"),
                                new MutualEdge<>("b", "c"))
                ),
                argumentSet("三つ巴は相互なし扱い", // a->b, b->c, c->a は mutual ではない
                        List.of(Edge.of("a", "b"), Edge.of("b", "c"), Edge.of("c", "a")),
                        Set.of()
                )
        );
    }

    @MethodSource
    @ParameterizedTest
    void singleDirection正規化(List<Edge<String>> edges, Set<MutualEdge<String>> expected) {
        MutualEdges<String> mutual = MutualEdges.from(edges);
        Set<MutualEdge<String>> actual = mutual.edges();
        assertEquals(expected, actual);
    }

    static Stream<Arguments> singleDirection正規化() {
        return Stream.of(
                argumentSet("一組の相互は片方向に正規化",
                        List.of(Edge.of("a", "b"), Edge.of("b", "a")),
                        Set.of(new MutualEdge<>("a", "b"))
                ),
                argumentSet("複数の相互は from<to のみ残す",
                        List.of(Edge.of("a", "b"), Edge.of("b", "a"),
                                Edge.of("c", "b"), Edge.of("b", "c")),
                        Set.of(new MutualEdge<>("a", "b"), new MutualEdge<>("b", "c"))
                ),
                argumentSet("自己ループは生成側で除外想定だが、混じっていても除外される",
                        List.of(Edge.of("a", "a"), Edge.of("a", "b"), Edge.of("b", "a")),
                        Set.of(new MutualEdge<>("a", "b"))
                ),
                argumentSet("順序は文字列表現（b<c）",
                        List.of(Edge.of("c", "b"), Edge.of("b", "c")),
                        Set.of(new MutualEdge<>("b", "c"))
                )
        );
    }

    @MethodSource
    @ParameterizedTest
    void notContainsの判定が期待通り(boolean expected, List<Edge<String>> allEdges, Edge<String> query) {
        MutualEdges<String> mutual = MutualEdges.from(allEdges);
        assertEquals(expected, mutual.notContains(query));
    }

    static Stream<Arguments> notContainsの判定が期待通り() {
        List<Edge<String>> base = List.of(
                Edge.of("a", "b"),
                Edge.of("b", "a"), // a<->b の相互
                Edge.of("b", "c")   // 片方向のみ
        );
        return Stream.of(
                argumentSet("相互に含まれる（a->b）ので notContains は false",
                        false, base, Edge.of("a", "b")),
                argumentSet("相互に含まれる（b->a）ので notContains は false",
                        false, base, Edge.of("b", "a")),
                argumentSet("相互に含まれない（b->c 片方向）ので notContains は true",
                        true, base, Edge.of("b", "c")),
                argumentSet("相互に含まれない（c->b が存在しない）ので notContains は true",
                        true, base, Edge.of("c", "b")),
                argumentSet("自己ループ（a->a）は mutual に含まれないので notContains は true",
                        true, base, Edge.of("a", "a"))
        );
    }
}
