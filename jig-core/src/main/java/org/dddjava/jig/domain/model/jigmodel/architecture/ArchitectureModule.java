package org.dddjava.jig.domain.model.jigmodel.architecture;

public interface ArchitectureModule {

    default String nodeLabel() {
        return toString();
    }
}
