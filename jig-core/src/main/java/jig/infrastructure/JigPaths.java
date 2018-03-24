package jig.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Component
public class JigPaths {

    private static final Logger LOGGER = LoggerFactory.getLogger(JigPaths.class);

    public boolean isJavaFile(Path path) {
        return path.toString().endsWith(".java");
    }

    public boolean isClassFile(Path path) {
        return path.toString().endsWith(".class");
    }

    public boolean isPackageInfoFile(Path path) {
        return path.toString().endsWith("package-info.java");
    }

    Path classesDirectory;
    Path resourcesDirectory;
    Path sourcesDirectory;

    public JigPaths(@Value("${directory.classes:build/classes/java/main}") String classesDirectory,
                    @Value("${directory.resources:build/resources/main}") String resourcesDirectory,
                    @Value("${directory.sources:src/main/java}") String sourcesDirectory) {
        LOGGER.info("classes suffix  : {}", classesDirectory);
        LOGGER.info("resources suffix: {}", resourcesDirectory);
        LOGGER.info("sources suffix  : {}", sourcesDirectory);

        this.classesDirectory = Paths.get(classesDirectory);
        this.resourcesDirectory = Paths.get(resourcesDirectory);
        this.sourcesDirectory = Paths.get(sourcesDirectory);
    }

    public Path[] extractClassPath(Path rootPath) {
        try (Stream<Path> walk = Files.walk(rootPath)) {
            return walk.filter(Files::isDirectory)
                    .filter(path -> path.endsWith(classesDirectory) || path.endsWith(resourcesDirectory))
                    .peek(path -> LOGGER.info("classes: {}", path))
                    .toArray(Path[]::new);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Path[] extractSourcePath(Path rootPath) {
        try (Stream<Path> walk = Files.walk(rootPath)) {
            return walk.filter(Files::isDirectory)
                    .filter(path -> path.endsWith(sourcesDirectory))
                    .peek(path -> LOGGER.info("sources: {}", path))
                    .toArray(Path[]::new);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean isMapperClassFile(Path path) {
        return path.toString().endsWith("Mapper.class");
    }

    public String toClassName(Path path) {
        String pathStr = path.toString();
        return pathStr.substring(0, pathStr.length() - 6).replace(File.separatorChar, '.');
    }
}
