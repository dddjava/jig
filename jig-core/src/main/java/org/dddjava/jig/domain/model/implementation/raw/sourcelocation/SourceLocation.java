package org.dddjava.jig.domain.model.implementation.raw.sourcelocation;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public URI uri() {
        return Paths.get(value).toUri();
    }

    public String value() {
        return value;
    }
}
