package jig.application.usecase;

import jig.application.service.DependencyService;
import jig.domain.model.identifier.namespace.PackageIdentifier;
import jig.domain.model.project.SourceFactory;
import jig.domain.model.relation.dependency.PackageDependencies;
import jig.infrastructure.JigPaths;
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
public class ImportLocalProjectServiceTest {

    @Autowired
    DependencyService sut;

    @Autowired
    ImportLocalProjectService importLocalProjectService;

    @Test
    void パッケージ依存() {
        importLocalProjectService.importProject();
        PackageDependencies packageDependencies = sut.packageDependencies();

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
        SourceFactory sourceFactory() {
            // 読み込む対象のソースを取得
            Path path = Paths.get(TestSupport.defaultPackageClassURI());
            JigPaths jigPaths = new JigPaths(
                    path.toString(),
                    path.toString(),
                    // Mapper.xmlのためだが、ここではHitしなくてもテストのクラスパスから読めてしまう
                    "not/read/resources",
                    // TODO ソースディレクトリの安定した取得方法が欲しい
                    "not/read/sources");
            return jigPaths;
        }
    }
}
