package jig.infrastructure.javaparser;

import jig.domain.model.identifier.namespace.PackageIdentifier;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.japanese.JapaneseNameRepository;
import jig.domain.model.project.ProjectLocation;
import jig.infrastructure.JigPaths;
import jig.infrastructure.onmemoryrepository.OnMemoryJapaneseNameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import stub.domain.model.ClassJavadocStub;
import stub.domain.model.MethodJavadocStub;
import stub.domain.model.NotJavadocStub;
import testing.TestSupport;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JavaparserJapaneseReaderTest {

    @Test
    void パッケージ和名取得() {
        JapaneseNameRepository repository = new OnMemoryJapaneseNameRepository();
        JigPaths jigPath = new JigPaths("dummy", "dummy", "src/test/java");
        JavaparserJapaneseReader sut = new JavaparserJapaneseReader(repository, jigPath);

        ProjectLocation projectLocation = new ProjectLocation(TestSupport.getModuleRootPath());
        sut.readFrom(projectLocation);

        assertThat(repository.get(new PackageIdentifier("stub")).value())
                .isEqualTo("テストで使用するスタブたち");
    }

    @ParameterizedTest
    @MethodSource
    void クラス和名取得(TypeIdentifier typeIdentifier, String comment) {
        JapaneseNameRepository repository = new OnMemoryJapaneseNameRepository();
        JigPaths jigPath = new JigPaths("dummy", "dummy", "src/test/java");
        JavaparserJapaneseReader sut = new JavaparserJapaneseReader(repository, jigPath);

        ProjectLocation projectLocation = new ProjectLocation(TestSupport.getModuleRootPath());
        sut.readFrom(projectLocation);

        assertThat(repository.get(typeIdentifier).value())
                .isEqualTo(comment);
    }

    static Stream<Arguments> クラス和名取得() {
        return Stream.of(
                Arguments.of(new TypeIdentifier(ClassJavadocStub.class), "クラスのJavadoc"),
                Arguments.of(new TypeIdentifier(MethodJavadocStub.class), ""),
                Arguments.of(new TypeIdentifier(NotJavadocStub.class), ""),
                Arguments.of(new TypeIdentifier("DefaultPackageClass"), "デフォルトパッケージにあるクラス")
        );
    }

}