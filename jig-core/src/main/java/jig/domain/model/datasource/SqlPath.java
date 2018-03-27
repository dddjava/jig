package jig.domain.model.datasource;

import java.nio.file.Path;

public class SqlPath {
    private final Path value;

    public SqlPath(Path value) {
        this.value = value;
    }

    public Path getValue() {
        return value;
    }
}
