package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.classes.type.ClassRelation;
import org.dddjava.jig.domain.model.data.classes.type.ClassRelations;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageComment;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifiers;
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

    @Test
    void パッケージが2つあり関連があれば出力できる() {
        var packageIdentifiers = new PackageIdentifiers(List.of(
                PackageIdentifier.valueOf("a.b.hoge"),
                PackageIdentifier.valueOf("a.c.fuga")
        ));
        var classRelations = new ClassRelations(List.of(
                new ClassRelation(TypeIdentifier.valueOf("a.b.hoge.Foo"), TypeIdentifier.valueOf("a.c.fuga.Bar"))
        ));

        var sut = new PackageRelationDiagram(packageIdentifiers, classRelations);

        JigDocumentContext jigDocumentContext = mock(JigDocumentContext.class);
        when(jigDocumentContext.packageComment(any())).thenReturn(PackageComment.empty(null));
        var actual = sut.dependencyDotText(jigDocumentContext);

        assertFalse(actual.noValue());
        assertTrue(actual.text().contains("""
                "a.b.hoge" -> "a.c.fuga";
                """), actual.text());

        logger.debug(actual.text());
    }
}