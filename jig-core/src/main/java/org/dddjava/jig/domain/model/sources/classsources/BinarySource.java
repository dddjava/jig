package org.dddjava.jig.domain.model.sources.classsources;

/**
 * バイナリソース
 */
public class BinarySource {

    BinarySourceLocation binarySourceLocation;
    ClassSources classSources;

    public BinarySource(BinarySourceLocation binarySourceLocation, ClassSources classSources) {
        this.binarySourceLocation = binarySourceLocation;
        this.classSources = classSources;
    }

    public BinarySource() {
        this(new BinarySourceLocation(), new ClassSources());
    }

    public ClassSources classSources() {
        return classSources;
    }

    BinarySource merge(BinarySource other) {
        return new BinarySource(
                new BinarySourceLocation(),
                classSources.merge(other.classSources));
    }

    public BinarySourceLocation sourceLocation() {
        return binarySourceLocation;
    }
}
