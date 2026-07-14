package org.dddjava.jig.gradle;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class GradleProject {
    final Project project;

    public GradleProject(Project project) {
        this.project = project;
    }

    public static boolean isJavaProject(Project project) {
        return findJavaPluginExtension(project).isPresent();
    }

    private Stream<FileCollection> classOutputs() {
        return sourceSets().map(SourceSet::getOutput);
    }

    private Stream<FileCollection> sourceDirectories() {
        return sourceSets().map(set -> set.getJava().getSourceDirectories());
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

    /**
     * 依存プロジェクトを含むすべてのクラス出力を返す。
     * FileCollection のまま返すことでコンパイルタスクへの依存（builtBy）を保つ。
     */
    public List<FileCollection> allClassOutputs() {
        return allDependencyProjectsFrom(project)
                .map(GradleProject::new)
                .flatMap(GradleProject::classOutputs)
                .toList();
    }

    /**
     * 依存プロジェクトを含むすべてのソースディレクトリを返す
     */
    public List<FileCollection> allSourceDirectories() {
        return allDependencyProjectsFrom(project)
                .map(GradleProject::new)
                .flatMap(GradleProject::sourceDirectories)
                .toList();
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
                        // implementation は api を extendsFrom しているため、getAllDependencies で api 宣言分も含まれる
                        .map(Configuration::getAllDependencies)
                        .flatMap(DependencySet::stream)
                        .filter(dependency -> ProjectDependency.class.isAssignableFrom(dependency.getClass()))
                        .map(ProjectDependency.class::cast)
                        .map(this::resolveDependencyProject)
                        .flatMap(this::allDependencyProjectsFrom);

        return Stream.concat(Stream.of(currentProject), descendantStream);
    }

    private Project resolveDependencyProject(ProjectDependency projectDependency) {
        return project.project(projectDependency.getPath());
    }
}
