package org.dddjava.jig.infrastructure;

import org.dddjava.jig.domain.model.implementation.raw.*;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class LocalProject {

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
            }
            return new BinarySources(list);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
