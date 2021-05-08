package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.documents.diagrams.PackageRelationDiagram;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.sources.file.Sources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import testing.JigTestExtension;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(JigTestExtension.class)
public class DependencyServiceTest {

    @Test
    void パッケージ依存(DependencyService dependencyService, Sources sources, JigSourceReadService jigSourceReadService) {
        jigSourceReadService.readProjectData(sources);
        PackageRelationDiagram packageRelationDiagram = dependencyService.packageDependencies();

        // パッケージのリストアップ
        List<String> packageNames = packageRelationDiagram.allPackages().list().stream()
                .map(packageIdentifier -> packageIdentifier.asText())
                .collect(Collectors.toList());
        assertThat(packageNames)
                .containsExactlyInAnyOrder(
                        "stub.domain.model",
                        "stub.domain.model.booleans",
                        "stub.domain.model.category",
                        "stub.domain.model.relation",
                        "stub.domain.model.relation.clz",
                        "stub.domain.model.relation.method",
                        "stub.domain.model.relation.field",
                        "stub.domain.model.relation.annotation",
                        "stub.domain.model.relation.enumeration",
                        "stub.domain.model.smell",
                        "stub.domain.model.type",
                        "stub.domain.model.type.fuga",
                        "stub.domain.model.relation.constant.to_primitive_constant",
                        "stub.domain.model.relation.constant.to_primitive_wrapper_constant",
                        "stub.domain.model.visibility"
                );

        // パッケージの関連
        assertThat(packageRelationDiagram.packageDependencies().list())
                .extracting(dependency -> {
                    PackageIdentifier from = dependency.from();
                    PackageIdentifier to = dependency.to();
                    return from.asText() + " -> " + to.asText();
                })
                .containsExactlyInAnyOrder(
                        "stub.domain.model -> stub.domain.model.relation.annotation",
                        "stub.domain.model.relation -> stub.domain.model.relation.clz",
                        "stub.domain.model.relation -> stub.domain.model.relation.field",
                        "stub.domain.model.relation -> stub.domain.model.relation.method",
                        "stub.domain.model.relation -> stub.domain.model.relation.enumeration",
                        "stub.domain.model.relation -> stub.domain.model.relation.constant.to_primitive_wrapper_constant"
                );
    }
}
