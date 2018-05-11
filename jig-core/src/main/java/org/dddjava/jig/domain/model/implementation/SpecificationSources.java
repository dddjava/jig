package org.dddjava.jig.domain.model.implementation;

import java.util.List;

public class SpecificationSources {
    private final List<SpecificationSource> sources;

    public SpecificationSources(List<SpecificationSource> sources) {
        this.sources = sources;
    }

    public List<SpecificationSource> list() {
        return sources;
    }

    public boolean notFound() {
        return sources.isEmpty();
    }
}
