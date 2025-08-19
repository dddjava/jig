package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelations;
import org.junit.jupiter.api.Test;
import testing.JigTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JigTest
public class PackageDependenciesTest {

    @Test
    void パッケージ依存(JigService jigService, JigRepository jigRepository) {
        var jigTypes = jigService.coreTypesAndRelations(jigRepository);

        var sut = PackageRelations.from(jigTypes.internalTypeRelationships());

        // パッケージの関連
        var actual = sut.listUnique().stream()
                .map(dependency -> {
                    PackageId from = dependency.from();
                    PackageId to = dependency.to();
                    return from.asText() + " -> " + to.asText();
                })
                .toList();

        var expected = List.of(
                "stub.domain.model -> stub.domain.model.relation.annotation",
                "stub.domain.model.relation -> stub.domain.model.relation.clz",
                "stub.domain.model.relation -> stub.domain.model.relation.constant.to_primitive_wrapper_constant",
                "stub.domain.model.relation -> stub.domain.model.relation.enumeration",
                "stub.domain.model.relation -> stub.domain.model.relation.method"
        );

        assertEquals(expected, actual);
    }
}
