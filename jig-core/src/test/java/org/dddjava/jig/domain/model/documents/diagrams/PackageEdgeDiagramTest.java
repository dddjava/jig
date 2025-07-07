package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.packages.PackageDepth;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.packages.PackageIds;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.terms.TermIdentifier;
import org.dddjava.jig.domain.model.data.terms.TermKind;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.relation.graph.Edge;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelations;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationKind;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationship;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
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
        return new TypeRelationship(Edge.of(TypeId.valueOf(value), TypeId.valueOf(value1)), TypeRelationKind.不明);
    }

    @MethodSource
    @ParameterizedTest
    void 出力されるパターン(List<TypeRelationship> classRelations, int depth, List<String> expectedContainsTexts) {
        var sut = PackageRelations.from(new TypeRelationships(classRelations)).applyDepth(new PackageDepth(depth));

        JigDocumentContext jigDocumentContext = mock(JigDocumentContext.class);
        when(jigDocumentContext.packageTerm(any()))
                .thenAnswer(invocationOnMock ->
                        Term.simple(new TermIdentifier("dummy"), "dummy", TermKind.パッケージ));

        var list = sut.listUnique().stream()
                .map(packageRelation -> "\"%s\" -> \"%s\";".formatted(packageRelation.from().asText(), packageRelation.to().asText()))
                .toList();

        assertEquals(expectedContainsTexts, list);
    }

    @Test
    void PackageIdentifiersのapplyDepth検証() {
        PackageIds sut = new PackageIds(Set.of(
                PackageId.valueOf("a.a.a.a"),
                PackageId.valueOf("a.a.b"),
                PackageId.valueOf("a"),
                PackageId.valueOf("a.b.c.d.e.f"),
                PackageId.valueOf("a.b.c.d"),
                PackageId.valueOf("x")
        ));
        assertEquals(6, sut.values().size());
        assertEquals(6, sut.maxDepth().value());

        PackageIds depth3 = sut.applyDepth(new PackageDepth(3));
        assertEquals(5, depth3.values().size());
        assertEquals(3, depth3.maxDepth().value());

        PackageIds depth2 = sut.applyDepth(new PackageDepth(2));
        assertEquals(4, depth2.values().size());

        PackageIds depth1 = sut.applyDepth(new PackageDepth(1));
        assertEquals(2, depth1.values().size());
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