package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.packages.PackageDepth;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifiers;
import org.dddjava.jig.domain.model.data.term.TermIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.relation.graph.Edge;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelations;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationKind;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationship;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.dddjava.jig.infrastructure.javaparser.TermFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PackageEdgeDiagramTest {

    @Test
    void 出力されない() {
        var sut = PackageRelations.from(new TypeRelationships(List.of()));
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

    private static TypeRelationship classRelationFrom(String value, String value1) {
        return new TypeRelationship(Edge.of(TypeIdentifier.valueOf(value), TypeIdentifier.valueOf(value1)), TypeRelationKind.不明);
    }

    @MethodSource
    @ParameterizedTest
    void 出力されるパターン(List<TypeRelationship> classRelations, int depth, List<String> expectedContainsTexts) {
        var sut = PackageRelations.from(new TypeRelationships(classRelations)).applyDepth(new PackageDepth(depth));

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
        PackageIdentifiers sut = new PackageIdentifiers(Set.of(
                PackageIdentifier.valueOf("a.a.a.a"),
                PackageIdentifier.valueOf("a.a.b"),
                PackageIdentifier.valueOf("a"),
                PackageIdentifier.valueOf("a.b.c.d.e.f"),
                PackageIdentifier.valueOf("a.b.c.d"),
                PackageIdentifier.valueOf("x")
        ));
        assertEquals(6, sut.identifiers().size());
        assertEquals(6, sut.maxDepth().value());

        PackageIdentifiers depth3 = sut.applyDepth(new PackageDepth(3));
        assertEquals(5, depth3.identifiers().size());
        assertEquals(3, depth3.maxDepth().value());

        PackageIdentifiers depth2 = sut.applyDepth(new PackageDepth(2));
        assertEquals(4, depth2.identifiers().size());

        PackageIdentifiers depth1 = sut.applyDepth(new PackageDepth(1));
        assertEquals(2, depth1.identifiers().size());
    }

    @Test
    void ClassRelationsからPackageRelationsへの変換とapplyDepthの検証() {
        TypeRelationships typeRelationships = new TypeRelationships(List.of(
                classRelationFrom("a.aa.aaa.Foo", "a.ab.aab.aaba.Bar"),
                classRelationFrom("a.aa.aaa.Foo", "b.Bbb")
        ));

        PackageRelations sut = PackageRelations.from(typeRelationships);
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
}