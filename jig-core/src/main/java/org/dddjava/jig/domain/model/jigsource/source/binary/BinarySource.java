package org.dddjava.jig.domain.model.jigsource.source.binary;

import java.util.ArrayList;
import java.util.List;

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
        List<ClassSource> list = new ArrayList<>(classSources.list());
        list.addAll(other.classSources.list());
        return new BinarySource(
                new BinarySourceLocation(),
                new ClassSources(list));
    }

    public BinarySourceLocation sourceLocation() {
        return binarySourceLocation;
    }
}
