package org.dddjava.jig.domain.model.sources;

import org.dddjava.jig.domain.model.sources.classsources.ClassSources;
import org.dddjava.jig.domain.model.sources.javasources.JavaSources;

/**
 * 生ソース
 */
public record Sources(SourceBasePaths sourceBasePaths, JavaSources javaSources, ClassSources classSources) {

    public boolean emptyClassSources() {
        return classSources.nothing();
    }

    public boolean emptyJavaSources() {
        return javaSources.nothing();
    }
}
