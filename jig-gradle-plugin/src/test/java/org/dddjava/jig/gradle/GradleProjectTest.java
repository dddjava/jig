package org.dddjava.jig.gradle;

import org.assertj.core.api.SoftAssertions;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency;
import org.gradle.api.internal.project.DefaultProjectAccessListener;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class GradleProjectTest {

    @ParameterizedTest
    @MethodSource("fixtures")
    public void 依存関係にあるすべてのJavaPluginが適用されたプロジェクトのクラスパスとソースパスが取得できること(Fixture fixture) {
        GradleProjects gradleProjects = new GradleProject(fixture.root).allDependencyJavaProjects();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fixture.classPathContains(gradleProjects.extractLayoutClassPath().collect(Collectors.toSet()))).isTrue();
        softly.assertThat(fixture.sourcePathContains(gradleProjects.extractLayoutSourcePath().collect(Collectors.toSet()))).isTrue();
        softly.assertAll();
    }


    static List<Fixture> fixtures() {
        return Arrays.asList(
                new Fixture(singleJavaProject(),
                        new String[]{
                                "依存プロジェクトのないJavaプロジェクト/build/classes/java/main",
                                "依存プロジェクトのないJavaプロジェクト/build/resources/main"
                        },
                        new String[]{
                                "依存プロジェクトのないJavaプロジェクト/src/main/java"
                        }
                ),
                new Fixture(projectHasThreeTiersAndThreeJavaProjectDependencies(),
                        new String[]{
                                "3階層構造で依存Javaプロジェクトが２つあるJavaプロジェクト/build/classes/java/main",
                                "3階層構造で依存Javaプロジェクトが２つあるJavaプロジェクト/build/resources/main",
                                "javaChild/build/classes/java/main",
                                "javaChild/build/resources/main",
                                "javaGrandson/build/classes/java/main",
                                "javaGrandson/build/resources/main"
                        },
                        new String[]{
                                "3階層構造で依存Javaプロジェクトが２つあるJavaプロジェクト/src/main/java",
                                "javaChild/src/main/java",
                                "javaGrandson/src/main/java"
                        }
                ),
                new Fixture(multiSourceSetJavaProject(),
                        new String[]{
                                "複数のソースセットを持つJavaプロジェクト/build/classes/java/main",
                                "複数のソースセットを持つJavaプロジェクト/build/classes/java/sub",
                                "複数のソースセットを持つJavaプロジェクト/build/resources/main",
                                "複数のソースセットを持つJavaプロジェクト/build/resources/sub"
                        },
                        new String[]{
                                "複数のソースセットを持つJavaプロジェクト/src/main/java",
                                "複数のソースセットを持つJavaプロジェクト/src/sub/java"
                        }
                )
        );
    }

    static class Fixture {
        final Project root;
        final String[] classPathSuffixes;
        final String[] sourcePathSuffixes;

        Fixture(Project root, String[] classPathSuffixes, String[] sourcePathSuffixes) {
            this.root = root;
            this.classPathSuffixes = classPathSuffixes;
            this.sourcePathSuffixes = sourcePathSuffixes;
        }


        public boolean classPathContains(Set<Path> paths) {
            return Arrays.stream(classPathSuffixes).allMatch(suffix -> paths.stream().anyMatch(path -> path.endsWith(suffix)));
        }

        public boolean sourcePathContains(Set<Path> paths) {
            return Arrays.stream(sourcePathSuffixes).allMatch(suffix -> paths.stream().anyMatch(path -> path.endsWith(suffix)));
        }

        @Override
        public String toString() {
            return String.format("プロジェクト:%s のクラスパスは %s ソースパスの %s であること",
                    root.getName(), Arrays.toString(classPathSuffixes), Arrays.toString(sourcePathSuffixes));
        }
    }

    private static Project singleJavaProject() {
        return javaProjectOf("依存プロジェクトのないJavaプロジェクト");
    }

    private static Project multiSourceSetJavaProject() {
        ProjectInternal project = javaProjectOf("複数のソースセットを持つJavaプロジェクト");
        JavaPluginConvention convention = project.getConvention().getPlugin(JavaPluginConvention.class);
        convention.getSourceSets().create("sub");
        return project;
    }

    private static Project projectHasThreeTiersAndThreeJavaProjectDependencies() {
        Project root = javaProjectOf("3階層構造で依存Javaプロジェクトが２つあるJavaプロジェクト");
        DependencyHandler dependencies = root.getDependencies();

        ProjectInternal javaChild = javaProjectOf("javaChild");
        Stream<ProjectInternal> children = Stream.of(
                projectOf("nonJavaChild"),
                javaChild
        );
        children
                .map(GradleProjectTest::dependencyOf)
                .forEach(dependency -> dependencies.add("compile", dependency));


        DependencyHandler javaChildDependencies = javaChild.getDependencies();
        Stream<ProjectInternal> grandsons = Stream.of(
                projectOf("nonJavaGrandson"),
                javaProjectOf("javaGrandson")
        );
        grandsons
                .map(GradleProjectTest::dependencyOf)
                .forEach(dependency -> javaChildDependencies.add("compile", dependency));
        return root;
    }

    private static DefaultProjectDependency dependencyOf(ProjectInternal nonJavaChild) {
        return new DefaultProjectDependency(nonJavaChild, new DefaultProjectAccessListener(), true);
    }

    private static ProjectInternal projectOf(String name) {
        Project root = ProjectBuilder.builder()
                .withName(name)
                .withProjectDir(new File(name))
                .build();
        return (ProjectInternal) root;
    }

    private static ProjectInternal javaProjectOf(String name) {
        ProjectInternal project = projectOf(name);
        project.getPlugins().apply(JavaPlugin.class);
        return project;
    }

}
