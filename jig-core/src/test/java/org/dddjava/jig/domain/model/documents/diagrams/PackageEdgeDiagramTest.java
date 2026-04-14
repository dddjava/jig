package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelations;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationKind;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationship;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PackageEdgeDiagramTest {

    @Test
    void 出力されない() {
        var sut = PackageRelations.from(new TypeRelationships(List.of()));
        assertTrue(sut.listUnique().isEmpty());
    }

    private static TypeRelationship classRelationFrom(String value, String value1) {
        return new TypeRelationship(TypeId.valueOf(value), TypeId.valueOf(value1), TypeRelationKind.不明);
    }

    @Test
    void 出力されるパターン() {
        List<TypeRelationship> classRelations = List.of(classRelationFrom("a.aa.aaa.Foo", "a.aa.aab.Bar"));
        var sut = PackageRelations.from(new TypeRelationships(classRelations));//.applyDepth(new PackageDepth(depth));

        var list = sut.listUnique().stream()
                .map(packageRelation -> "\"%s\" -> \"%s\";".formatted(packageRelation.from().asText(), packageRelation.to().asText()))
                .toList();

        assertEquals(List.of("\"a.aa.aaa\" -> \"a.aa.aab\";"), list);
    }

    @Test
    void ClassRelationsからPackageRelationsへの変換とapplyDepthの検証() {
        TypeRelationships typeRelationships = new TypeRelationships(List.of(
                classRelationFrom("a.aa.aaa.Foo", "a.ab.aab.aaba.Bar"),
                classRelationFrom("a.aa.aaa.Foo", "b.Bbb")
        ));

        PackageRelations sut = PackageRelations.from(typeRelationships);
        assertFalse(sut.listUnique().isEmpty());
    }
}