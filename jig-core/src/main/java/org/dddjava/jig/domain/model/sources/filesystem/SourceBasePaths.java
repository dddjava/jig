package org.dddjava.jig.domain.model.sources.filesystem;

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
}
