package org.dddjava.jig.gradle;

import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class GradleProject {
    final Project project;

    public GradleProject(Project project) {
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
        if (convention == null) throw new IllegalStateException("Java プラグインが適用されていません。");
        return convention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
    }


    public GradleProjects allDependencyJavaProjects() {
        Set<Project> projects = allDependencyJavaProjectsFrom(project);
        return projects.stream()
                .map(GradleProject::new).collect(GradleProjects.collector());
    }

    private Set<Project> allDependencyJavaProjectsFrom(Project root) {
        Set<Project> allDepends = new HashSet<>();
        if (isJavaProject(root)) {
            allDepends.add(root);
        }

        DependencySet dependencies = root.getConfigurations().getByName("compile").getAllDependencies();

        Set<Project> children = dependencies.stream()
                .filter(dependency -> ProjectDependency.class.isAssignableFrom(dependency.getClass()))
                .map(ProjectDependency.class::cast)
                .map(ProjectDependency::getDependencyProject)
                .filter(this::isJavaProject)
                .collect(toSet());

        if (children.isEmpty()) return allDepends;

        Set<Project> descendants = children.stream()
                .flatMap(project -> allDependencyJavaProjectsFrom(project).stream())
                .collect(toSet());
        allDepends.addAll(descendants);

        return allDepends;
    }

    private boolean isJavaProject(Project root) {
        return root.getConvention().findPlugin(JavaPluginConvention.class) != null;
    }
}
