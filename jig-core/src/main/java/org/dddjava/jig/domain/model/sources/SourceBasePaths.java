package org.dddjava.jig.domain.model.sources;

import org.dddjava.jig.domain.model.sources.classsources.ClassSourceBasePaths;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceBasePaths;

import java.nio.file.Path;
import java.util.List;

/**
 * ソースのパス
 */
public class SourceBasePaths {

    ClassSourceBasePaths classSourceBasePaths;
    JavaSourceBasePaths javaSourceBasePaths;

    public SourceBasePaths(ClassSourceBasePaths classSourceBasePaths, JavaSourceBasePaths javaSourceBasePaths) {
        this.classSourceBasePaths = classSourceBasePaths;
        this.javaSourceBasePaths = javaSourceBasePaths;
    }

    public List<Path> classSourceBasePaths() {
        return classSourceBasePaths.paths();
    }

    public List<Path> javaSourceBasePaths() {
        return javaSourceBasePaths.paths();
    }

    public SourceBasePaths merge(SourceBasePaths other) {
        return new SourceBasePaths(classSourceBasePaths.merge(other.classSourceBasePaths), javaSourceBasePaths.merge(other.javaSourceBasePaths));
    }
}
