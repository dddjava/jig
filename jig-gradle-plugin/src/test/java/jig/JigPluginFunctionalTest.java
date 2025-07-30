package jig;

import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * jig-gradle-pluginのFunctionalTest
 *
 * Gradle TestKitを使用して実際にGradleを動作させてテストを行う。
 *
 * @see <a href="https://docs.gradle.org/8.13/userguide/test_kit.html">Testing Build Logic with TestKit</a>
 */
@EnabledOnJre(JRE.JAVA_17) // Gradle7がJava21に対応していないため17でのみ実行する
public class JigPluginFunctionalTest {

    @TempDir
    Path testProjectDir;

    static Stream<String> supportGradleVersion() {
        return Stream.of(SupportGradleVersion.values())
                .map(SupportGradleVersion::getVersion)
                .distinct();
    }

    @ParameterizedTest
    @MethodSource("supportGradleVersion")
    void Javaプラグインが適用されていないプロジェクトではプロジェクトのビルドでエラーになる(String version) throws IOException {
        settingsGradle("""
                rootProject.name = 'my-test'
                """);
        buildGradle("""
                plugins {
                    id 'org.dddjava.jig-gradle-plugin'
                }
                """);

        var result = runner(version).buildAndFail();

        BuildTask taskResult = result.task(":jigReports");
        assertNotNull(taskResult);
        assertEquals(TaskOutcome.FAILED, taskResult.getOutcome());
        assertTrue(result.getOutput().contains("Java プラグインが適用されていません。"), result.getOutput());
    }

    /**
     * java&classファイルの有無はプラグインの責務ではないので空で実行できればよしとする。
     * classファイルも含めたテストをする場合はbuildする必要も出てくる。
     */
    @ParameterizedTest
    @MethodSource("supportGradleVersion")
    void 実行できる(String version) throws IOException {
        settingsGradle("""
                rootProject.name = 'my-test'
                """);
        buildGradle("""
                plugins {
                    id 'java'
                    id 'org.dddjava.jig-gradle-plugin'
                }
                """);

        var result = runner(version).build();

        var taskResult = Objects.requireNonNull(result.task(":jigReports"));
        assertEquals(TaskOutcome.SUCCESS, taskResult.getOutcome());
        assertTrue(result.getOutput().contains("[JIG] all JIG documents completed: "), result.getOutput());

        assertTrue(testProjectDir.resolve(Path.of("build", "jig", "index.html")).toFile().isFile());
    }

    @ParameterizedTest
    @MethodSource("supportGradleVersion")
    void オプションを指定して実行できる(String version) throws IOException {
        settingsGradle("""
                rootProject.name = 'my-test'
                """);
        buildGradle("""
                plugins {
                    id 'java'
                    id 'org.dddjava.jig-gradle-plugin'
                }
                jig {
                    modelPattern = "a"
                    documentTypes = []
                    documentTypesExclude = []
                    diagramFormat = 'png'
                    diagramTransitiveReduction = false
                    dotTimeout = '100ms'
                }
                """);

        var result = runner(version).build();

        var taskResult = Objects.requireNonNull(result.task(":jigReports"));
        assertEquals(TaskOutcome.SUCCESS, taskResult.getOutcome());
        assertTrue(result.getOutput().contains("[JIG] all JIG documents completed: "), result.getOutput());

        assertTrue(testProjectDir.resolve(Path.of("build", "jig", "index.html")).toFile().isFile());
    }

    @ParameterizedTest
    @MethodSource("supportGradleVersion")
    void 複数のソースセットを収集できる(String version) throws IOException {
        settingsGradle("rootProject.name = 'my-test'");
        buildGradle("""
                plugins {
                    id 'java'
                    id 'org.dddjava.jig-gradle-plugin'
                }
                sourceSets {
                    main {
                        java {
                            srcDirs = ['src/hoge/java', 'src/fuga/java']
                        }
                    }
                }
                """);
        var result = runner(version).build();

        var taskResult = Objects.requireNonNull(result.task(":jigReports"));
        assertEquals(TaskOutcome.SUCCESS, taskResult.getOutcome());
        assertTrue(result.getOutput().contains("[JIG] all JIG documents completed: "), result.getOutput());

        assertAll(
                () -> assertFalse(result.getOutput().contains("src/main/java".replace("/", File.separator)), result.getOutput()),
                () -> assertTrue(result.getOutput().contains("src/hoge/java".replace("/", File.separator)), result.getOutput()),
                () -> assertTrue(result.getOutput().contains("src/fuga/java".replace("/", File.separator)), result.getOutput())
        );
    }

