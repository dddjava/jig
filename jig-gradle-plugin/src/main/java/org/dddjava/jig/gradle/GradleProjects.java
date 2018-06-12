package org.dddjava.jig.gradle;

import org.dddjava.jig.infrastructure.Layout;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GradleProjects implements Layout {
    final Set<GradleProject> values;

    GradleProjects(Set<GradleProject> values) {
        this.values = values;
    }

    static Collector<GradleProject, ?, GradleProjects> collector() {
        return Collectors.collectingAndThen(Collectors.toSet(), GradleProjects::new);
    }

    @Override
    public Path[] extractClassPath() {
        return extractLayoutClassPath()
                .filter(path -> Files.exists(path))
                .toArray(Path[]::new);
    }

    @Override
    public Path[] extractSourcePath() {
        return extractLayoutSourcePath()
                .filter(path -> Files.exists(path))
                .toArray(Path[]::new);
    }

    public Stream<Path> extractLayoutClassPath() {
        return values.stream()
                .flatMap(project -> project.classPaths().stream());
    }

    public Stream<Path> extractLayoutSourcePath() {
        return values.stream()
                .flatMap(project -> project.sourcePaths().stream());
    }
}
