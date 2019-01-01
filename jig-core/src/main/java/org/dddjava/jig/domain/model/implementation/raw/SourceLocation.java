package org.dddjava.jig.domain.model.implementation.raw;

import java.nio.file.Path;

/**
 * ソースの場所
 */
public class SourceLocation {

    String value;

    public SourceLocation(String value) {
        this.value = value;
    }

    public SourceLocation() {
        this("");
    }

    public SourceLocation(Path path) {
        this(path.toAbsolutePath().toString());
    }
}
