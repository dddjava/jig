package org.dddjava.jig.application.usecase;

import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.application.service.ImplementationService;
import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;
import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.networks.PackageDependencies;
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

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = "jig.model.pattern = stub.domain.model.+")
public class ImplementationServiceTest {

    @Autowired
    DependencyService sut;

    @Autowired
    ImplementationService implementationService;
    @Autowired
    LocalProject localProject;

    @Test
    void パッケージ依存() {
        ProjectData projectData = implementationService.readProjectData(localProject);
        PackageDependencies packageDependencies = sut.packageDependencies(new PackageDepth(-1), projectData);

        // パッケージのリストアップ
        List<String> packageNames = packageDependencies.allPackages().stream()
                .map(packageIdentifier -> packageIdentifier.format(value -> value))
                // TODO 重複して入ってるけどどうしよう
                .distinct()
                .collect(Collectors.toList());
        assertThat(packageNames)
                .containsExactlyInAnyOrder(
                        "stub.domain.model",
                        "stub.domain.model.kind",
                        "stub.domain.model.relation",
                        "stub.domain.model.relation.foo",
                        "stub.domain.model.relation.test",
                        "stub.domain.model.type",
                        "stub.domain.model.type.fuga"
                );

        // パッケージの関連
        assertThat(packageDependencies.list())
                .extracting(dependency -> {
                    PackageIdentifier from = dependency.from();
                    PackageIdentifier to = dependency.to();
                    return from.format(value -> value) + " -> " + to.format(value -> value);
                })
                .containsExactlyInAnyOrder(
                        "stub.domain.model -> stub.domain.model.relation.test",
                        "stub.domain.model.relation -> stub.domain.model.relation.test",
                        "stub.domain.model.relation -> stub.domain.model.relation.foo",
                        "stub.domain.model.relation.foo -> stub.domain.model.relation.test"
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
