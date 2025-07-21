package org.dddjava.jig.gradle;

import org.dddjava.jig.domain.model.sources.SourceBasePath;
import org.dddjava.jig.domain.model.sources.SourceBasePaths;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class GradleProject {
    private static final Logger logger = LoggerFactory.getLogger(GradleProject.class);

    final Project project;

    public GradleProject(Project project) {
        if (isNonJavaProject(project)) {
            throw new IllegalStateException("Java プラグインが適用されていません。");
        }
        this.project = project;
    }

    public Set<Path> classPaths() {
        return sourceSets()
                .map(SourceSet::getOutput)
                .flatMap(output -> Stream.concat(output.getClassesDirs().getFiles().stream(), Stream.of(output.getResourcesDir())))
                .map(File::toPath)
                .collect(toSet());
    }

    public Set<Path> sourcePaths() {
        return sourceSets()
                .flatMap(set -> set.getJava().getSrcDirs().stream())
                .map(File::toPath)
                .collect(toSet());
    }

    private boolean isNonJavaProject(Project root) {
        return findJavaPluginExtension(root).isEmpty();
    }

    private static Optional<JavaPluginExtension> findJavaPluginExtension(Project root) {
        return Optional.ofNullable(root.getExtensions().findByType(JavaPluginExtension.class));
    }

    private Stream<SourceSet> sourceSets() {
        return findJavaPluginExtension(project)
                .stream()
                .flatMap(extension -> extension.getSourceSets().stream()
                        .filter(sourceSet -> !sourceSet.getName().equals(SourceSet.TEST_SOURCE_SET_NAME)));
    }

    public SourceBasePaths rawSourceLocations() {
        return allDependencyProjectsFrom(project)
                .map(GradleProject::new)
                .map(gradleProject ->
                        new SourceBasePaths(
                                new SourceBasePath(gradleProject.classPaths()),
                                new SourceBasePath(gradleProject.sourcePaths())
                        ))
                .reduce(SourceBasePaths::merge)
                .orElseThrow(() -> new IllegalStateException("対象プロジェクトが見つかりません。"));
    }

    private Stream<Project> allDependencyProjectsFrom(Project currentProject) {
        if (isNonJavaProject(currentProject)) {
            return Stream.empty();
        }

        Stream<Project> descendantStream =
                currentProject.getConfigurations().stream()
                        .filter(configuration -> {
                            String name = configuration.getName();
                            return "implementation".equals(name);
                        })
                        .map(Configuration::getAllDependencies)
                        .flatMap(DependencySet::stream)
                        .filter(dependency -> ProjectDependency.class.isAssignableFrom(dependency.getClass()))
                        .map(ProjectDependency.class::cast)
                        .map(ProjectDependency::getDependencyProject)
                        .flatMap(this::allDependencyProjectsFrom);

        return Stream.concat(Stream.of(currentProject), descendantStream);
    }
}
