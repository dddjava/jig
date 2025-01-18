package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.classes.type.ClassRelations;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifiers;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PackageRelationDiagramTest {

    @Test
    void 対象となるパッケージがない状態で実行してもエラーにならない() {
        var packageIdentifiers = new PackageIdentifiers(List.of());
        var classRelations = new ClassRelations(List.of());

        var sut = new PackageRelationDiagram(packageIdentifiers, classRelations);

        JigDocumentContext jigDocumentContext = null;
        var actual = sut.dependencyDotText(jigDocumentContext);

        assertTrue(actual.noValue());
    }
}