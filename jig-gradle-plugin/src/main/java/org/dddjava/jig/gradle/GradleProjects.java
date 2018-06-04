package org.dddjava.jig.gradle;

import org.dddjava.jig.infrastructure.Origin;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class GradleProjects implements Origin {
    final Set<GradleProject> values;

    GradleProjects(Set<GradleProject> values) {
        this.values = values;
    }

    static Collector<GradleProject, ?, GradleProjects> collector() {
        return Collectors.collectingAndThen(Collectors.toSet(), GradleProjects::new);
    }

    @Override
    public Path[] extractClassPath() {
        return values.stream()
                .flatMap(project -> project.classPaths().stream())
                .toArray(Path[]::new);
    }

    @Override
    public Path[] extractSourcePath() {
        return values.stream()
                .flatMap(project -> project.sourcePaths().stream())
                .toArray(Path[]::new);
    }
}
