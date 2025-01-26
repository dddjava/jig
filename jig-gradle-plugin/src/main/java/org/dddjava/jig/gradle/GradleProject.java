package org.dddjava.jig.gradle;

import org.dddjava.jig.domain.model.sources.CodeSourcePaths;
import org.dddjava.jig.domain.model.sources.SourcePaths;
import org.dddjava.jig.domain.model.sources.classsources.BinarySourcePaths;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

    /**
     * ConventionやJavaPluginConventionは非推奨となっておりGradle8で削除されるが、
     * Gradle6.xでは新しい方法が使用できないので警告抑止して使っておく。
     */
    @SuppressWarnings("deprecation")
    private boolean isNonJavaProject(Project root) {
        return root.getConvention().findPlugin(JavaPluginConvention.class) == null;
    }

    /**
     * ConventionやJavaPluginConventionは非推奨となっておりGradle8で削除されるが、
     * Gradle6.xでは新しい方法が使用できないので警告抑止して使っておく。
     */
    @SuppressWarnings("deprecation")
    private List<SourceSet> sourceSets() {
        JavaPluginConvention convention = project.getConvention().getPlugin(JavaPluginConvention.class);
        List<SourceSet> sourceSets = convention.getSourceSets().stream()
                .filter(sourceSet -> !sourceSet.getName().equals(SourceSet.TEST_SOURCE_SET_NAME))
                .collect(Collectors.toList());
        return sourceSets;
    }

    public SourcePaths rawSourceLocations() {
        SourcePaths sourcePaths = allDependencyProjectsFrom(project)
                .map(GradleProject::new)
                .map(gradleProject ->
                        new SourcePaths(
                                new BinarySourcePaths(gradleProject.classPaths()),
                                new CodeSourcePaths(gradleProject.sourcePaths())
                        ))
                .reduce(SourcePaths::merge)
                .orElseThrow(() -> new IllegalStateException("対象プロジェクトが見つかりません。"));
        return sourcePaths;
    }

    private Stream<Project> allDependencyProjectsFrom(Project root) {
        if (isNonJavaProject(root)) {
            return Stream.empty();
        }

        Stream<Project> descendantStream =
                root.getConfigurations().stream()
                        .filter(this::implementationConfiguration)
                        .map(Configuration::getAllDependencies)
                        .flatMap(DependencySet::stream)
                        .filter(dependency -> ProjectDependency.class.isAssignableFrom(dependency.getClass()))
                        .map(ProjectDependency.class::cast)
                        .map(ProjectDependency::getDependencyProject)
                        .flatMap(this::allDependencyProjectsFrom);

        return Stream.concat(Stream.of(root), descendantStream);
    }

    private boolean implementationConfiguration(Configuration configuration) {
        String name = configuration.getName();
        if ("implementation".equals(name)) return true;

        // Gradle7でcompileスコープが削除されたが、後方互換のため対応しておく
        if ("compile".equals(name)) {
            logger.warn("Gradle7で削除された compile が使用されています。JIGでもこちらの対応は今後削除される予定です。 https://docs.gradle.org/current/userguide/upgrading_version_6.html#sec:configuration_removal");
            return true;
        }

        return false;
    }
}
