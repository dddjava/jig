package org.dddjava.jig.infrastructure.filesystem;

import org.dddjava.jig.domain.model.sources.SourcePaths;
import org.dddjava.jig.domain.model.sources.SourceReader;
import org.dddjava.jig.domain.model.sources.Sources;
import org.dddjava.jig.domain.model.sources.classsources.*;
import org.dddjava.jig.domain.model.sources.javasources.JavaSource;
import org.dddjava.jig.domain.model.sources.javasources.JavaSources;
import org.dddjava.jig.domain.model.sources.javasources.TextSourceType;
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

/**
 * ローカルのclassファイルを読み取るSourceReader
 */
public class LocalClassFileSourceReader implements SourceReader {

    private static final Logger logger = LoggerFactory.getLogger(LocalClassFileSourceReader.class);

    BinarySources readBinarySources(SourcePaths sourcePaths) {
        List<BinarySource> list = new ArrayList<>();
        for (Path path : sourcePaths.binarySourcePaths()) {
            try {
                List<ClassSource> sources = new ArrayList<>();
                Files.walkFileTree(path, new SimpleFileVisitor<>() {
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
                logger.warn("skip binary source '{}'. (type={}, message={})", path, e.getClass().getName(), e.getMessage());
            }
        }
        return new BinarySources(list);
    }

    JavaSources readTextSources(SourcePaths sourcePaths) {
        List<JavaSource> list = new ArrayList<>();
        for (Path path : sourcePaths.textSourcePaths()) {
            try {
                Files.walkFileTree(path, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                        JavaSource javaSource = new JavaSource(path);
                        TextSourceType textSourceType = javaSource.textSourceType();
                        if (textSourceType != TextSourceType.UNSUPPORTED) {
                            list.add(javaSource);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                logger.warn("skip text source '{}'. (type={}, message={})", path, e.getClass().getName(), e.getMessage());
            }
        }

        return new JavaSources(list);
    }

    @Override
    public Sources readSources(SourcePaths sourcePaths) {
        logger.info("read paths: binary={}, text={}", sourcePaths.binarySourcePaths(), sourcePaths.textSourcePaths());
        return new Sources(readTextSources(sourcePaths), readBinarySources(sourcePaths));
    }
}
