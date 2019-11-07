package org.dddjava.jig.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class GradleTaskRunner {

    File file;

    GradleTaskRunner(File file) {
        this.file = file;
    }

    BuildResult executeGradleTasks(String version, String... tasks) throws IOException, URISyntaxException {
        URL resource = GradleTaskRunner.class.getClassLoader().getResource("plugin-classpath.txt");
        List<File> pluginClasspath = Files.readAllLines(Paths.get(resource.toURI())).stream()
                .map(File::new)
                .collect(toList());

        HashMap<String, String> env = new HashMap<>();
        Map<String, String> systemEnv = System.getenv();
        env.putAll(systemEnv);
        env.put("GRADLE_OPTS","-Dorg.gradle.daemon=false");

        return GradleRunner.create()
                .withGradleVersion(version)
                .withProjectDir(file)
                .withArguments(tasks)
                .withPluginClasspath(pluginClasspath)
                .withEnvironment(env)
                .build();
    }
}
