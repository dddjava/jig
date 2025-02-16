package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.information.relation.classes.ClassRelations;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelations;
import org.dddjava.jig.domain.model.sources.JigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import testing.JigTestExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(JigTestExtension.class)
public class PackageDependenciesTest {

    @Test
    void パッケージ依存(JigService jigService, JigRepository jigRepository) {
        var jigTypes = jigService.coreDomainJigTypes(jigRepository);

        var sut = PackageRelations.from(ClassRelations.internalRelation(jigTypes));

        // パッケージの関連
        assertThat(sut.list())
                .extracting(dependency -> {
                    PackageIdentifier from = dependency.from();
                    PackageIdentifier to = dependency.to();
                    return from.asText() + " -> " + to.asText();
                })
                .containsExactly(
                        "stub.domain.model -> stub.domain.model.relation.annotation",
                        "stub.domain.model.relation -> stub.domain.model.relation.clz",
                        "stub.domain.model.relation -> stub.domain.model.relation.constant.to_primitive_wrapper_constant",
                        "stub.domain.model.relation -> stub.domain.model.relation.enumeration",
                        "stub.domain.model.relation -> stub.domain.model.relation.field",
                        "stub.domain.model.relation -> stub.domain.model.relation.method"
                );
    }
}
