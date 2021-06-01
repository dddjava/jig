package org.dddjava.jig.gradle;

import org.assertj.core.api.SoftAssertions;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class IntegrationTest {

    @DisabledOnJre(JRE.JAVA_13)
    @ParameterizedTest
    @EnumSource(SupportGradleVersion.class)
    void test(SupportGradleVersion version) throws IOException, URISyntaxException {
        GradleTaskRunner runner = new GradleTaskRunner(new File("./stub"));
        Path outputDir = Paths.get("./stub/sub-project/build/jig");
        String jigTask = ":sub-project:jigReports";

        {
            BuildResult result = runner.executeGradleTasks(version, "clean", "compileJava", jigTask, "--stacktrace");
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
            assertTrue(Files.exists(outputDir));
            runner.executeGradleTasks(version, "clean");
            assertFalse(Files.exists(outputDir));
        }
    }

    @Test
    void testKotlinDSL() throws IOException, URISyntaxException {
        Path outputDir = Paths.get("./stub-kotlin-dsl/build/jig");
        GradleTaskRunner runner = new GradleTaskRunner(new File("./stub-kotlin-dsl"));
        BuildResult result = runner.executeGradleTasks(SupportGradleVersion.CURRENT, "clean", "compileJava", "jigReports", "--stacktrace");

        System.out.println(result.getOutput());
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
        softly.assertThat(outputDir.resolve("package-relation-depth4.svg")).exists();
        softly.assertAll();
    }
}
