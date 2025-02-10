package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.packages.PackageDepth;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifiers;
import org.dddjava.jig.domain.model.data.term.TermIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.relation.classes.ClassRelation;
import org.dddjava.jig.domain.model.information.relation.classes.ClassRelations;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelations;
import org.dddjava.jig.infrastructure.javaparser.TermFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PackageRelationDiagramTest {
    private static final Logger logger = LoggerFactory.getLogger(PackageRelationDiagramTest.class);

    public static Stream<Arguments> 出力されないパターン() {
        return Stream.of(
                Arguments.argumentSet(
                        "パッケージも関連も空",
                        List.of(),
                        List.of()
                ),
                Arguments.argumentSet(
                        "パッケージはあるが関連はない",
                        List.of(PackageIdentifier.valueOf("a.aa"),
                                PackageIdentifier.valueOf("a.aa.aaa"),
                                PackageIdentifier.valueOf("a.ab")),
                        List.of()
                )
        );
    }

    @MethodSource
    @ParameterizedTest
    void 出力されないパターン(List<PackageIdentifier> packageIdentifiers, List<ClassRelation> classRelations) {
        var sut = new PackageRelationDiagram(new PackageIdentifiers(packageIdentifiers), new ClassRelations(classRelations));

        var actual = sut.dependencyDotText(null);

        assertTrue(actual.noValue());
    }

    public static Stream<Arguments> 出力されるパターン() {
        return Stream.of(
                Arguments.argumentSet("直下の関連がある",
                        List.of(PackageIdentifier.valueOf("a.aa.aaa"),
                                PackageIdentifier.valueOf("a.aa.aab")),
                        List.of(classRelationFrom("a.aa.aaa.Foo", "a.aa.aab.Bar")),
                        3,
                        List.of("\"a.aa.aaa\" -> \"a.aa.aab\";")
                ),
                Arguments.argumentSet("階層がずれた関連がある",
                        List.of(PackageIdentifier.valueOf("a.aa.aaa"),
                                PackageIdentifier.valueOf("a.aa.aab.aaba"),
                                PackageIdentifier.valueOf("b")),
                        List.of(classRelationFrom("a.aa.aaa.Foo", "a.aa.aab.aaba.Bar"),
                                classRelationFrom("a.aa.aaa.Foo", "b.Bbb")),
                        3,
                        List.of("\"a.aa.aaa\" -> \"a.aa.aab\";",
                                "\"a.aa.aaa\" -> \"b\";",
                                "subgraph \"cluster_a.aa\"")
                ),
                Arguments.argumentSet("階層がずれた関連を1階層切り詰める",
                        List.of(PackageIdentifier.valueOf("a.aa.aaa"),
                                PackageIdentifier.valueOf("a.aa.aab.aaba"),
                                PackageIdentifier.valueOf("b")),
                        List.of(classRelationFrom("a.aa.aaa.Foo", "a.aa.aab.aaba.Bar"),
                                classRelationFrom("a.aa.aaa.Foo", "b.Bbb")),
                        2,
                        List.of("\"a.aa\" -> \"b\";")
                ),
                Arguments.argumentSet("デフォルトパッケージを扱える",
                        List.of(PackageIdentifier.valueOf("a.aa.aaa.aaaa.aaaaa"),
                                PackageIdentifier.valueOf("a.aa.aaa.aaaa.aaaab"),
                                PackageIdentifier.defaultPackage(),
                                PackageIdentifier.valueOf("a")),
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
    void 出力されるパターン(List<PackageIdentifier> packageIdentifiers, List<ClassRelation> classRelations, int depth, List<String> expectedContainsTexts) {
        var sut = new PackageRelationDiagram(new PackageIdentifiers(packageIdentifiers), new ClassRelations(classRelations))
                .applyDepth(new PackageDepth(depth));

        JigDocumentContext jigDocumentContext = mock(JigDocumentContext.class);
        when(jigDocumentContext.packageTerm(any()))
                .thenAnswer(invocationOnMock ->
                        TermFactory.fromPackage(new TermIdentifier("dummy"), "dummy"));
        var actual = sut.dependencyDotText(jigDocumentContext);

        for (String expectedContains : expectedContainsTexts) {
            assertTrue(actual.text().contains(expectedContains), actual.text());
        }

        logger.debug(actual.text());
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
        assertFalse(depth0Relations.available());
    }
}