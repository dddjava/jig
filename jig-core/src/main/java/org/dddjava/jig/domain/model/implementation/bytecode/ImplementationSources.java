package org.dddjava.jig.domain.model.implementation.bytecode;

import java.util.List;

/**
 * モデルが実装されたソース一式
 */
public class ImplementationSources {
    private final List<ImplementationSource> sources;

    public ImplementationSources(List<ImplementationSource> sources) {
        this.sources = sources;
    }

    public List<ImplementationSource> list() {
        return sources;
    }

    public boolean notFound() {
        return sources.isEmpty();
    }
}
