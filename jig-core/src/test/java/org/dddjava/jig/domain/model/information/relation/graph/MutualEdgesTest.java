package org.dddjava.jig.domain.model.information.relation.graph;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

class MutualEdgesTest {

    @MethodSource
    @ParameterizedTest
    void mutualEdgesが期待通り抽出されること(List<Edge<String>> relations, List<Edge<String>> expected) {
        var edges = new Edges<>(relations);
        MutualEdges<String> mutual = edges.mutualEdges();

        assertTrue(mutual.edges().containsAll(expected));
    }

    static Stream<Arguments> mutualEdgesが期待通り抽出されること() {
        return Stream.of(
                argumentSet("相互なし", // 何も抽出されない
                        List.of(Edge.of("a", "b"), Edge.of("b", "c"), Edge.of("a", "c")),
                        List.of()
                ),
                argumentSet("一組の相互", // a<->b のみ
                        List.of(Edge.of("a", "b"), Edge.of("b", "a"), Edge.of("b", "c")),
                        List.of(Edge.of("a", "b"), Edge.of("b", "a"))
                ),
                argumentSet("自己ループは除外", // a->a は mutual にならない
                        List.of(Edge.of("a", "a"), Edge.of("a", "b")),
                        List.of()
                ),
                argumentSet("複数の相互", // a<->b と b<->c の両方
                        List.of(Edge.of("a", "b"), Edge.of("b", "a"),
                                Edge.of("b", "c"), Edge.of("c", "b"),
                                Edge.of("x", "y") // 無関係
                        ),
                        List.of(
                                Edge.of("a", "b"), Edge.of("b", "a"),
                                Edge.of("b", "c"), Edge.of("c", "b"))
                ),
                argumentSet("三つ巴は相互なし扱い", // a->b, b->c, c->a は mutual ではない
                        List.of(Edge.of("a", "b"), Edge.of("b", "c"), Edge.of("c", "a")),
                        List.of()
                )
        );
    }

    @MethodSource
    @ParameterizedTest
    void singleDirection正規化(Set<Edge<String>> mutualEdges, List<Edge<String>> expectedSingleDirection) {
        MutualEdges<String> mutual = new MutualEdges<>(mutualEdges);
        Edges<String> single = mutual.singleDirection();
        assertEquals(expectedSingleDirection.stream().sorted().toList(), single.list());
    }

    static Stream<Arguments> singleDirection正規化() {
        return Stream.of(
                argumentSet("一組の相互は片方向に正規化",
                        Set.of(Edge.of("a", "b"), Edge.of("b", "a")),
                        List.of(Edge.of("a", "b"))
                ),
                argumentSet("複数の相互は from<to のみ残す",
                        Set.of(Edge.of("a", "b"), Edge.of("b", "a"),
                                Edge.of("c", "b"), Edge.of("b", "c")),
                        List.of(Edge.of("a", "b"), Edge.of("b", "c"))
                ),
                argumentSet("自己ループは生成側で除外想定だが、混じっていても除外される",
                        Set.of(Edge.of("a", "a"), Edge.of("a", "b"), Edge.of("b", "a")),
                        List.of(Edge.of("a", "b"))
                ),
                argumentSet("順序は文字列表現（b<c）",
                        Set.of(Edge.of("c", "b"), Edge.of("b", "c")),
                        List.of(Edge.of("b", "c"))
                )
        );
    }
}
