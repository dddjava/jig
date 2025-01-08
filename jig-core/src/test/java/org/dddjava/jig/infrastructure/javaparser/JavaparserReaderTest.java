package org.dddjava.jig.infrastructure.javaparser;

import com.github.javaparser.ParseProblemException;
import org.dddjava.jig.domain.model.data.classes.type.ClassComment;
import org.dddjava.jig.domain.model.sources.jigfactory.TextSourceModel;
import org.dddjava.jig.infrastructure.configuration.JigProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaparserReaderTest {

    @EnabledOnJre(JRE.JAVA_17)
    @Test
    void Java17のrecordが読める() throws Exception {
        var sut = new JavaparserReader(new JigProperties(List.of(), "", Path.of("")));
        try (InputStream inputStream = Objects.requireNonNull(this.getClass().getResource("/jdk17/MyRecord.java")).openStream()) {
            TextSourceModel textSourceModel = sut.read(inputStream);
            ClassComment actual = textSourceModel.classCommentList().get(0);
            assertEquals("レコードのJavadocコメント", actual.asText());
        }
    }

    @EnabledForJreRange(max = JRE.JAVA_16)
    @Test
    void Java16まではJava17のrecordが読めない() throws Exception {
        var sut = new JavaparserReader(new JigProperties(List.of(), "", Path.of("")));
        try (InputStream inputStream = Objects.requireNonNull(this.getClass().getResource("/jdk17/MyRecord.java")).openStream()) {

            assertThrows(ParseProblemException.class, () -> sut.read(inputStream));
        }
    }
}