package org.dddjava.jig.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gradleのサポート対象のバージョンでの動作確認
 */
public class IntegrationTest {

    @ParameterizedTest
    @EnumSource(SupportGradleVersion.class)
    void test(SupportGradleVersion version) throws IOException, URISyntaxException {
        Path path = Paths.get("./stub");
        GradleTaskRunner runner = version.runner(path);
        Path outputDir = path.resolve("sub-project/build/jig");
        String jigTask = ":sub-project:jigReports";

        {
            BuildResult result = runner.runTask("clean", "compileJava", jigTask, "--stacktrace");
            assertAll("スタブプロジェクトへの適用でパッケージ図とビジネスルール一覧が出力されること",
                    () -> {
                        BuildTask buildTask = Objects.requireNonNull(result.task(jigTask));
                        assertEquals(TaskOutcome.SUCCESS, buildTask.getOutcome());
                    },
                    () -> assertTrue(outputDir.resolve("package-relation-depth4.svg").toFile().exists()),
                    () -> assertTrue(outputDir.resolve("business-rule.xlsx").toFile().exists())
            );
        }

        {
            // cleanで build/jig が削除されること
            assertTrue(Files.exists(outputDir));
            runner.runTask("clean");
            assertFalse(Files.exists(outputDir));
        }
    }

    @Test
    void testKotlinDSL() throws IOException, URISyntaxException {
        SupportGradleVersion version = SupportGradleVersion.CURRENT;
        Path path = Paths.get("./stub-kotlin-dsl");
        GradleTaskRunner runner = version.runner(path);
        BuildResult result = runner.runTask("clean", "compileJava", "jigReports", "--stacktrace");

        Path outputDir = path.resolve("build/jig");
        assertAll(
                () -> {
                    BuildTask buildTask = Objects.requireNonNull(result.task(":jigReports"));
                    assertEquals(TaskOutcome.SUCCESS, buildTask.getOutcome());
                },
                () -> assertTrue(outputDir.resolve("package-relation-depth4.svg").toFile().exists())
        );
    }
}
