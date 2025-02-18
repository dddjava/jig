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
        assertEquals(expected.stream().sorted().toList(), edges.transitiveReduction().list());
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
                                edge("a", "b"), edge("b", "c"), edge("c", "d"), // a->b->c->d
                                edge("a", "c"), edge("a", "d"), edge("b", "d") // 削除対象となる直接依存
                        ),
                        List.of(edge("a", "b"), edge("b", "c"), edge("c", "d"))
                ),
                argumentSet("推移依存がある直接依存でも相互依存なら削除しない",
                        List.of(
                                edge("a", "b"), edge("b", "c"), edge("c", "d"), // a->b->c->d
                                edge("a", "c"), edge("a", "d"), edge("b", "d"), // 通常は削除対象となる直接依存
                                edge("c", "b") // c<->bによりb,cが相互依存
                        ),
                        List.of( // a->d は推移依存で解決できる判定になるので削除される
                                edge("a", "b"), edge("b", "c"), edge("c", "d"),
                                edge("a", "c"), edge("b", "d"),
                                edge("c", "b"))
                ),
                argumentSet("推移依存がある直接依存でも相互依存なら削除しない2",
                        List.of(
                                edge("a", "b"), edge("b", "c"), edge("c", "d"), // a->b->c->d
                                edge("a", "c"), edge("a", "d"), edge("b", "d"), // 通常は削除対象となる直接依存
                                edge("d", "b") // d->bによりa以外のすべてがSCCになる
                        ),
                        List.of( // SCC内はすべて残し、aからSCCへのedgeも残すため何も削除されない
                                edge("a", "b"), edge("b", "c"), edge("c", "d"),
                                edge("a", "c"), edge("a", "d"), edge("b", "d"),
                                edge("d", "b"))
                ),
                argumentSet("推移依存がある直接依存でも相互依存なら削除しない3",
                        List.of(
                                edge("a", "b"), edge("b", "c"), edge("c", "d"), // a->b->c->d
                                edge("a", "d"), // 通常は削除対象となる直接依存
                                edge("c", "b") // c<->bによりb,cが相互依存
                        ),
                        List.of(
                                edge("a", "b"), edge("b", "c"), edge("c", "d"),
                                edge("a", "d"), // a->b->c->dで到達可能だが、b<->cが相互依存なので判定対象外となり、a->dは除去対象でなくなる
                                edge("c", "b"))
                ),
                argumentSet("循環依存は除去しない",
                        List.of(edge("a", "b"), edge("a", "c"),
                                edge("b", "a"), edge("b", "c"),
                                edge("c", "b")),
                        List.of(edge("a", "b"), edge("a", "c"),
                                edge("b", "a"), edge("b", "c"),
                                edge("c", "b"))
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
                        List.of(edge("a", "b"), edge("b", "c"), edge("a", "c")),
                        List.of()
                ),
                argumentSet("相互依存あり",
                        List.of(edge("a", "b"), edge("a", "c"), edge("b", "a"), edge("b", "c")),
                        List.of(edge("a", "b"), edge("b", "a"))
                ),
                argumentSet("相互依存あり2",
                        List.of(edge("a", "b"), edge("b", "a"),
                                edge("c", "d"), edge("d", "c"),
                                edge("b", "c"), edge("a", "z"), edge("b", "z") // これは除外される
                        ),
                        List.of(edge("a", "b"), edge("b", "a"), edge("c", "d"), edge("d", "c"))
                ),
                argumentSet("循環依存",
                        List.of(edge("a", "b"), edge("b", "c"), edge("c", "d"), edge("d", "a"),
                                edge("c", "x")), // これは除外される
                        List.of(edge("a", "b"), edge("b", "c"), edge("c", "d"), edge("d", "a"))
                ),
                argumentSet("循環依存2",
                        List.of(edge("a", "b"), edge("b", "c"), edge("c", "a"),
                                edge("aa", "ab"), edge("ab", "ac"), edge("ac", "aa"),
                                edge("x", "y")),  // これは除外される
                        List.of(edge("a", "b"), edge("b", "c"), edge("c", "a"),
                                edge("aa", "ab"), edge("ab", "ac"), edge("ac", "aa"))
                )
        );
    }

}