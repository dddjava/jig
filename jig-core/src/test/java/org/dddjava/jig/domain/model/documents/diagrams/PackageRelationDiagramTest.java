package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.classes.type.ClassRelation;
import org.dddjava.jig.domain.model.data.classes.type.ClassRelations;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.packages.*;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PackageRelationDiagramTest {
    private static final Logger logger = LoggerFactory.getLogger(PackageRelationDiagramTest.class);

    public static Stream<Arguments> 出力されないパターン() {
        return Stream.of(
                Arguments.argumentSet(
                        "パッケージも関連も空",
                        new PackageIdentifiers(List.of()),
                        new ClassRelations(List.of())
                ),
                Arguments.argumentSet(
                        "パッケージはあるが関連はない",
                        new PackageIdentifiers(List.of(
                                PackageIdentifier.valueOf("a.aa"),
                                PackageIdentifier.valueOf("a.aa.aaa"),
                                PackageIdentifier.valueOf("a.ab")
                        )),
                        new ClassRelations(List.of())
                )
        );
    }

    @MethodSource
    @ParameterizedTest
    void 出力されないパターン(PackageIdentifiers packageIdentifiers, ClassRelations classRelations) {
        var sut = new PackageRelationDiagram(packageIdentifiers, classRelations);

        var actual = sut.dependencyDotText(null);

        assertTrue(actual.noValue());
    }

    public static Stream<Arguments> 出力されるパターン() {
        return Stream.of(
                Arguments.argumentSet("直下の関連がある",
                        new PackageIdentifiers(List.of(
                                PackageIdentifier.valueOf("a.aa.aaa"),
                                PackageIdentifier.valueOf("a.aa.aab")
                        )),
                        new ClassRelations(List.of(
                                new ClassRelation(TypeIdentifier.valueOf("a.aa.aaa.Foo"), TypeIdentifier.valueOf("a.aa.aab.Bar"))
                        )),
                        3,
                        List.of(
                                "\"a.aa.aaa\" -> \"a.aa.aab\";"
                        )
                ),
                Arguments.argumentSet("階層がずれた関連がある",
                        new PackageIdentifiers(List.of(
                                PackageIdentifier.valueOf("a.aa.aaa"),
                                PackageIdentifier.valueOf("a.aa.aab.aaba"),
                                PackageIdentifier.valueOf("b")
                        )),
                        new ClassRelations(List.of(
                                new ClassRelation(TypeIdentifier.valueOf("a.aa.aaa.Foo"), TypeIdentifier.valueOf("a.aa.aab.aaba.Bar")),
                                new ClassRelation(TypeIdentifier.valueOf("a.aa.aaa.Foo"), TypeIdentifier.valueOf("b.Bbb"))
                        )),
                        3,
                        List.of(
                                "\"a.aa.aaa\" -> \"a.aa.aab\";",
                                "\"a.aa.aaa\" -> \"b\";",
                                "subgraph \"cluster_a.aa\""
                        )
                )
        );
    }

    @MethodSource
    @ParameterizedTest
    void 出力されるパターン(PackageIdentifiers packageIdentifiers, ClassRelations classRelations, int depth, List<String> expectedContainsTexts) {
        var sut = new PackageRelationDiagram(packageIdentifiers, classRelations).applyDepth(new PackageDepth(depth));

        JigDocumentContext jigDocumentContext = mock(JigDocumentContext.class);
        when(jigDocumentContext.packageComment(any())).thenReturn(PackageComment.empty(null));
        var actual = sut.dependencyDotText(jigDocumentContext);

        for (String expectedContains : expectedContainsTexts) {
            assertTrue(actual.text().contains(expectedContains), actual.text());
        }

        logger.debug(actual.text());
    }

    @Test
    void ClassRelationsからPackageRelationsへの変換とdepthの検証() {
        ClassRelations classRelations = new ClassRelations(List.of(
                new ClassRelation(TypeIdentifier.valueOf("a.aa.aaa.Foo"), TypeIdentifier.valueOf("a.ab.aab.aaba.Bar")),
                new ClassRelation(TypeIdentifier.valueOf("a.aa.aaa.Foo"), TypeIdentifier.valueOf("b.Bbb"))
        ));

        PackageRelations sut = classRelations.toPackageRelations();
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