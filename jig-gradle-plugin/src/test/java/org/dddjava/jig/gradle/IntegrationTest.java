package org.dddjava.jig.gradle;

import org.assertj.core.api.SoftAssertions;
import org.gradle.internal.impldep.org.junit.Before;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class IntegrationTest {
    final Path outputDir = Paths.get("stub/build/jig/sub-project");

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
    void スタブプロジェクトへの適用でパッケージ図と機能一覧が出力されること() throws IOException {
        BuildResult result = executeGradleTasks("clean", ":sub-project:jigReports");

        System.out.println(result.getOutput());
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
        softly.assertThat(outputDir.resolve("package-dependency-depth4.svg")).exists();
        softly.assertThat(outputDir.resolve("application.xlsx")).exists();
        softly.assertAll();
    }

    //TODO 並列で走ると競合して落ちる
    @Test
    void スタブプロジェクトのcleanタスクで出力ディレクトリが中のファイルごと削除されること() throws IOException {
        Files.createDirectories(outputDir);
        Path includedFile = outputDir.resolve("somme.txt");
        Files.createFile(includedFile);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(outputDir).exists();
        softly.assertThat(includedFile).exists();
        softly.assertAll();

        BuildResult result = executeGradleTasks("clean");

        softly.assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
        softly.assertThat(includedFile).doesNotExist();
        softly.assertThat(outputDir).doesNotExist();
        softly.assertAll();
    }

    private BuildResult executeGradleTasks(String... tasks) throws IOException {
        URL resource = getClass().getClassLoader().getResource("plugin-classpath.txt");
        List<File> pluginClasspath = Files.readAllLines(Paths.get(resource.getPath())).stream()
                .map(File::new)
                .collect(toList());

        return GradleRunner.create()
                .withGradleVersion("4.9")
                .withProjectDir(new File("./stub"))
                .withArguments(tasks)
                .withPluginClasspath(pluginClasspath)
                .build();
    }


}
