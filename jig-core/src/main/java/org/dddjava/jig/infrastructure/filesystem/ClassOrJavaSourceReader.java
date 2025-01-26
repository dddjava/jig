package org.dddjava.jig.infrastructure.filesystem;

import org.dddjava.jig.domain.model.sources.SourceBasePaths;
import org.dddjava.jig.domain.model.sources.SourceReader;
import org.dddjava.jig.domain.model.sources.Sources;
import org.dddjava.jig.domain.model.sources.classsources.ClassSource;
import org.dddjava.jig.domain.model.sources.classsources.ClassSources;
import org.dddjava.jig.domain.model.sources.javasources.JavaSources;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * classやjavaファイルを対象とするSourceReader
 */
public class ClassOrJavaSourceReader implements SourceReader {

    private static final Logger logger = LoggerFactory.getLogger(ClassOrJavaSourceReader.class);

    ClassSources collectClassSources(SourceBasePaths sourceBasePaths) {
        var classSourceList = sourceBasePaths.classSourceBasePaths().stream()
                .map(sourceBasePath -> collectSourcePathList(sourceBasePath, ".class"))
                .flatMap(List::stream)
                .map(path -> {
                    try {
                        byte[] bytes = Files.readAllBytes(path);
                        ClassReader classReader = new ClassReader(bytes);
                        // TODO このクラス名の用途がMyBatisでロードするためだけなのでほとんどのクラスで意味がない。不要にしたい。
                        String className = classReader.getClassName().replace('/', '.');
                        return new ClassSource(bytes, className);
                    } catch (IOException e) {
                        // スタックトレースが出ない環境での実行を考慮して、例外型とメッセージは出すようにしておく
                        logger.warn("skip class source '{}'. (type={}, message={})", path, e.getClass().getName(), e.getMessage(), e);
                        return null;
                    }
                })
                .filter(classSource -> classSource != null)
                .toList();
        return new ClassSources(classSourceList);
    }

    JavaSources collectJavaSources(SourceBasePaths sourceBasePaths) {
        return sourceBasePaths.javaSourceBasePaths().stream()
                .map(basePath -> collectSourcePathList(basePath, ".java"))
                .flatMap(List::stream)
                .collect(collectingAndThen(toList(), JavaSources::new));
    }

    private List<Path> collectSourcePathList(Path basePath, String suffix) {
        try (Stream<Path> pathStream = Files.walk(basePath)) {
            return pathStream
                    .filter(path -> path.getFileName().toString().endsWith(suffix))
                    .toList();
        } catch (IOException e) {
            // スタックトレースが出ない環境での実行を考慮して、例外型とメッセージは出すようにしておく
            logger.warn("skip collect java source '{}'. (type={}, message={})", basePath, e.getClass().getName(), e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public Sources readSources(SourceBasePaths sourceBasePaths) {
        logger.info("read paths: binary={}, text={}", sourceBasePaths.classSourceBasePaths(), sourceBasePaths.javaSourceBasePaths());
        return new Sources(sourceBasePaths, collectJavaSources(sourceBasePaths), collectClassSources(sourceBasePaths));
    }
}
