package jig.application.service;

import jig.domain.model.identifier.Identifier;
import jig.domain.model.japanasename.JapaneseNameRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import stub.ClassJavadocStub;
import stub.MethodJavadocStub;
import stub.NotJavadocStub;

import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
class AnalyzeServiceTest {

    @Autowired
    JapaneseNameRepository japaneseNameRepository;

    @ParameterizedTest
    @MethodSource
    void 和名取得(Identifier identifier, String comment) {
        assertThat(japaneseNameRepository.get(identifier).value())
                .isEqualTo(comment);
    }

    static Stream<Arguments> 和名取得() {
        return Stream.of(
                Arguments.of(new Identifier(ClassJavadocStub.class), "クラスのJavadoc"),
                Arguments.of(new Identifier(MethodJavadocStub.class), ""),
                Arguments.of(new Identifier(NotJavadocStub.class), ""),
                Arguments.of(new Identifier("DefaultPackageClass"), "デフォルトパッケージにあるクラス"),
                Arguments.of(new Identifier("stub"), "テストで使用するスタブたち")
        );
    }

    @TestConfiguration
    static class Config {

        @Autowired
        AnalyzeService sut;

        @EventListener
        void hoge(ContextRefreshedEvent event) {
            sut.analyze(Paths.get(""));
        }
    }
}