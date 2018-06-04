package org.dddjava.jig.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Component
public class DefaultOrigin implements Origin {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalProject.class);

    private final Path projectPath;
    Path classesDirectory;
    Path resourcesDirectory;
    Path sourcesDirectory;

    public DefaultOrigin(@Value("${project.path}") String projectPath,
                               @Value("${directory.classes:build/classes/java/main}") String classesDirectory,
                               @Value("${directory.resources:build/resources/main}") String resourcesDirectory,
                               @Value("${directory.sources:src/main/java}") String sourcesDirectory) {
        LOGGER.info("Project Path: {}", projectPath);
        LOGGER.info("classes suffix  : {}", classesDirectory);
        LOGGER.info("resources suffix: {}", resourcesDirectory);
        LOGGER.info("sources suffix  : {}", sourcesDirectory);

        this.projectPath = Paths.get(projectPath);
        this.classesDirectory = Paths.get(classesDirectory);
        this.resourcesDirectory = Paths.get(resourcesDirectory);
        this.sourcesDirectory = Paths.get(sourcesDirectory);
    }
    @Override
    public Path[] extractClassPath() {
        try (Stream<Path> walk = Files.walk(projectPath)) {
            return walk
                    .filter(Files::isDirectory)
                    .filter(path -> path.endsWith(classesDirectory) || path.endsWith(resourcesDirectory))
                    .peek(path -> LOGGER.info("classes: {}", path))
                    .toArray(Path[]::new);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Path[] extractSourcePath() {
        try (Stream<Path> walk = Files.walk(projectPath)) {
            return walk.filter(Files::isDirectory)
                    .filter(path -> path.endsWith(sourcesDirectory))
                    .peek(path -> LOGGER.info("sources: {}", path))
                    .toArray(Path[]::new);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
