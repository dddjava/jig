package org.dddjava.jig.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
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

    @ParameterizedTest
    @EnumSource(SupportGradleVersion.class)
    void test(SupportGradleVersion version) throws IOException, URISyntaxException {
        Path path = Paths.get("./stub");
        Path outputDir = path.resolve("sub-project/build/jig");
        String jigTask = ":sub-project:jigReports";

        runTest(version, path, outputDir, jigTask);
    }

    void runTest(SupportGradleVersion version, Path path, Path outputDir, String jigTask) throws IOException, URISyntaxException {
        GradleTaskRunner runner = version.runner(path);
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
    }

    @Test
    void testKotlinDSL() throws IOException, URISyntaxException {
        SupportGradleVersion version = SupportGradleVersion.CURRENT;
        Path path = Paths.get("./stub-kotlin-dsl");
        Path outputDir = path.resolve("build/jig");
        String jigTask = ":jigReports";

        runTest(version, path, outputDir, jigTask);
    }
}
