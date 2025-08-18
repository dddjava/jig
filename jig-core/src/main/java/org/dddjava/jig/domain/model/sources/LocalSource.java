package org.dddjava.jig.domain.model.sources;

import org.dddjava.jig.domain.model.sources.classsources.ClassFilePaths;
import org.dddjava.jig.domain.model.sources.javasources.JavaFilePaths;

/**
 * ローカルにあるJIGの情報源
 */
public record LocalSource(SourceBasePaths sourceBasePaths, JavaFilePaths javaFilePaths, ClassFilePaths classFilePaths) {

    public boolean emptyClassSources() {
        return classFilePaths.nothing();
    }

    public boolean emptyJavaSources() {
        return javaFilePaths.nothing();
    }
}
