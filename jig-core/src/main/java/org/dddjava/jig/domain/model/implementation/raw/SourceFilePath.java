package org.dddjava.jig.domain.model.implementation.raw;

import java.nio.file.Path;

/**
 * ファイル名
 */
public class SourceFilePath {

    Path path;
    String fileName;

    public SourceFilePath(Path path) {
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
