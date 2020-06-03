package org.dddjava.jig.gradle;

import org.assertj.core.api.SoftAssertions;
import org.dddjava.jig.domain.model.jigsource.file.SourcePaths;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency;
import org.gradle.api.internal.project.ConfigurationOnDemandProjectAccessListener;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class GradleProjectTest {

    @TempDir
    Path tempDir;

    @ParameterizedTest
    @MethodSource("fixtures")
    public void 依存関係にあるすべてのJavaPluginが適用されたプロジェクトのクラスパスとソースパスが取得できること(
            String name,
            String[] classPathSuffixes,
            String[] sourcePathSuffixes) throws Exception {

        Method projectMethod = GradleProjectTest.class.getDeclaredMethod("_" + name, Path.class);
        projectMethod.setAccessible(true);
        Project project = (Project) projectMethod.invoke(null, tempDir);

        SourcePaths sourcePaths = new GradleProject(project).rawSourceLocations();

        List<Path> binarySourcePaths = sourcePaths.binarySourcePaths();
        List<Path> textSourcePaths = sourcePaths.textSourcePaths();

        Fixture fixture = new Fixture(classPathSuffixes, sourcePathSuffixes);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fixture.classPathContains(new HashSet<>(binarySourcePaths))).isTrue();
        softly.assertThat(fixture.sourcePathContains(new HashSet<>(textSourcePaths))).isTrue();
        softly.assertAll();
    }


    static List<Arguments> fixtures() {
        return Arrays.asList(
                arguments(
                        "依存プロジェクトのないJavaプロジェクト",
                        new String[]{
                                "依存プロジェクトのないJavaプロジェクト/build/classes/java/main",
                                "依存プロジェクトのないJavaプロジェクト/build/resources/main"
                        },
                        new String[]{
                                "依存プロジェクトのないJavaプロジェクト/src/main/java"
                        }
                ),
                arguments(
                        "3階層構造でcompile依存Javaプロジェクトが２つあるJavaプロジェクト",
                        new String[]{
                                "3階層構造でcompile依存Javaプロジェクトが２つあるJavaプロジェクト/build/classes/java/main",
                                "3階層構造でcompile依存Javaプロジェクトが２つあるJavaプロジェクト/build/resources/main",
                                "javaChild/build/classes/java/main",
                                "javaChild/build/resources/main",
                                "javaGrandson/build/classes/java/main",
                                "javaGrandson/build/resources/main"
                        },
                        new String[]{
                                "3階層構造でcompile依存Javaプロジェクトが２つあるJavaプロジェクト/src/main/java",
                                "javaChild/src/main/java",
                                "javaGrandson/src/main/java"
                        }
                ),
                arguments(
                        "3階層構造でimplementation依存Javaプロジェクトが２つあるJavaプロジェクト",
                        new String[]{
                                "3階層構造でimplementation依存Javaプロジェクトが２つあるJavaプロジェクト/build/classes/java/main",
                                "3階層構造でimplementation依存Javaプロジェクトが２つあるJavaプロジェクト/build/resources/main",
                                "javaChild/build/classes/java/main",
                                "javaChild/build/resources/main",
                                "javaGrandson/build/classes/java/main",
                                "javaGrandson/build/resources/main"
                        },
                        new String[]{
                                "3階層構造でimplementation依存Javaプロジェクトが２つあるJavaプロジェクト/src/main/java",
                                "javaChild/src/main/java",
                                "javaGrandson/src/main/java"
                        }
                ),
                arguments(
                        "複数のソースセットを持つJavaプロジェクト",
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
        final String[] classPathSuffixes;
        final String[] sourcePathSuffixes;

        Fixture(String[] classPathSuffixes, String[] sourcePathSuffixes) {
            this.classPathSuffixes = classPathSuffixes;
            this.sourcePathSuffixes = sourcePathSuffixes;
        }


        public boolean classPathContains(Set<Path> paths) {
            return Arrays.stream(classPathSuffixes).allMatch(suffix -> paths.stream().anyMatch(path -> path.endsWith(suffix)));
        }

        public boolean sourcePathContains(Set<Path> paths) {
            return Arrays.stream(sourcePathSuffixes).allMatch(suffix -> paths.stream().anyMatch(path -> path.endsWith(suffix)));
        }
    }

    private static Project _依存プロジェクトのないJavaプロジェクト(Path tempDir) {
        return javaProjectOf("依存プロジェクトのないJavaプロジェクト", tempDir);
    }

    private static Project _複数のソースセットを持つJavaプロジェクト(Path tempDir) {
        ProjectInternal project = javaProjectOf("複数のソースセットを持つJavaプロジェクト", tempDir);
        JavaPluginConvention convention = project.getConvention().getPlugin(JavaPluginConvention.class);
        convention.getSourceSets().create("sub");
        return project;
    }

    private static Project _3階層構造でcompile依存Javaプロジェクトが２つあるJavaプロジェクト(Path tempDir) {
        Project root = javaProjectOf("3階層構造でcompile依存Javaプロジェクトが２つあるJavaプロジェクト", tempDir);
        DependencyHandler dependencies = root.getDependencies();

        ProjectInternal javaChild = javaProjectOf("javaChild", tempDir);
        Stream<ProjectInternal> children = Stream.of(
                projectOf("nonJavaChild", tempDir),
                javaChild
        );
        children
                .map(GradleProjectTest::dependencyOf)
                .forEach(dependency -> dependencies.add("compile", dependency));


        DependencyHandler javaChildDependencies = javaChild.getDependencies();
        Stream<ProjectInternal> grandsons = Stream.of(
                projectOf("nonJavaGrandson", tempDir),
                javaProjectOf("javaGrandson", tempDir)
        );
        grandsons
                .map(GradleProjectTest::dependencyOf)
                .forEach(dependency -> javaChildDependencies.add("compile", dependency));
        return root;
    }

    private static Project _3階層構造でimplementation依存Javaプロジェクトが２つあるJavaプロジェクト(Path tempDir) {
        Project root = javaProjectOf("3階層構造でimplementation依存Javaプロジェクトが２つあるJavaプロジェクト", tempDir);
        DependencyHandler dependencies = root.getDependencies();

        ProjectInternal javaChild = javaProjectOf("javaChild", tempDir);
        Stream<ProjectInternal> children = Stream.of(
                projectOf("nonJavaChild", tempDir),
                javaChild
        );
        children
                .map(GradleProjectTest::dependencyOf)
                .forEach(dependency -> dependencies.add("implementation", dependency));


        DependencyHandler javaChildDependencies = javaChild.getDependencies();
        Stream<ProjectInternal> grandsons = Stream.of(
                projectOf("nonJavaGrandson", tempDir),
                javaProjectOf("javaGrandson", tempDir)
        );
        grandsons
                .map(GradleProjectTest::dependencyOf)
                .forEach(dependency -> javaChildDependencies.add("implementation", dependency));
        return root;
    }

    private static DefaultProjectDependency dependencyOf(ProjectInternal nonJavaChild) {
        return new DefaultProjectDependency(nonJavaChild, new ConfigurationOnDemandProjectAccessListener(), true);
    }

    private static ProjectInternal projectOf(String name, Path tempDir) {
        Project root = ProjectBuilder.builder()
                .withName(name)
                .withProjectDir(tempDir.resolve(name).toFile())
                .build();
        return (ProjectInternal) root;
    }

    private static ProjectInternal javaProjectOf(String name, Path tempDir) {
        ProjectInternal project = projectOf(name, tempDir);
        project.getPlugins().apply(JavaPlugin.class);
        return project;
    }

}
