package org.dddjava.jig.domain.model.implementation.bytecode;

import java.nio.file.Path;

/**
 * モデルが実装されたソース
 */
public class ImplementationSource {
    private final Path path;

    public ImplementationSource(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }
}
