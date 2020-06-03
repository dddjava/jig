package org.dddjava.jig.domain.model.jigsource.file.text.kotlincode;

import java.nio.file.Path;

public class KotlinSourceFile {
    Path path;
    String fileName;

    public KotlinSourceFile(Path path) {
        this.path = path;
        this.fileName = path.getFileName().toString();
    }

    public String fineName() {
        return fileName;
    }

    public boolean isKotlin() {
        return fileName.endsWith(".kt");
    }
}
