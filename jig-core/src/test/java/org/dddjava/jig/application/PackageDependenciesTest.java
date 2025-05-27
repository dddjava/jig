package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import testing.JigTestExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(JigTestExtension.class)
public class PackageDependenciesTest {

    @Test
    void パッケージ依存(JigService jigService, JigRepository jigRepository) {
        var jigTypes = jigService.coreDomainJigTypesWithRelationships(jigRepository);

        var sut = PackageRelations.from(jigTypes.typeRelationships());

        // パッケージの関連
        var actual = sut.listUnique().stream()
                .map(dependency -> {
                    PackageIdentifier from = dependency.from();
                    PackageIdentifier to = dependency.to();
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
