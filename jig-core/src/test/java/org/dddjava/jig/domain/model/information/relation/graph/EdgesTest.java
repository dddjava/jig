package org.dddjava.jig.domain.model.information.relation.graph;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

class EdgesTest {

    @MethodSource
    @ParameterizedTest
    void 推移簡約(List<Edge<String>> relations, List<Edge<String>> expected) {
        System.setProperty("transitiveReduction", "true");
        var edges = new Edges<>(relations);
        assertEquals(expected.stream().sorted().toList(), edges.transitiveReduction().list());
    }

    public static Stream<Arguments> 推移簡約() {
        return Stream.of(
                argumentSet("推移依存なしはそのまま",
                        List.of(Edge.of("a", "b"), Edge.of("a", "c"), Edge.of("b", "d")),
                        List.of(Edge.of("a", "b"), Edge.of("a", "c"), Edge.of("b", "d"))
                ),
                argumentSet("推移依存のある直接依存は削除される",
                        List.of(Edge.of("a", "b"), Edge.of("a", "c"), Edge.of("b", "c")),
                        List.of(Edge.of("a", "b"), Edge.of("b", "c"))
                ),
                argumentSet("推移依存のある直接依存は削除される2",
                        List.of(
                                Edge.of("a", "b"), Edge.of("b", "c"), Edge.of("c", "d"), // a->b->c->d
                                Edge.of("a", "c"), Edge.of("a", "d"), Edge.of("b", "d") // 削除対象となる直接依存
                        ),
                        List.of(Edge.of("a", "b"), Edge.of("b", "c"), Edge.of("c", "d"))
                ),
                argumentSet("推移依存がある直接依存でも相互依存なら削除しない",
                        List.of(
                                Edge.of("a", "b"), Edge.of("b", "c"), Edge.of("c", "d"), // a->b->c->d
                                Edge.of("a", "c"), Edge.of("a", "d"), Edge.of("b", "d"), // 通常は削除対象となる直接依存
                                Edge.of("c", "b") // c<->bによりb,cが相互依存
                        ),
                        List.of( // a->d は推移依存で解決できる判定になるので削除される
                                Edge.of("a", "b"), Edge.of("b", "c"), Edge.of("c", "d"),
                                Edge.of("a", "c"), Edge.of("b", "d"),
                                Edge.of("c", "b"))
                ),
                argumentSet("推移依存がある直接依存でも相互依存なら削除しない2",
                        List.of(
                                Edge.of("a", "b"), Edge.of("b", "c"), Edge.of("c", "d"), // a->b->c->d
                                Edge.of("a", "c"), Edge.of("a", "d"), Edge.of("b", "d"), // 通常は削除対象となる直接依存
                                Edge.of("d", "b") // d->bによりa以外のすべてがSCCになる
                        ),
                        List.of( // SCC内はすべて残し、aからSCCへのedgeも残すため何も削除されない
                                Edge.of("a", "b"), Edge.of("b", "c"), Edge.of("c", "d"),
                                Edge.of("a", "c"), Edge.of("a", "d"), Edge.of("b", "d"),
                                Edge.of("d", "b"))
                ),
                argumentSet("推移依存がある直接依存でも相互依存なら削除しない3",
                        List.of(
                                Edge.of("a", "b"), Edge.of("b", "c"), Edge.of("c", "d"), // a->b->c->d
                                Edge.of("a", "d"), // 通常は削除対象となる直接依存
                                Edge.of("c", "b") // c<->bによりb,cが相互依存
                        ),
                        List.of(
                                Edge.of("a", "b"), Edge.of("b", "c"), Edge.of("c", "d"),
                                Edge.of("a", "d"), // a->b->c->dで到達可能だが、b<->cが相互依存なので判定対象外となり、a->dは除去対象でなくなる
                                Edge.of("c", "b"))
                ),
                argumentSet("循環依存は除去しない",
                        List.of(Edge.of("a", "b"), Edge.of("a", "c"),
                                Edge.of("b", "a"), Edge.of("b", "c"),
                                Edge.of("c", "b")),
                        List.of(Edge.of("a", "b"), Edge.of("a", "c"),
                                Edge.of("b", "a"), Edge.of("b", "c"),
                                Edge.of("c", "b"))
                )
        );
    }

    @MethodSource
    @ParameterizedTest
    void 相互依存抽出(List<Edge<String>> relations, List<Edge<String>> expected) {
        var edges = new Edges<>(relations);
        assertEquals(expected.stream().sorted().toList(),
                edges.cyclicEdgesGroup().stream().map(Edges::list).flatMap(List::stream).sorted().toList());
    }

    public static Stream<Arguments> 相互依存抽出() {
        return Stream.of(
                argumentSet("相互依存なし",
                        List.of(Edge.of("a", "b"), Edge.of("b", "c"), Edge.of("a", "c")),
                        List.of()
                ),
                argumentSet("相互依存あり",
                        List.of(Edge.of("a", "b"), Edge.of("a", "c"), Edge.of("b", "a"), Edge.of("b", "c")),
                        List.of(Edge.of("a", "b"), Edge.of("b", "a"))
                ),
                argumentSet("相互依存あり2",
                        List.of(Edge.of("a", "b"), Edge.of("b", "a"),
                                Edge.of("c", "d"), Edge.of("d", "c"),
                                Edge.of("b", "c"), Edge.of("a", "z"), Edge.of("b", "z") // これは除外される
                        ),
                        List.of(Edge.of("a", "b"), Edge.of("b", "a"), Edge.of("c", "d"), Edge.of("d", "c"))
                ),
                argumentSet("循環依存",
                        List.of(Edge.of("a", "b"), Edge.of("b", "c"), Edge.of("c", "d"), Edge.of("d", "a"),
                                Edge.of("c", "x")), // これは除外される
                        List.of(Edge.of("a", "b"), Edge.of("b", "c"), Edge.of("c", "d"), Edge.of("d", "a"))
                ),
                argumentSet("循環依存2",
                        List.of(Edge.of("a", "b"), Edge.of("b", "c"), Edge.of("c", "a"),
                                Edge.of("aa", "ab"), Edge.of("ab", "ac"), Edge.of("ac", "aa"),
                                Edge.of("x", "y")),  // これは除外される
                        List.of(Edge.of("a", "b"), Edge.of("b", "c"), Edge.of("c", "a"),
                                Edge.of("aa", "ab"), Edge.of("ab", "ac"), Edge.of("ac", "aa"))
                )
        );
    }

}