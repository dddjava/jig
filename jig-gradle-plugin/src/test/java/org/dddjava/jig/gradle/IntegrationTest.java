package org.dddjava.jig.gradle;

import org.assertj.core.api.SoftAssertions;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class IntegrationTest {

    @Test
    public void スタブプロジェクトへの適用でパッケージ図とリポジトリが出力されること() throws IOException {

        URL resource = getClass().getClassLoader().getResource("plugin-classpath.txt");
        List<File> classpaths = Files.readAllLines(Paths.get(resource.getPath())).stream()
                .map(path -> new File(path))
                .collect(toList());


        BuildResult result = GradleRunner.create()
                .withGradleVersion("4.9")
                .withProjectDir(new File("./stub"))
                .withArguments("compileJava", "jigReports")
                .withPluginClasspath(classpaths)
                .build();


        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
        softly.assertThat(new File("./build/jig/stub/package-dependency-depth4.svg")).exists();
        softly.assertThat(Files.readAllLines(Paths.get("./build/jig/stub/application.txt")))
                .contains("[com.example.infrastructure.FromDataSource, register(From), void, , [], [], [], []]");
        softly.assertAll();
    }
}
