package org.dddjava.jig.domain.model.sources;

import java.nio.file.Path;
import java.util.List;

/**
 * ソースのパス
 */
public class SourceBasePaths {

    SourceBasePath classSourceBasePaths;
    SourceBasePath sourceBasePath;

    public SourceBasePaths(SourceBasePath classSourceBasePaths, SourceBasePath sourceBasePath) {
        this.classSourceBasePaths = classSourceBasePaths;
        this.sourceBasePath = sourceBasePath;
    }

    public List<Path> classSourceBasePaths() {
        return classSourceBasePaths.paths();
    }

    public List<Path> javaSourceBasePaths() {
        return sourceBasePath.paths();
    }

    public SourceBasePaths merge(SourceBasePaths other) {
        return new SourceBasePaths(classSourceBasePaths.merge(other.classSourceBasePaths), sourceBasePath.merge(other.sourceBasePath));
    }
}
