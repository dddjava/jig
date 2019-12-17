package org.dddjava.jig.domain.model.jigsource.source.code.scalacode;

import java.nio.file.Path;

public class ScalaSourceFile {
    Path path;
    String fileName;

    public ScalaSourceFile(Path path) {
        this.path = path;
        this.fileName = path.getFileName().toString();
    }

    public String fineName() {
        return fileName;
    }

    public boolean isScala() {
        return fileName.endsWith(".scala");
    }
}
