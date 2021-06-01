package org.dddjava.jig.gradle;

import org.dddjava.jig.domain.model.sources.file.SourcePaths;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency;
import org.gradle.api.internal.project.ConfigurationOnDemandProjectAccessListener;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.initialization.ProjectAccessListener;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GradleProjectTest {

    @TempDir
    Path tempDir;

    @ParameterizedTest
    @MethodSource("fixtures")
    public void 依存関係にあるすべてのJavaPluginが適用されたプロジェクトのクラスパスとソースパスが取得できること(
            Fixture fixture) throws Exception {
        Project project = fixture.createProject(tempDir);
        SourcePaths sourcePaths = new GradleProject(project).rawSourceLocations();

        assertAll(
                () -> assertPath(sourcePaths.binarySourcePaths(), fixture.classPathSuffixes),
                () -> assertPath(sourcePaths.textSourcePaths(), fixture.sourcePathSuffixes)
        );
    }

    void assertPath(List<Path> actualPaths, List<String> expectPathSuffixes) {
        // 数の一致を検証。不一致の場合は実際に解決されたパスを出力する。
        assertEquals(expectPathSuffixes.size(), actualPaths.size(),
                () -> actualPaths.toString());
        // 順序通りの一致を検証する。
        // Path#relativize などで完全一致を検証したいが
        // /var がシンボリックリンクだとactualが /private/var になったりするためうまくいかない。
        // endsWithで検証する。
        for (int i = 0; i < actualPaths.size(); i++) {
            Path actualPath = actualPaths.get(i);
            String expectedSuffix = expectPathSuffixes.get(i);
            assertTrue(actualPath.endsWith(expectedSuffix),
                    () -> String.format("expected: %s, actual: %s", expectedSuffix, actualPath));
        }
    }


    static List<Fixture> fixtures() {
        return Arrays.asList(
                Fixture.of("依存プロジェクトのないJavaプロジェクト")
                        .withClassPathSuffixes(
                                "依存プロジェクトのないJavaプロジェクト/build/classes/java/main",
                                "依存プロジェクトのないJavaプロジェクト/build/resources/main")
                        .withSourcePathSuffixes(
                                "依存プロジェクトのないJavaプロジェクト/src/main/java"),
                Fixture.of("3階層構造でcompile依存Javaプロジェクトが２つあるJavaプロジェクト")
                        .withClassPathSuffixes(
                                "3階層構造でcompile依存Javaプロジェクトが２つあるJavaプロジェクト/build/classes/java/main",
                                "3階層構造でcompile依存Javaプロジェクトが２つあるJavaプロジェクト/build/resources/main",
                                "javaChild/build/classes/java/main",
                                "javaChild/build/resources/main",
                                "javaGrandson/build/classes/java/main",
                                "javaGrandson/build/resources/main")
                        .withSourcePathSuffixes(
                                "3階層構造でcompile依存Javaプロジェクトが２つあるJavaプロジェクト/src/main/java",
                                "javaChild/src/main/java",
                                "javaGrandson/src/main/java"),
                Fixture.of("3階層構造でimplementation依存Javaプロジェクトが２つあるJavaプロジェクト")
                        .withClassPathSuffixes(
                                "3階層構造でimplementation依存Javaプロジェクトが２つあるJavaプロジェクト/build/classes/java/main",
                                "3階層構造でimplementation依存Javaプロジェクトが２つあるJavaプロジェクト/build/resources/main",
                                "javaChild/build/classes/java/main",
                                "javaChild/build/resources/main",
                                "javaGrandson/build/classes/java/main",
                                "javaGrandson/build/resources/main")
                        .withSourcePathSuffixes(
                                "3階層構造でimplementation依存Javaプロジェクトが２つあるJavaプロジェクト/src/main/java",
                                "javaChild/src/main/java",
                                "javaGrandson/src/main/java"),
                Fixture.of("複数のソースセットを持つJavaプロジェクト")
                        .withClassPathSuffixes(
                                "複数のソースセットを持つJavaプロジェクト/build/classes/java/main",
                                "複数のソースセットを持つJavaプロジェクト/build/classes/java/sub",
                                "複数のソースセットを持つJavaプロジェクト/build/resources/main",
                                "複数のソースセットを持つJavaプロジェクト/build/resources/sub")
                        .withSourcePathSuffixes(
                                "複数のソースセットを持つJavaプロジェクト/src/main/java",
                                "複数のソースセットを持つJavaプロジェクト/src/sub/java")
        );
    }

    static class Fixture {
        final String name;
        final List<String> classPathSuffixes;
        final List<String> sourcePathSuffixes;

        static Fixture of(String name) {
            return new Fixture(name, Collections.emptyList(), Collections.emptyList());
        }

        Fixture withClassPathSuffixes(String... args) {
            return new Fixture(name,
                    Stream.of(args).sorted().collect(Collectors.toList()),
                    sourcePathSuffixes);
        }

        Fixture withSourcePathSuffixes(String... args) {
            return new Fixture(name,
                    classPathSuffixes,
                    Stream.of(args).sorted().collect(Collectors.toList()));
        }

        public Fixture(String name, List<String> classPathSuffixes, List<String> sourcePathSuffixes) {
            this.name = name;
            this.classPathSuffixes = classPathSuffixes;
            this.sourcePathSuffixes = sourcePathSuffixes;
        }

        @Override
        public String toString() {
            return String.format("%s{classPaths: %s, sourcePaths: %s}", name, classPathSuffixes, sourcePathSuffixes);
        }

        public Project createProject(Path tempDir) {
            try {
                Method projectMethod = GradleProjectTest.class.getDeclaredMethod("_" + name, Path.class);
                projectMethod.setAccessible(true);
                return (Project) projectMethod.invoke(null, tempDir);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }

    }

    private static Project _依存プロジェクトのないJavaプロジェクト(Path tempDir) {
        return javaProjectOf("依存プロジェクトのないJavaプロジェクト", tempDir);
    }

    private static Project _複数のソースセットを持つJavaプロジェクト(Path tempDir) {
        Project project = javaProjectOf("複数のソースセットを持つJavaプロジェクト", tempDir);
        JavaPluginConvention convention = project.getConvention().getPlugin(JavaPluginConvention.class);
        convention.getSourceSets().create("sub");
        return project;
    }

    /**
     * 後方互換用: Gradle6サポートを切るまでは必要なテスト
     */
    private static Project _3階層構造でcompile依存Javaプロジェクトが２つあるJavaプロジェクト(Path tempDir) {
        Project javaChild = withDependency(javaProjectOf("javaChild", tempDir),
                "compile",
                projectOf("nonJavaGrandson", tempDir),
                javaProjectOf("javaGrandson", tempDir));

        return withDependency(javaProjectOf("3階層構造でcompile依存Javaプロジェクトが２つあるJavaプロジェクト", tempDir),
                "compile",
                projectOf("nonJavaChild", tempDir),
                javaChild);
    }

    private static Project _3階層構造でimplementation依存Javaプロジェクトが２つあるJavaプロジェクト(Path tempDir) {
        Project javaChild = withDependency(javaProjectOf("javaChild", tempDir),
                "implementation",
                projectOf("nonJavaGrandson", tempDir),
                javaProjectOf("javaGrandson", tempDir));

        return withDependency(javaProjectOf("3階層構造でimplementation依存Javaプロジェクトが２つあるJavaプロジェクト", tempDir),
                "implementation",
                projectOf("nonJavaChild", tempDir),
                javaChild);
    }

    static Project withDependency(Project project, String configurationName, Project... dependencies) {
        DependencyHandler projectDependencies = project.getDependencies();
        for (Project dependency : dependencies) {
            projectDependencies.add(configurationName, dependency);
        }
        return project;
    }

    private static DefaultProjectDependency dependencyOf(ProjectInternal nonJavaChild) {
        ConfigurationOnDemandProjectAccessListener listener = new ConfigurationOnDemandProjectAccessListener();
        return new DefaultProjectDependency(nonJavaChild, new ProjectAccessListener() {
            @Override
            public void beforeRequestingTaskByPath(@Nonnull ProjectInternal projectInternal) {
                listener.beforeRequestingTaskByPath(projectInternal);
            }

            @Override
            public void beforeResolvingProjectDependency(@Nonnull ProjectInternal projectInternal) {
                listener.beforeResolvingProjectDependency(projectInternal);
            }
        }, true);
    }

    private static ProjectInternal javaProjectOf(String name, Path tempDir) {
        ProjectInternal project = projectOf(name, tempDir);
        project.getPlugins().apply(JavaPlugin.class);
        return project;
    }

    private static ProjectInternal projectOf(String name, Path tempDir) {
        Project root = ProjectBuilder.builder()
                .withName(name)
                .withProjectDir(tempDir.resolve(name).toFile())
                .build();
        return (ProjectInternal) root;
    }

}
