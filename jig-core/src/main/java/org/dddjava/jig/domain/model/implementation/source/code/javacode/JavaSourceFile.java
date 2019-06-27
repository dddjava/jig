package org.dddjava.jig.domain.model.implementation.source.code.javacode;

import java.nio.file.Path;

public class JavaSourceFile {

    String fileName;
    Path path;

    public JavaSourceFile(Path path) {
        this.path = path;
        this.fileName = path.getFileName().toString();
    }

    public boolean isJava() {
        return fileName.endsWith(".java");
    }

    public boolean isPackageInfo() {
        return fileName.endsWith("package-info.java");
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
