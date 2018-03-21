package jig.application.service;

import jig.domain.model.identifier.Identifier;
import jig.domain.model.japanasename.JapaneseName;
import jig.domain.model.japanasename.JapaneseNameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import stub.ClassCommentStub;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
class AnalyzeServiceTest {

    @Autowired
    AnalyzeService sut;

    @Autowired
    JapaneseNameRepository japaneseNameRepository;

    @Test
    void test() {
        Path path = Paths.get("");
        sut.analyze(path);

        JapaneseName japaneseName = japaneseNameRepository.get(new Identifier(ClassCommentStub.class));
        assertThat(japaneseName.value()).isEqualTo("クラスコメントスタブ");
    }

    @ComponentScan("jig")
    @Configuration
    static class Config {
    }
}