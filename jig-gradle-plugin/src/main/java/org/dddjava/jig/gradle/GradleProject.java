package org.dddjava.jig.gradle;

import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class GradleProject {
    final Project project;

    public GradleProject(Project project) {
        if (isNonJavaProject(project)) {
            throw new IllegalStateException("Java プラグインが適用されていません。");
        }
        this.project = project;
    }

    public Set<Path> classPaths() {
        SourceSet mainSourceSet = sourceSet();
        File classesOutputDir = mainSourceSet.getOutput().getClassesDir();
        File resourceOutputDir = mainSourceSet.getOutput().getResourcesDir();

        HashSet<Path> paths = new HashSet<>();
        paths.add(classesOutputDir.toPath());
        paths.add(resourceOutputDir.toPath());
        return paths;
    }

    public Set<Path> sourcePaths() {
        SourceSet mainSourceSet = sourceSet();
        File srcDir = mainSourceSet.getJava().getSrcDirs().iterator().next();
        HashSet<Path> paths = new HashSet<>();
        paths.add(srcDir.toPath());
        return paths;
    }

    private SourceSet sourceSet() {
        JavaPluginConvention convention = project.getConvention().findPlugin(JavaPluginConvention.class);
        return convention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
    }


    public GradleProjects allDependencyJavaProjects() {
        return allDependencyProjectsFrom(project)
                .map(GradleProject::new)
                .collect(GradleProjects.collector());
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
