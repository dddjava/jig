package org.dddjava.jig.infrastructure;

import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodeSource;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodeSources;
import org.dddjava.jig.domain.model.implementation.datasource.SqlSources;
import org.dddjava.jig.domain.model.implementation.sourcecode.PackageNameSources;
import org.dddjava.jig.domain.model.implementation.sourcecode.TypeNameSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalProject {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalProject.class);

    Layout layout;

    public LocalProject(Layout layout) {
        this.layout = layout;
    }

    public ByteCodeSources getByteCodeSources() {
        ArrayList<ByteCodeSource> sources = new ArrayList<>();
        try {
            for (Path path : layout.extractClassPath()) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (isClassFile(file)) {
                            ByteCodeSource byteCodeSource = new ByteCodeSource(file);
                            sources.add(byteCodeSource);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        LOGGER.info("*.class: {}件", sources.size());
        return new ByteCodeSources(sources);
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
            Path[] array = layout.extractClassPath();

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
        // WET: org.dddjava.jig.domain.model.characteristic.Characteristic.MAPPER
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
            for (Path path : layout.extractSourcePath()) {
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

}
