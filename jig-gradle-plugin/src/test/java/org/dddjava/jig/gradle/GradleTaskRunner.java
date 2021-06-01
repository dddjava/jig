package org.dddjava.jig.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class GradleTaskRunner {

    SupportGradleVersion version;
    Path path;

    GradleTaskRunner(SupportGradleVersion version, Path path) {
        this.version = version;
        this.path = path;
    }

    BuildResult runTask(String... tasks) throws IOException, URISyntaxException {
        URL resource = Objects.requireNonNull(GradleTaskRunner.class.getClassLoader().getResource("plugin-classpath.txt"));
        List<File> pluginClasspath = Files.readAllLines(Paths.get(resource.toURI())).stream()
                .map(File::new)
                .collect(toList());

        return GradleRunner.create()
                .withGradleVersion(version.version())
                .withProjectDir(path.toFile())
                .withArguments(tasks)
                .withPluginClasspath(pluginClasspath)
                .build();
    }
}
