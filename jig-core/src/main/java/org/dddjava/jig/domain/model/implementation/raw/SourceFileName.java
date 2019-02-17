package org.dddjava.jig.domain.model.implementation.raw;

import java.nio.file.Path;

/**
 * ファイル名
 */
public class SourceFileName {

    String value;

    public SourceFileName(String value) {
        this.value = value;
    }

    public SourceFileName(Path file) {
        this(file.getFileName().toString());
    }

    public boolean isJava() {
        return value.endsWith(".java");
    }

    public boolean isPackageInfo() {
        return value.endsWith("package-info.java");
    }
}
