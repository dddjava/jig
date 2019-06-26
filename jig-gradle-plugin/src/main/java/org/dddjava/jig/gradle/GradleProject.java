package org.dddjava.jig.gradle;

import org.dddjava.jig.domain.model.implementation.source.binary.BinarySourceLocations;
import org.dddjava.jig.domain.model.implementation.raw.raw.RawSourceLocations;
import org.dddjava.jig.domain.model.implementation.raw.raw.TextSourceLocations;
import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class GradleProject {
    final Project project;

    public GradleProject(Project project) {
        if (isNonJavaProject(project)) {
            throw new IllegalStateException("Java プラグインが適用されていません。");
        }
        this.project = project;
    }

    public Set<Path> classPaths() {
        return sourceSets().stream()
                .map(SourceSet::getOutput)
                .flatMap(output -> Stream.concat(output.getClassesDirs().getFiles().stream(), Stream.of(output.getResourcesDir())))
                .map(File::toPath)
                .collect(toSet());
    }

    public Set<Path> sourcePaths() {
        return sourceSets().stream()
                .flatMap(set -> set.getJava().getSrcDirs().stream())
                .map(File::toPath)
                .collect(toSet());
    }

    private List<SourceSet> sourceSets() {
        JavaPluginConvention convention = project.getConvention().findPlugin(JavaPluginConvention.class);
        List<SourceSet> sourceSets = convention.getSourceSets().stream()
                .filter(sourceSet -> !sourceSet.getName().equals(SourceSet.TEST_SOURCE_SET_NAME))
                .collect(Collectors.toList());
        return sourceSets;
    }


    public RawSourceLocations rawSourceLocations() {
        RawSourceLocations rawSourceLocations = allDependencyProjectsFrom(project)
                .map(GradleProject::new)
                .map(gradleProject ->
                        new RawSourceLocations(
                                new BinarySourceLocations(gradleProject.classPaths()),
                                new TextSourceLocations(gradleProject.sourcePaths())
                        ))
                .reduce(RawSourceLocations::merge)
                .orElseThrow(() -> new IllegalStateException("対象プロジェクトが見つかりません。"));
        return rawSourceLocations;
    }

    private Stream<Project> allDependencyProjectsFrom(Project root) {
        if (isNonJavaProject(root)) {
            return Stream.empty();
        }

        DependencySet dependencies = root.getConfigurations().getByName("compile").getAllDependencies();
        Stream<Project> descendantStream = dependencies.stream()
                .filter(dependency -> ProjectDependency.class.isAssignableFrom(dependency.getClass()))
                .map(ProjectDependency.class::cast)
                .map(ProjectDependency::getDependencyProject)
                .flatMap(this::allDependencyProjectsFrom);

        return Stream.concat(Stream.of(root), descendantStream);
    }

    private boolean isNonJavaProject(Project root) {
        return root.getConvention().findPlugin(JavaPluginConvention.class) == null;
    }
}
