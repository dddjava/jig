package jig.application.service;

import jig.domain.model.identifier.namespace.PackageIdentifier;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.project.ProjectLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import stub.domain.model.ClassJavadocStub;
import stub.domain.model.MethodJavadocStub;
import stub.domain.model.NotJavadocStub;

import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@TestPropertySource(properties = {"directory.classes=build/classes/java/test", "directory.sources=src/test/java"})
@ExtendWith(SpringExtension.class)
class AnalyzeServiceTest {

    @Autowired
    JapaneseNameRepository japaneseNameRepository;

    @ParameterizedTest
    @MethodSource
    void 和名取得(TypeIdentifier typeIdentifier, String comment) {
        assertThat(japaneseNameRepository.get(typeIdentifier).value())
                .isEqualTo(comment);
    }

    static Stream<Arguments> 和名取得() {
        return Stream.of(
                Arguments.of(new TypeIdentifier(ClassJavadocStub.class), "クラスのJavadoc"),
                Arguments.of(new TypeIdentifier(MethodJavadocStub.class), ""),
                Arguments.of(new TypeIdentifier(NotJavadocStub.class), ""),
                Arguments.of(new TypeIdentifier("DefaultPackageClass"), "デフォルトパッケージにあるクラス")
        );
    }

    @Test
    void パッケージ和名() {
        assertThat(japaneseNameRepository.get(new PackageIdentifier("stub")).value())
                .isEqualTo("テストで使用するスタブたち");
    }

    @TestConfiguration
    static class Config {

        @Autowired
        AnalyzeService sut;

        @EventListener
        void hoge(ContextRefreshedEvent event) {
            sut.importProject(new ProjectLocation(Paths.get("")));
        }
    }
}