package org.dddjava.jig.gradle;

import org.assertj.core.api.SoftAssertions;
import org.gradle.internal.impldep.org.junit.Before;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IntegrationTestForKotlinDSL {
    final Path outputDir = Paths.get("stub-kotlin-dsl/build/jig");
    final GradleTaskRunner runner = new GradleTaskRunner(new File("./stub-kotlin-dsl"));

    @Before
    public void clean() {
        File dir = outputDir.toFile();
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                file.delete();
            }
        }
    }


    @Test
    void スタブプロジェクトへの適用でパッケージ図と機能一覧が出力されること() throws IOException, URISyntaxException {
        BuildResult result = runner.executeGradleTasks("5.6", "clean", "compileJava", "jigReports", "--stacktrace");

        System.out.println(result.getOutput());
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
        softly.assertThat(outputDir.resolve("package-relation-depth4.svg")).exists();
        softly.assertAll();
    }



}
