package org.dddjava.jig.infrastructure;

import org.dddjava.jig.domain.model.implementation.datasource.SqlSources;
import org.dddjava.jig.domain.model.implementation.raw.*;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalProject {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalProject.class);

    Layout layout;

    public LocalProject(Layout layout) {
        this.layout = layout;
    }

    BinarySources readBinarySources() {
        try {
            List<BinarySource> list = new ArrayList<>();
            for (Path path : layout.extractClassPath()) {
                List<ClassSource> sources = new ArrayList<>();
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (file.toString().endsWith(".class")) {
                            try {
                                byte[] bytes = Files.readAllBytes(file);
                                ClassSource classSource = new ClassSource(bytes);
                                sources.add(classSource);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                list.add(new BinarySource(new SourceLocation(path), new ClassSources(sources)));
            }
            return new BinarySources(list);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
        return path.toString().endsWith("Mapper.class");
    }

    private String toClassName(Path path) {
        String pathStr = path.toString();
        return pathStr.substring(0, pathStr.length() - 6).replace(File.separatorChar, '.');
    }

    TextSources readTextSources() {
        try {
            List<TextSource> list = new ArrayList<>();
            for (Path path : layout.extractSourcePath()) {
                List<JavaSource> javaSources = new ArrayList<>();
                List<PackageInfoSource> packageInfoSources = new ArrayList<>();
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        try {
                            String name = file.toString();
                            if (name.endsWith("package-info.java")) {
                                packageInfoSources.add(new PackageInfoSource(Files.readAllBytes(file)));
                            } else if (name.endsWith(".java")) {
                                javaSources.add(new JavaSource(Files.readAllBytes(file)));
                            }

                            return FileVisitResult.CONTINUE;
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
                });
                list.add(new TextSource(new SourceLocation(path), new JavaSources(javaSources), new PackageInfoSources(packageInfoSources)));
            }

            return new TextSources(list);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public RawSource createSource() {
        return new RawSource(readTextSources(), readBinarySources());
    }
}
