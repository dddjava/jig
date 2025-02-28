package jig;

import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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

    private void settingsGradle(String buildScript) throws IOException {
        Files.writeString(testProjectDir.resolve("settings.gradle"), buildScript);
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
