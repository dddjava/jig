package org.dddjava.jig.domain.model.sources;

import org.dddjava.jig.domain.model.sources.classsources.ClassSources;
import org.dddjava.jig.domain.model.sources.javasources.JavaSources;

/**
 * 生ソース
 */
public class Sources {

    private final SourceBasePaths sourceBasePaths;
    private final JavaSources javaSources;
    private final ClassSources classSources;

    public Sources(SourceBasePaths sourceBasePaths, JavaSources javaSources, ClassSources classSources) {
        this.sourceBasePaths = sourceBasePaths;
        this.javaSources = javaSources;
        this.classSources = classSources;
    }

    public JavaSources javaSources() {
        return javaSources;
    }

    public boolean emptyClassSources() {
        return classSources.nothing();
    }

    public boolean emptyJavaSources() {
        return javaSources.nothing();
    }

    public ClassSources classSources() {
        return classSources;
    }

    public SourceBasePaths sourceBasePaths() {
        return sourceBasePaths;
    }
}
