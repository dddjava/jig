package org.dddjava.jig.gradle;

import org.assertj.core.api.SoftAssertions;
import org.gradle.internal.impldep.org.junit.Before;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class IntegrationTest {
    final Path outputDir = Paths.get("stub/sub-project/build/jig");

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
        BuildResult result = executeGradleTasks(version, "clean", "compileJava", ":sub-project:jigReports", "--stacktrace");

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

        BuildResult result = executeGradleTasks(version, "clean");

        softly.assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
        softly.assertThat(includedFile).doesNotExist();
        softly.assertThat(outputDir).doesNotExist();
        softly.assertAll();
    }

    private BuildResult executeGradleTasks(String version, String... tasks) throws IOException, URISyntaxException {
        URL resource = getClass().getClassLoader().getResource("plugin-classpath.txt");
        List<File> pluginClasspath = Files.readAllLines(Paths.get(resource.toURI())).stream()
                .map(File::new)
                .collect(toList());

        return GradleRunner.create()
                .withGradleVersion(version)
                .withProjectDir(new File("./stub"))
                .withArguments(tasks)
                .withPluginClasspath(pluginClasspath)
                .build();
    }


}
