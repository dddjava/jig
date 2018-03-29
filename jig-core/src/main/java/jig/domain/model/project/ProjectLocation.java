package jig.domain.model.project;

import java.nio.file.Path;

public class ProjectLocation {
    private final Path value;

    public ProjectLocation(Path value) {
        this.value = value;
    }

    public Path toPath() {
        return value;
    }
}
