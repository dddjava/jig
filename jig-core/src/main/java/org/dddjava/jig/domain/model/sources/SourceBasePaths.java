package org.dddjava.jig.domain.model.sources;

import java.nio.file.Path;
import java.util.List;

/**
 * ソースのパス
 */
public class SourceBasePaths {

    SourceBasePath classFileBasePath;
    SourceBasePath javaFileBasePath;

    public SourceBasePaths(SourceBasePath classFileBasePath, SourceBasePath javaFileBasePath) {
        this.classFileBasePath = classFileBasePath;
        this.javaFileBasePath = javaFileBasePath;
    }

    public List<Path> classSourceBasePaths() {
        return classFileBasePath.paths();
    }

    public List<Path> javaSourceBasePaths() {
        return javaFileBasePath.paths();
    }

    public SourceBasePaths merge(SourceBasePaths other) {
        return new SourceBasePaths(classFileBasePath.merge(other.classFileBasePath), javaFileBasePath.merge(other.javaFileBasePath));
    }
}
