package org.dddjava.jig.domain.model.sources;

import java.nio.file.Path;
import java.util.List;

/**
 * ソースのパス
 */
public record SourceBasePaths(SourceBasePath classFileBasePath, SourceBasePath javaFileBasePath) {

    public List<Path> classSourceBasePaths() {
        return classFileBasePath.pathList();
    }

    public List<Path> javaSourceBasePaths() {
        return javaFileBasePath.pathList();
    }

    public SourceBasePaths merge(SourceBasePaths other) {
        return new SourceBasePaths(classFileBasePath.merge(other.classFileBasePath), javaFileBasePath.merge(other.javaFileBasePath));
    }
}
