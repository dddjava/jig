package org.dddjava.jig.domain.model.sources;

import org.dddjava.jig.domain.model.sources.classsources.BinarySourcePaths;

import java.nio.file.Path;
import java.util.List;

/**
 * ソースのパス
 */
public class SourcePaths {

    BinarySourcePaths binarySourcePaths;
    CodeSourcePaths codeSourcePaths;

    public SourcePaths(BinarySourcePaths binarySourcePaths, CodeSourcePaths codeSourcePaths) {
        this.binarySourcePaths = binarySourcePaths;
        this.codeSourcePaths = codeSourcePaths;
    }

    public List<Path> classSourceBasePaths() {
        return binarySourcePaths.paths();
    }

    public List<Path> javaSourceBasePaths() {
        return codeSourcePaths.paths();
    }

    public SourcePaths merge(SourcePaths other) {
        return new SourcePaths(binarySourcePaths.merge(other.binarySourcePaths), codeSourcePaths.merge(other.codeSourcePaths));
    }
}