    @ParameterizedTest
    @MethodSource("supportGradleVersion")
    void マルチプロジェクト構成でソースディレクトリを収集できる(String version) throws IOException {
        settingsGradle("""
                rootProject.name = 'my-test'
                include 'a', 'b', 'c', 'c-a', 'c-b', 'c-c', 'd'
                """);
        // aにプラグイン適用
        buildGradle("a", """
                plugins {
                    id 'java'
                    id 'org.dddjava.jig-gradle-plugin'
                }
                dependencies {
                    implementation project(':b');
                    implementation project(':c');
                    implementation project(':d');
                }
                """);
        // bは非javaプロジェクト。
        buildGradle("b", """
                plugins {
                }
                """);
        // cはjavaプロジェクトかつc-a,c-b,c-cに依存する
        // 複数階層とimplementation以外を拾わない検証
        buildGradle("c", """
                plugins {
                    id 'java'
                }
                dependencies {
                    testImplementation project(':c-a');
                    implementation project(':c-b');
                    runtimeOnly project(':c-c');
                }
                """);
        buildGradle("c-a", "plugins { id 'java' }");
        buildGradle("c-b", "plugins { id 'java' }");
        buildGradle("c-c", "plugins { id 'java' }");
        // dはjavaプロジェクト
        // 複数のjavaプロジェクトに依存しても取得できる検証用
        buildGradle("d", """
                plugins {
                     id 'java'
                }
                """);

        var result = runner(version).build();

        var taskResult = Objects.requireNonNull(result.task(":a:jigReports"));
        assertEquals(TaskOutcome.SUCCESS, taskResult.getOutcome());
        assertTrue(result.getOutput().contains("[JIG] all JIG documents completed: "), result.getOutput());

        // jig-coreのread paths... の中に出力されているのでそこで検証する。
        // TODO plugin側で解決したパスを出力してそこで検証する形にしたい

        String output = result.getOutput();
        // 含まれるもの
        // FIXME java pluginが適用されていないbが入っているのは想定外
        assertAll(Stream.of("a", "b", "c", "c-b", "d")
                .flatMap(project -> Stream.of(
                        project + "/src/main/java",
                        project + "/build/classes/java/main",
                        project + "/build/resources/main"
                ))
                .map(path -> path.replace("/", File.separator))
                .map(path -> () -> assertTrue(output.contains(path), path)));
        // 含まれないもの
        assertAll(Stream.of("c-a", "c-c")
                .flatMap(project -> Stream.of(
                        project + "/src/main/java",
                        project + "/build/classes/java/main",
                        project + "/build/resources/main"
                ))
                .map(path -> path.replace("/", File.separator))
                .map(path -> () -> assertFalse(output.contains(path), path)));
    }

    private void settingsGradle(String buildScript) throws IOException {
        Files.writeString(testProjectDir.resolve("settings.gradle"), buildScript);
    }

    private void buildGradle(String path, String buildScript) throws IOException {
        Path subprojectDir = testProjectDir.resolve(path);
        Files.createDirectories(subprojectDir);
        Files.writeString(subprojectDir.resolve("build.gradle"), buildScript);
    }

    private void buildGradle(String buildScript) throws IOException {
        Files.writeString(testProjectDir.resolve("build.gradle"), buildScript);
    }

    private GradleRunner runner(String version) throws IOException {

        return GradleRunner.create()
                .withGradleVersion(version)
                .withProjectDir(testProjectDir.toFile())
                .withArguments("jig", "--info")
                .withPluginClasspath();
    }
}
