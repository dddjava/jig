package org.dddjava.jig.domain.model.implementation.raw;

/**
 * バイナリソース
 */
public class BinarySource {

    ClassSources classSources;

    public BinarySource(ClassSources classSources) {
        this.classSources = classSources;
    }

    public ClassSources classSources() {
        return classSources;
    }
}
