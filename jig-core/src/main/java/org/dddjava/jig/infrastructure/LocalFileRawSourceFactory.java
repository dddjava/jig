package org.dddjava.jig.infrastructure;

import org.dddjava.jig.domain.model.implementation.raw.*;
import org.dddjava.jig.domain.model.implementation.raw.binary.BinarySource;
import org.dddjava.jig.domain.model.implementation.raw.binary.BinarySources;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class LocalFileRawSourceFactory implements RawSourceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileRawSourceFactory.class);

    BinarySources readBinarySources(RawSourceLocations rawSourceLocations) {
        List<BinarySource> list = new ArrayList<>();
        for (Path path : rawSourceLocations.binarySourcePaths()) {
            try {
                List<ClassSource> sources = new ArrayList<>();
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (file.toString().endsWith(".class")) {
                            try {
                                byte[] bytes = Files.readAllBytes(file);
                                ClassReader classReader = new ClassReader(bytes);
                                String className = classReader.getClassName().replace('/', '.');
                                ClassSource classSource = new ClassSource(new SourceLocation(file), bytes, className);
                                sources.add(classSource);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                list.add(new BinarySource(new SourceLocation(path), new ClassSources(sources)));
            } catch (IOException e) {
                LOGGER.warn("skipped '{}'. (type={}, message={})", path, e.getClass().getName(), e.getMessage());
            }
        }
        return new BinarySources(list);
    }

    TextSources readTextSources(RawSourceLocations rawSourceLocations) {
        List<TextSource> list = new ArrayList<>();
        for (Path path : rawSourceLocations.textSourcePaths()) {
            try {
                List<JavaSource> javaSources = new ArrayList<>();
                List<PackageInfoSource> packageInfoSources = new ArrayList<>();
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        try {
                            SourceFilePath sourceFilePath = new SourceFilePath(file);
                            if (sourceFilePath.isJava()) {
                                JavaSource javaSource = new JavaSource(sourceFilePath, Files.readAllBytes(file));
                                if (sourceFilePath.isPackageInfo()) {
                                    packageInfoSources.add(new PackageInfoSource(javaSource));
                                } else {
                                    javaSources.add(javaSource);
                                }
                            }

                            return FileVisitResult.CONTINUE;
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
                });
                list.add(new TextSource(new SourceLocation(path), new JavaSources(javaSources), new PackageInfoSources(packageInfoSources)));
            } catch (IOException e) {
                LOGGER.warn("skipped '{}'. (type={}, message={})", path, e.getClass().getName(), e.getMessage());
            }
        }

        return new TextSources(list);
    }

    @Override
    public RawSource createSource(RawSourceLocations rawSourceLocations) {
        return new RawSource(readTextSources(rawSourceLocations), readBinarySources(rawSourceLocations));
    }
}
