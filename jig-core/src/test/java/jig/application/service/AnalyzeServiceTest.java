package jig.application.service;

import jig.domain.model.identifier.Identifier;
import jig.domain.model.japanasename.JapaneseNameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import stub.ClassJavadocStub;
import stub.MethodJavadocStub;
import stub.NotJavadocStub;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
class AnalyzeServiceTest {

    @Autowired
    JapaneseNameRepository japaneseNameRepository;

    @Test
    void 通常のJavadocを拾う() {
        assertThat(japaneseNameRepository.get(new Identifier(ClassJavadocStub.class)).value())
                .isEqualTo("クラスのJavadoc");
    }

    @Test
    void メソッドのJavadocを拾わない() {
        assertThat(japaneseNameRepository.get(new Identifier(MethodJavadocStub.class)).value())
                .isEqualTo("");
    }

    @Test
    void Javadocじゃないコメントを拾わない() {
        assertThat(japaneseNameRepository.get(new Identifier(NotJavadocStub.class)).value())
                .isEqualTo("");
    }

    @Test
    void defaultPackageClass() {
        assertThat(japaneseNameRepository.get(new Identifier("DefaultPackageClass")).value())
                .isEqualTo("デフォルトパッケージにあるクラス");
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