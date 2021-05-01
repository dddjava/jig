package org.dddjava.jig.domain.model.models.architectures;

public interface ArchitectureModule {

    default String nodeLabel() {
        return toString();
    }
}
