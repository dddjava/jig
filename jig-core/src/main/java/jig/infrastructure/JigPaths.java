package jig.infrastructure;

import jig.domain.model.project.ProjectLocation;
import jig.domain.model.specification.SpecificationSource;
import jig.domain.model.specification.SpecificationSources;
import jig.infrastructure.mybatis.SqlSources;
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
import java.util.stream.Collectors;
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
            return walk
                    .filter(Files::isDirectory)
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

    public SpecificationSources getSpecificationSources(ProjectLocation rootPath) {
        ArrayList<SpecificationSource> sources = new ArrayList<>();
        try {
            for (Path path : extractClassPath(rootPath.getValue())) {
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
        return new SpecificationSources(sources);
    }

    public List<Path> sourcePaths(ProjectLocation location) {
        try {
            List<Path> paths = new ArrayList<>();
            for (Path path : extractSourcePath(location.getValue())) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (isJavaFile(file)) paths.add(file);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            return paths;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<Path> packageInfoPaths(ProjectLocation location) {
        try {
            List<Path> paths = new ArrayList<>();
            for (Path path : extractSourcePath(location.getValue())) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (isPackageInfoFile(file)) paths.add(file);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            return paths;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public SqlSources getSqlSources(ProjectLocation projectLocation) {
        try {
            Path[] array = extractClassPath(projectLocation.getValue());

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

            return new SqlSources(urls, classNames);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
