package org.dddjava.jig.domain.model.implementation;

import java.nio.file.Path;

public class SpecificationSource {
    private final Path path;

    public SpecificationSource(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }
}
