package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.sources.file.text.TextSource;
import org.dddjava.jig.domain.model.sources.jigreader.TextSourceModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled("ファイルの読み方が環境依存/非Java17環境で17ソースを読ませるテストのためdisableにしておく")
class ClassReaderTest {

    @Test
    void record() throws Exception {
        ParserConfiguration configuration = StaticJavaParser.getConfiguration();
        configuration.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);

        ClassReader sut = new ClassReader();
        Path path = Paths.get("./src/test/resources/jdk17/MyRecord.java").toAbsolutePath();
        TextSourceModel textSourceModel = sut.read(new TextSource(path).toReadableTextSource());
        ClassComment actual = textSourceModel.classCommentList().get(0);

        assertEquals("レコードのJavadocコメント", actual.asText());
    }
}