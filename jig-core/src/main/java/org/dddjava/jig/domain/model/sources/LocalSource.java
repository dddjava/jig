package org.dddjava.jig.domain.model.sources;

import org.dddjava.jig.domain.model.sources.classsources.ClassFiles;
import org.dddjava.jig.domain.model.sources.javasources.JavaFilePaths;

/**
 * ローカルにあるJIGの情報源
 */
public record LocalSource(SourceBasePaths sourceBasePaths, JavaFilePaths javaFilePaths, ClassFiles classFiles) {

    public boolean emptyClassSources() {
        return classFiles.nothing();
    }

    public boolean emptyJavaSources() {
        return javaFilePaths.nothing();
    }
}
