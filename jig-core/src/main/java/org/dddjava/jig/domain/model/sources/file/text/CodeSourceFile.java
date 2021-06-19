package org.dddjava.jig.domain.model.sources.file.text;

import org.dddjava.jig.domain.model.sources.file.text.javacode.JavaSourceFile;
import org.dddjava.jig.domain.model.sources.file.text.kotlincode.KotlinSourceFile;
import org.dddjava.jig.domain.model.sources.file.text.scalacode.ScalaSourceFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * コードのファイル
 */
public class CodeSourceFile {

    Path path;

    public CodeSourceFile(Path path) {
        this.path = path;
    }

    public JavaSourceFile asJava() {
        return new JavaSourceFile(path);
    }

    public KotlinSourceFile asKotlin() {
        return new KotlinSourceFile(path);
    }

    public ScalaSourceFile asScala() {
        return new ScalaSourceFile(path);
    }

    public TextSourceType textSourceType() {
        String fileName = path.getFileName().toString();
        return TextSourceType.from(fileName);
    }

    byte[] getAllBytes() throws IOException {
        return Files.readAllBytes(path);
    }
}
