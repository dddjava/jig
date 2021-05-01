package org.dddjava.jig.domain.model.jigmodel.architectures;

public interface ArchitectureModule {

    default String nodeLabel() {
        return toString();
    }
}
