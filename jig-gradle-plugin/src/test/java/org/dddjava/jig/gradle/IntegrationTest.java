package org.dddjava.jig.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
    static Logger logger = LoggerFactory.getLogger(IntegrationTest.class);

    @EnabledOnJre(JRE.JAVA_11)
    @ParameterizedTest
    @EnumSource(SupportGradleVersion.class)
    void testJava11(SupportGradleVersion version) {
        runTest("./stub", "sub-project/build/jig", ":sub-project:jigReports", version);
    }

    @EnabledOnJre(JRE.JAVA_17)
    @Test
    void testJava17() {
        // Java17対応はGradle7.3以降なのでCURRENTのみを対象にする
        SupportGradleVersion version = SupportGradleVersion.CURRENT;
        runTest("./stub", "sub-project/build/jig", ":sub-project:jigReports", version);
    }


    @EnabledOnJre(JRE.JAVA_11)
    @ParameterizedTest
    @EnumSource(SupportGradleVersion.class)
    void testKotlinDSL(SupportGradleVersion version) {
        runTest("./stub-kotlin-dsl", "build/jig", ":jigReports", version);
    }

    private void runTest(String projectDirString, String outputDirString, String jigTask, SupportGradleVersion gradleVersion) {
        try {
            Path projectDir = Paths.get(projectDirString);
            Path outputDir = projectDir.resolve(outputDirString);

            GradleTaskRunner runner = gradleVersion.createTaskRunner(projectDir);
            BuildResult result = runner.runTask("clean", "compileJava", jigTask, "--stacktrace");

            logger.warn("task results = {}", result.getTasks());

            BuildTask buildTask = Objects.requireNonNull(result.task(jigTask));
            assertEquals(TaskOutcome.SUCCESS, buildTask.getOutcome());

            File outputDirectory = outputDir.toFile();
            logger.warn("outputDir={}, exists={}, list={}", outputDir.toAbsolutePath(), outputDirectory.exists(), outputDirectory.list());

            assertTrue(Files.exists(outputDir));
            assertTrue(Objects.requireNonNull(outputDirectory.list()).length > 0);

            // cleanで build/jig が削除されること
            runner.runTask("clean");
            assertFalse(Files.exists(outputDir));
        } catch (IOException | URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

}
