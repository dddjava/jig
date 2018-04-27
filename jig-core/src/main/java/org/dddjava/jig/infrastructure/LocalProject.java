package org.dddjava.jig.infrastructure;

import org.dddjava.jig.domain.model.datasource.SqlSources;
import org.dddjava.jig.domain.model.japanese.PackageNameSources;
import org.dddjava.jig.domain.model.japanese.TypeNameSources;
import org.dddjava.jig.domain.model.specification.SpecificationSource;
import org.dddjava.jig.domain.model.specification.SpecificationSources;
import org.dddjava.jig.domain.model.datasource.SqlSources;
import org.dddjava.jig.domain.model.japanese.PackageNameSources;
import org.dddjava.jig.domain.model.japanese.TypeNameSources;
import org.dddjava.jig.domain.model.specification.SpecificationSource;
import org.dddjava.jig.domain.model.specification.SpecificationSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class LocalProject {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalProject.class);

    private final Path projectPath;
    Path classesDirectory;
    Path resourcesDirectory;
    Path sourcesDirectory;

    public LocalProject(@Value("${project.path}") String projectPath,
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

    public SpecificationSources getSpecificationSources() {
        ArrayList<SpecificationSource> sources = new ArrayList<>();
        try {
            for (Path path : extractClassPath()) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (isClassFile(file)) {
                            SpecificationSource specificationSource = new SpecificationSource(file);
                            sources.add(specificationSource);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        LOGGER.info("*.class: {}件", sources.size());
        return new SpecificationSources(sources);
    }

    private Path[] extractClassPath() {
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

    private boolean isClassFile(Path path) {
        return path.toString().endsWith(".class");
    }

    private boolean isJavaFile(Path path) {
        return path.toString().endsWith(".java");
    }

    private boolean isPackageInfoFile(Path path) {
        return path.toString().endsWith("package-info.java");
    }

    public SqlSources getSqlSources() {
        try {
            Path[] array = extractClassPath();

            URL[] urls = new URL[array.length];
            List<String> classNames = new ArrayList<>();
            for (int i = 0; i < array.length; i++) {
                Path path = array[i];
                urls[i] = path.toUri().toURL();

                try (Stream<Path> walk = Files.walk(path)) {
                    List<String> collect = walk.filter(p -> p.toFile().isFile())
                            .map(path::relativize)
                            .filter(this::isMapperClassFile)
                            .map(this::toClassName)
                            .collect(Collectors.toList());
                    classNames.addAll(collect);
                }
            }

            LOGGER.info("*Mapper.class: {}件", classNames.size());
            return new SqlSources(urls, classNames);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isMapperClassFile(Path path) {
        return path.toString().endsWith("Mapper.class");
    }

    private String toClassName(Path path) {
        String pathStr = path.toString();
        return pathStr.substring(0, pathStr.length() - 6).replace(File.separatorChar, '.');
    }

    public PackageNameSources getPackageNameSources() {
        List<Path> paths = pathsOf(this::isPackageInfoFile);
        LOGGER.info("package-info.java: {}件", paths.size());
        return new PackageNameSources(paths);
    }

    public TypeNameSources getTypeNameSources() {
        List<Path> paths = pathsOf(this::isJavaFile);
        LOGGER.info("*.java: {}件", paths.size());
        return new TypeNameSources(paths);
    }

    private List<Path> pathsOf(Predicate<Path> condition) {
        try {
            List<Path> paths = new ArrayList<>();
            for (Path path : extractSourcePath()) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (condition.test(file)) paths.add(file);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            return paths;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path[] extractSourcePath() {
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
