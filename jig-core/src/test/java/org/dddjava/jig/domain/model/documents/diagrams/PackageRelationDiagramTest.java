package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.packages.PackageDepth;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifiers;
import org.dddjava.jig.domain.model.data.term.TermIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.relation.classes.ClassRelation;
import org.dddjava.jig.domain.model.information.relation.classes.ClassRelations;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelation;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelations;
import org.dddjava.jig.infrastructure.javaparser.TermFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PackageRelationDiagramTest {
    private static final Logger logger = LoggerFactory.getLogger(PackageRelationDiagramTest.class);

    @Test
    void 出力されない() {
        var sut = PackageRelations.from(new ClassRelations(List.of()));

        assertFalse(sut.available());
    }

    public static Stream<Arguments> 出力されるパターン() {
        return Stream.of(
                argumentSet("直下の関連がある",
                        List.of(classRelationFrom("a.aa.aaa.Foo", "a.aa.aab.Bar")),
                        3,
                        List.of("\"a.aa.aaa\" -> \"a.aa.aab\";")
                ),
                argumentSet("階層がずれた関連がある",
                        List.of(classRelationFrom("a.aa.aaa.Foo", "a.aa.aab.aaba.Bar"),
                                classRelationFrom("a.aa.aaa.Foo", "b.Bbb")),
                        3,
                        List.of("\"a.aa.aaa\" -> \"a.aa.aab\";",
                                "\"a.aa.aaa\" -> \"b\";")
                ),
                argumentSet("階層がずれた関連を1階層切り詰める",
                        List.of(classRelationFrom("a.aa.aaa.Foo", "a.aa.aab.aaba.Bar"),
                                classRelationFrom("a.aa.aaa.Foo", "b.Bbb")),
                        2,
                        List.of("\"a.aa\" -> \"b\";")
                ),
                argumentSet("デフォルトパッケージを扱える",
                        List.of(classRelationFrom("a.aa.aaa.aaaa.aaaaa.Hoge", "a.aa.aaa.aaaa.aaaab.Fuga"),
                                classRelationFrom("a.aa.aaa.aaaa.aaaaa.Hoge", "DefaultPackageClass")),
                        4,
                        List.of("\"a.aa.aaa.aaaa\" -> \"(default)\";")
                )
        );
    }

    private static ClassRelation classRelationFrom(String value, String value1) {
        return ClassRelation.from(TypeIdentifier.valueOf(value), TypeIdentifier.valueOf(value1)).orElseThrow();
    }

    @MethodSource
    @ParameterizedTest
    void 出力されるパターン(List<ClassRelation> classRelations, int depth, List<String> expectedContainsTexts) {
        var sut = PackageRelations.from(new ClassRelations(classRelations)).applyDepth(new PackageDepth(depth));

        JigDocumentContext jigDocumentContext = mock(JigDocumentContext.class);
        when(jigDocumentContext.packageTerm(any()))
                .thenAnswer(invocationOnMock ->
                        TermFactory.fromPackage(new TermIdentifier("dummy"), "dummy"));

        var list = sut.listUnique().stream()
                .map(packageRelation -> "\"%s\" -> \"%s\";".formatted(packageRelation.from().asText(), packageRelation.to().asText()))
                .toList();

        assertEquals(expectedContainsTexts, list);
    }

    @Test
    void PackageIdentifiersのapplyDepth検証() {
        PackageIdentifiers sut = new PackageIdentifiers(List.of(
                PackageIdentifier.valueOf("a.a.a.a"),
                PackageIdentifier.valueOf("a.a.b"),
                PackageIdentifier.valueOf("a"),
                PackageIdentifier.valueOf("a.b.c.d.e.f"),
                PackageIdentifier.valueOf("a.b.c.d"),
                PackageIdentifier.valueOf("x")
        ));
        assertEquals(6, sut.list().size());
        assertEquals(6, sut.maxDepth().value());

        PackageIdentifiers depth3 = sut.applyDepth(new PackageDepth(3));
        assertEquals(5, depth3.list().size());
        assertEquals(3, depth3.maxDepth().value());

        PackageIdentifiers depth2 = sut.applyDepth(new PackageDepth(2));
        assertEquals(4, depth2.list().size());

        PackageIdentifiers depth1 = sut.applyDepth(new PackageDepth(1));
        assertEquals(2, depth1.list().size());
    }

    @Test
    void ClassRelationsからPackageRelationsへの変換とapplyDepthの検証() {
        ClassRelations classRelations = new ClassRelations(List.of(
                classRelationFrom("a.aa.aaa.Foo", "a.ab.aab.aaba.Bar"),
                classRelationFrom("a.aa.aaa.Foo", "b.Bbb")
        ));

        PackageRelations sut = PackageRelations.from(classRelations);
        assertTrue(sut.available());

        PackageRelations depth3 = sut.applyDepth(new PackageDepth(3));
        assertTrue(depth3.available());

        PackageRelations depth2Relations = sut.applyDepth(new PackageDepth(2));
        assertTrue(depth2Relations.available());

        PackageRelations depth1Relations = sut.applyDepth(new PackageDepth(1));
        assertTrue(depth1Relations.available());

        PackageRelations depth0Relations = sut.applyDepth(new PackageDepth(0));
        // packageRelationでは自己参照も含むためTrueになる。この場合は default -> default
        assertTrue(depth0Relations.available());
    }

    @MethodSource
    @ParameterizedTest
    void 推移依存の抑止(List<PackageRelation> relations, List<PackageRelation> expected) {
        System.setProperty("PackageDiagramRemoveTransitive", "true");
        List<PackageRelation> actual = PackageRelationDiagram.removeTransitive(relations);
        assertEquals(expected, actual);
    }

    public static Stream<Arguments> 推移依存の抑止() {
        BiFunction<String, String, PackageRelation> relation = (from, to) -> new PackageRelation(PackageIdentifier.valueOf(from), PackageIdentifier.valueOf(to));
        return Stream.of(
                argumentSet("推移依存でないものは抑止されない",
                        List.of(relation.apply("a", "b"), relation.apply("a", "c"), relation.apply("b", "d")),
                        List.of(relation.apply("a", "b"), relation.apply("a", "c"), relation.apply("b", "d"))
                ),
                argumentSet("単純な推移依存が抑止される",
                        List.of(relation.apply("a", "b"), relation.apply("a", "c"), relation.apply("b", "c")),
                        List.of(relation.apply("a", "b"), relation.apply("b", "c"))
                ),
                argumentSet("単純な推移依存が抑止される2",
                        List.of(
                                relation.apply("a", "b"), relation.apply("a", "c"), relation.apply("b", "c"),
                                relation.apply("a", "d"), relation.apply("b", "d"), relation.apply("c", "d")
                        ),
                        List.of(
                                relation.apply("a", "b"), relation.apply("b", "c"), relation.apply("c", "d")
                        )
                ),
                argumentSet("相互依存があると変になる", // これ単体だとイマイチだけど相互依存検出とあわせるとまぁいいかという感じかもしれない
                        List.of(
                                relation.apply("a", "b"), relation.apply("a", "c"),
                                relation.apply("b", "a"), relation.apply("b", "c"),
                                relation.apply("c", "b")
                        ),
                        List.of(
                                relation.apply("b", "a"),
                                relation.apply("c", "b")
                        )
                )
        );
    }
}