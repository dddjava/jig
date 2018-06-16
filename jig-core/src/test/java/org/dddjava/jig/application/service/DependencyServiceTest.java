package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.networks.packages.PackageNetwork;
import org.dddjava.jig.infrastructure.DefaultLayout;
import org.dddjava.jig.infrastructure.LocalProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import testing.TestConfiguration;
import testing.TestSupport;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = "jig.model.pattern = stub.domain.model.+")
public class DependencyServiceTest {

    @Autowired
    DependencyService sut;

    @Autowired
    ImplementationService implementationService;
    @Autowired
    LocalProject localProject;

    @Test
    void パッケージ依存() {
        ProjectData projectData = implementationService.readProjectData(localProject);
        PackageNetwork packageNetwork = sut.packageDependencies(projectData);

        // パッケージのリストアップ
        List<String> packageNames = packageNetwork.allPackages().stream()
                .map(packageIdentifier -> packageIdentifier.format(value -> value))
                // TODO 重複して入ってるけどどうしよう
                .distinct()
                .collect(Collectors.toList());
        assertThat(packageNames)
                .containsExactlyInAnyOrder(
                        "stub.domain.model",
                        "stub.domain.model.booleans",
                        "stub.domain.model.kind",
                        "stub.domain.model.relation",
                        "stub.domain.model.relation.clz",
                        "stub.domain.model.relation.method",
                        "stub.domain.model.relation.field",
                        "stub.domain.model.relation.annotation",
                        "stub.domain.model.relation.enumeration",
                        "stub.domain.model.type",
                        "stub.domain.model.type.fuga"
                );

        // パッケージの関連
        assertThat(packageNetwork.packageDependencies().list())
                .extracting(dependency -> {
                    PackageIdentifier from = dependency.from();
                    PackageIdentifier to = dependency.to();
                    return from.format(value -> value) + " -> " + to.format(value -> value);
                })
                .containsExactlyInAnyOrder(
                        "stub.domain.model -> stub.domain.model.relation.annotation",
                        "stub.domain.model.relation -> stub.domain.model.relation.clz",
                        "stub.domain.model.relation -> stub.domain.model.relation.field",
                        "stub.domain.model.relation -> stub.domain.model.relation.method",
                        "stub.domain.model.relation -> stub.domain.model.relation.enumeration"
                );
    }

    @TestConfiguration
    static class Config {

        @Bean
        LocalProject localProject() {
            // 読み込む対象のソースを取得
            Path path = Paths.get(TestSupport.defaultPackageClassURI());
            return new LocalProject(new DefaultLayout(
                    path.toString(),
                    path.toString(),
                    // Mapper.xmlのためだが、ここではHitしなくてもテストのクラスパスから読めてしまう
                    "not/read/resources",
                    // TODO ソースディレクトリの安定した取得方法が欲しい
                    "not/read/sources"));
        }
    }
}
