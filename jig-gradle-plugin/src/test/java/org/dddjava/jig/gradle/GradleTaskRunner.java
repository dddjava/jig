package org.dddjava.jig.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class GradleTaskRunner {

    File file;

    GradleTaskRunner(File file) {
        this.file = file;
    }

    BuildResult executeGradleTasks(GradleVersions version, String... tasks) throws IOException, URISyntaxException {
        URL resource = GradleTaskRunner.class.getClassLoader().getResource("plugin-classpath.txt");
        List<File> pluginClasspath = Files.readAllLines(Paths.get(resource.toURI())).stream()
                .map(File::new)
                .collect(toList());

        return GradleRunner.create()
                .withGradleVersion(version.version())
                .withProjectDir(file)
                .withArguments(tasks)
                .withPluginClasspath(pluginClasspath)
                .build();
    }
}
