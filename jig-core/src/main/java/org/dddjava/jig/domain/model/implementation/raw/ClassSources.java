package org.dddjava.jig.domain.model.implementation.raw;

import java.util.List;

/**
 * classソース一覧
 */
public class ClassSources {
    private final List<ClassSource> sources;

    public ClassSources(List<ClassSource> sources) {
        this.sources = sources;
    }

    public List<ClassSource> list() {
        return sources;
    }

    public boolean notFound() {
        return sources.isEmpty();
    }
}
