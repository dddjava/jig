package org.dddjava.jig.infrastructure.filesystem;

import org.dddjava.jig.domain.model.fact.source.SourcePaths;
import org.dddjava.jig.domain.model.fact.source.SourceReader;
import org.dddjava.jig.domain.model.fact.source.Sources;
import org.dddjava.jig.domain.model.fact.source.binary.*;
import org.dddjava.jig.domain.model.fact.source.code.CodeSource;
import org.dddjava.jig.domain.model.fact.source.code.CodeSourceFile;
import org.dddjava.jig.domain.model.fact.source.code.CodeSources;
import org.dddjava.jig.domain.model.fact.source.code.javacode.*;
import org.dddjava.jig.domain.model.fact.source.code.kotlincode.KotlinSource;
import org.dddjava.jig.domain.model.fact.source.code.kotlincode.KotlinSourceFile;
import org.dddjava.jig.domain.model.fact.source.code.kotlincode.KotlinSources;
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

public class LocalFileSourceReader implements SourceReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileSourceReader.class);

    BinarySources readBinarySources(SourcePaths sourcePaths) {
        List<BinarySource> list = new ArrayList<>();
        for (Path path : sourcePaths.binarySourcePaths()) {
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
                                ClassSource classSource = new ClassSource(new BinarySourceLocation(file), bytes, className);
                                sources.add(classSource);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                list.add(new BinarySource(new BinarySourceLocation(path), new ClassSources(sources)));
            } catch (IOException e) {
                LOGGER.warn("skipped '{}'. (type={}, message={})", path, e.getClass().getName(), e.getMessage());
            }
        }
        return new BinarySources(list);
    }

    CodeSources readTextSources(SourcePaths sourcePaths) {
        List<CodeSource> list = new ArrayList<>();
        for (Path path : sourcePaths.textSourcePaths()) {
            try {
                List<JavaSource> javaSources = new ArrayList<>();
                List<KotlinSource> kotlinSources = new ArrayList<>();
                List<PackageInfoSource> packageInfoSources = new ArrayList<>();
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        try {
                            CodeSourceFile codeSourceFile = new CodeSourceFile(file);
                            JavaSourceFile javaSourceFile = codeSourceFile.asJava();
                            KotlinSourceFile kotlinSourceFile = codeSourceFile.asKotlin();
                            if (javaSourceFile.isJava()) {
                                JavaSource javaSource = new JavaSource(javaSourceFile, Files.readAllBytes(file));
                                if (javaSourceFile.isPackageInfo()) {
                                    packageInfoSources.add(new PackageInfoSource(javaSource));
                                } else {
                                    javaSources.add(javaSource);
                                }
                            } else if (kotlinSourceFile.isKotlin()) {
                                KotlinSource kotlinSource = new KotlinSource(kotlinSourceFile, Files.readAllBytes(file));
                                kotlinSources.add(kotlinSource);
                            }

                            return FileVisitResult.CONTINUE;
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
                });
                list.add(new CodeSource(new JavaSources(javaSources), new KotlinSources(kotlinSources), new PackageInfoSources(packageInfoSources)));
            } catch (IOException e) {
                LOGGER.warn("skipped '{}'. (type={}, message={})", path, e.getClass().getName(), e.getMessage());
            }
        }

        return new CodeSources(list);
    }

    @Override
    public Sources readSources(SourcePaths sourcePaths) {
        return new Sources(readTextSources(sourcePaths), readBinarySources(sourcePaths));
    }
}
