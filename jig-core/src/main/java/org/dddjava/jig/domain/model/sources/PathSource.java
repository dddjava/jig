package org.dddjava.jig.domain.model.sources;

import org.dddjava.jig.domain.model.sources.classsources.ClassFilePaths;
import org.dddjava.jig.domain.model.sources.javasources.JavaFilePaths;

/**
 * PathによるJIGの情報源
 */
public record PathSource(SourceBasePaths sourceBasePaths, JavaFilePaths javaFilePaths, ClassFilePaths classFilePaths) {

    public boolean emptyClassSources() {
        return classFilePaths.nothing();
    }

    public boolean emptyJavaSources() {
        return javaFilePaths.nothing();
    }
}
