package jig;

import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * jig-gradle-pluginの適用および実行のテスト
 */
public class JigPluginTest {

    @TempDir
    Path tempDir;

    @ParameterizedTest
    @EnumSource(SupportGradleVersion.class)
    void Javaプラグインが適用されていないプロジェクトではプロジェクトのビルドでエラーになる(SupportGradleVersion version) throws IOException {
        var result = runner(version, """
                plugins {
                    id 'org.dddjava.jig-gradle-plugin'
                }
                """)
                .buildAndFail();

        var taskResult = Objects.requireNonNull(result.task(":jigReports"));
        assertEquals(TaskOutcome.FAILED, taskResult.getOutcome());
        assertTrue(result.getOutput().contains("Java プラグインが適用されていません。"));
    }

    @ParameterizedTest
    @EnumSource(SupportGradleVersion.class)
    void 空プロジェクトで実行できる(SupportGradleVersion version) throws IOException {
        var result = runner(version, """
                plugins {
                    id 'java'
                    id 'org.dddjava.jig-gradle-plugin'
                }
                """)
                .build();

        var taskResult = Objects.requireNonNull(result.task(":jigReports"));
        assertEquals(TaskOutcome.SUCCESS, taskResult.getOutcome());
        assertTrue(result.getOutput().contains("[JIG] all JIG documents completed: "), result.getOutput());
    }

    @ParameterizedTest
    @EnumSource(SupportGradleVersion.class)
    void 単純なプロジェクトでJIGドキュメントが作成できる(SupportGradleVersion version) throws IOException {
        var srcDir = Files.createDirectories(tempDir.resolve("src").resolve("main").resolve("java"));
        var samplePackageDir = Files.createDirectories(srcDir.resolve("sample"));
        Files.writeString(samplePackageDir.resolve("Sample.java"), """
                package sample;
                class Sample {
                }
                """);

        var result = runner(version, """
                plugins {
                    id 'java'
                    id 'org.dddjava.jig-gradle-plugin'
                }
                jig {
                    modelPattern = '.+'
                }
                """)
                .build();

        var taskResult = Objects.requireNonNull(result.task(":jigReports"));
        assertEquals(TaskOutcome.SUCCESS, taskResult.getOutcome());
        assertTrue(result.getOutput().contains("[JIG] all JIG documents completed: "), result.getOutput());
    }

    private GradleRunner runner(SupportGradleVersion version, String buildGradle) throws IOException {
        Files.writeString(tempDir.resolve("build.gradle"), buildGradle);

        return GradleRunner.create()
                .withGradleVersion(version.getVersion())
                .withProjectDir(tempDir.toFile())
                .withArguments("jig", "--info")
                .withPluginClasspath();
    }
}
