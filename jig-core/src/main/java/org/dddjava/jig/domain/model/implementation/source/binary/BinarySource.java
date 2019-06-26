package org.dddjava.jig.domain.model.implementation.source.binary;

import org.dddjava.jig.domain.model.implementation.raw.sourcelocation.SourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * バイナリソース
 */
public class BinarySource {

    SourceLocation sourceLocation;
    ClassSources classSources;

    public BinarySource(SourceLocation sourceLocation, ClassSources classSources) {
        this.sourceLocation = sourceLocation;
        this.classSources = classSources;
    }

    public BinarySource() {
        this(new SourceLocation(), new ClassSources());
    }

    public ClassSources classSources() {
        return classSources;
    }

    BinarySource merge(BinarySource other) {
        List<ClassSource> list = new ArrayList<>(classSources.list());
        list.addAll(other.classSources.list());
        return new BinarySource(
                new SourceLocation(),
                new ClassSources(list));
    }

    public SourceLocation sourceLocation() {
        return sourceLocation;
    }
}
