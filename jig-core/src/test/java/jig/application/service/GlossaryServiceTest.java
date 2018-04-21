package jig.application.service;

import jig.domain.model.identifier.namespace.PackageIdentifier;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.infrastructure.JigPaths;
import jig.infrastructure.javaparser.JavaparserJapaneseReader;
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

class GlossaryServiceTest {

    GlossaryService sut = new GlossaryService(
            new JavaparserJapaneseReader(),
            new OnMemoryJapaneseNameRepository());

    @Test
    void パッケージ和名取得() {
        JigPaths jigPath = new JigPaths(TestSupport.getModuleRootPath().toString(), "dummy", "dummy", "src/test/java");

        sut.importJapanese(jigPath.getPackageNameSources());

        assertThat(sut.japaneseNameFrom(new PackageIdentifier("stub")).value())
                .isEqualTo("テストで使用するスタブたち");
    }

    @ParameterizedTest
    @MethodSource
    void クラス和名取得(TypeIdentifier typeIdentifier, String comment) {
        JigPaths jigPath = new JigPaths(TestSupport.getModuleRootPath().toString(), "dummy", "dummy", "src/test/java");

        sut.importJapanese(jigPath.getTypeNameSources());

        assertThat(sut.japaneseNameFrom(typeIdentifier).value())
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