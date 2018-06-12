package org.dddjava.jig.gradle;

import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency;
import org.gradle.api.internal.project.DefaultProjectAccessListener;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

class GradleProjectTest {

    @ParameterizedTest
    @MethodSource("fixtures")
    public void 依存関係にあるすべてのJavaPluginが適用されたプロジェクトのクラスパスとソースパスが取得できること(Fixture fixture) {


        GradleProjects gradleProjects = new GradleProject(fixture.root).allDependencyJavaProjects();


        //TODO: もうちょっとアサーションを
        assertThat(gradleProjects.extractLayoutClassPath())
                .hasSize(fixture.classPathCount);

        assertThat(gradleProjects.extractLayoutSourcePath())
                .hasSize(fixture.sourcePathCount);
    }


    static List<Fixture> fixtures() {
        return Arrays.asList(
                new Fixture(singleJavaProject(), 2, 1),
                new Fixture(projectHasThreeTiersAndThreeJavaProjectDependencies(), 2 * 3, 3)
        );
    }

    static class Fixture {
        final Project root;
        final int classPathCount;
        final int sourcePathCount;

        Fixture(Project root, int classPathCount, int sourcePathCount) {
            this.root = root;
            this.classPathCount = classPathCount;
            this.sourcePathCount = sourcePathCount;
        }

        @Override
        public String toString() {
            return String.format("プロジェクト:%s のクラスパスの数は %s ソースパスの数は %s であること", root.getName(), classPathCount, sourcePathCount);
        }
    }

    private static Project singleJavaProject() {
        return javaProjectOf("依存プロジェクトのないJavaプロジェクト");
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
                .build();
        return (ProjectInternal) root;
    }

    private static ProjectInternal javaProjectOf(String name) {
        ProjectInternal project = projectOf(name);
        project.getPlugins().apply(JavaPlugin.class);
        return project;
    }

}
