package org.dddjava.jig.domain.model.information.relation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.dddjava.jig.domain.model.information.relation.Edges.edge;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

class EdgesTest {

    @MethodSource
    @ParameterizedTest
    void 推移簡約(List<Edge<String>> relations, List<Edge<String>> expected) {
        System.setProperty("transitiveReduction", "true");
        var edges = new Edges<>(relations);
        assertEquals(expected, edges.transitiveReduction().list());
    }

    public static Stream<Arguments> 推移簡約() {
        return Stream.of(
                argumentSet("推移依存なしはそのまま",
                        List.of(edge("a", "b"), edge("a", "c"), edge("b", "d")),
                        List.of(edge("a", "b"), edge("a", "c"), edge("b", "d"))
                ),
                argumentSet("推移依存のある直接依存は削除される",
                        List.of(edge("a", "b"), edge("a", "c"), edge("b", "c")),
                        List.of(edge("a", "b"), edge("b", "c"))
                ),
                argumentSet("推移依存のある直接依存は削除される2",
                        List.of(
                                edge("a", "b"), edge("a", "c"), edge("b", "c"),
                                edge("a", "d"), edge("b", "d"), edge("c", "d")
                        ),
                        List.of(edge("a", "b"), edge("b", "c"), edge("c", "d"))
                ),
                argumentSet("相互依存があると変になる", // これ単体だとイマイチだけど相互依存検出とあわせるとまぁいいかという感じかもしれない
                        List.of(
                                edge("a", "b"), edge("a", "c"),
                                edge("b", "a"), edge("b", "c"),
                                edge("c", "b")
                        ),
                        List.of(
                                edge("b", "a"), edge("c", "b")
                        )
                )
        );
    }
}