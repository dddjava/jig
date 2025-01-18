package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.classes.type.ClassRelation;
import org.dddjava.jig.domain.model.data.classes.type.ClassRelations;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageComment;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifiers;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PackageRelationDiagramTest {
    private static final Logger logger = LoggerFactory.getLogger(PackageRelationDiagramTest.class);

    @Test
    void パッケージがなければ出力されずエラーにもならない() {
        var packageIdentifiers = new PackageIdentifiers(List.of());
        var classRelations = new ClassRelations(List.of());

        var sut = new PackageRelationDiagram(packageIdentifiers, classRelations);

        JigDocumentContext jigDocumentContext = null;
        var actual = sut.dependencyDotText(jigDocumentContext);

        assertTrue(actual.noValue());
    }

    @Test
    void パッケージがあっても関連がなければ出力されない() {
        var packageIdentifiers = new PackageIdentifiers(List.of(
                PackageIdentifier.valueOf("a.b.hoge"),
                PackageIdentifier.valueOf("a.c.fuga")
        ));
        var classRelations = new ClassRelations(List.of());

        var sut = new PackageRelationDiagram(packageIdentifiers, classRelations);

        JigDocumentContext jigDocumentContext = mock(JigDocumentContext.class);
        when(jigDocumentContext.packageComment(any())).thenReturn(PackageComment.empty(null));
        var actual = sut.dependencyDotText(jigDocumentContext);

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