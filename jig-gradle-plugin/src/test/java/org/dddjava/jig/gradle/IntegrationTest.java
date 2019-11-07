package org.dddjava.jig.gradle;

import org.assertj.core.api.SoftAssertions;
import org.gradle.internal.impldep.org.junit.Before;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.condition.DisabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@DisabledOnJre(JRE.JAVA_13)
public class IntegrationTest {
    final Path outputDir = Paths.get("stub/sub-project/build/jig");
    final GradleTaskRunner runner = new GradleTaskRunner(new File("./stub"));

    @Before
    public void clean() {
        File dir = outputDir.toFile();
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                file.delete();
            }
        }
    }

    static Stream<String> versions() {
        return Stream.of("5.0", "5.6.2");
    }

    @ParameterizedTest
    @MethodSource("versions")
    void スタブプロジェクトへの適用でパッケージ図と機能一覧が出力されること(String version) throws IOException, URISyntaxException {
        BuildResult result = runner.executeGradleTasks(version, "clean", "compileJava", ":sub-project:jigReports", "--stacktrace");

        System.out.println(result.getOutput());
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
        softly.assertThat(outputDir.resolve("package-relation-depth4.svg")).exists();
        softly.assertThat(outputDir.resolve("application.xlsx")).exists();
        softly.assertAll();
    }

    //TODO 並列で走ると競合して落ちる
    @ParameterizedTest
    @MethodSource("versions")
    void スタブプロジェクトのcleanタスクで出力ディレクトリが中のファイルごと削除されること(String version) throws IOException, URISyntaxException {
        Files.createDirectories(outputDir);
        Path includedFile = outputDir.resolve("somme.txt");
        Files.createFile(includedFile);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(outputDir).exists();
        softly.assertThat(includedFile).exists();
        softly.assertAll();

        BuildResult result = runner.executeGradleTasks(version, "clean");

        softly.assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
        softly.assertThat(includedFile).doesNotExist();
        softly.assertThat(outputDir).doesNotExist();
        softly.assertAll();
    }



}
