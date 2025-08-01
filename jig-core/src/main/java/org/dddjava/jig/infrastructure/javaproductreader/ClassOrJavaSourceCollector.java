package org.dddjava.jig.infrastructure.javaproductreader;

import org.dddjava.jig.application.JigEventRepository;
import org.dddjava.jig.domain.model.sources.LocalSource;
import org.dddjava.jig.domain.model.sources.SourceBasePaths;
import org.dddjava.jig.domain.model.sources.classsources.ClassFile;
import org.dddjava.jig.domain.model.sources.classsources.ClassFiles;
import org.dddjava.jig.domain.model.sources.javasources.JavaFilePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

/**
 * classやjavaファイルを対象とするSourceReader
 */
public class ClassOrJavaSourceCollector {
    private static final Logger logger = LoggerFactory.getLogger(ClassOrJavaSourceCollector.class);

    private final JigEventRepository jigEventRepository;

    public ClassOrJavaSourceCollector(JigEventRepository jigEventRepository) {
        this.jigEventRepository = jigEventRepository;
    }

    private ClassFiles collectClassSources(SourceBasePaths sourceBasePaths) {
        var classSourceList = sourceBasePaths.classSourceBasePaths().stream()
                .map(sourceBasePath -> collectSourcePathList(sourceBasePath, ".class"))
                .flatMap(List::stream)
                .flatMap(path -> {
                    try {
                        return Stream.of(ClassFile.readFromPath(path));
                    } catch (IOException e) {
                        jigEventRepository.registerクラスファイルの読み込みに失敗しました(path, e);
                        return Stream.empty();
                    }
                })
                .toList();
        return new ClassFiles(classSourceList);
    }

    private enum JavaFileType {PackageInfoFile, JavaFile}

    private JavaFilePaths collectJavaSources(SourceBasePaths sourceBasePaths) {
        Map<JavaFileType, List<Path>> collected = sourceBasePaths.javaSourceBasePaths().stream()
                .map(basePath -> collectSourcePathList(basePath, ".java"))
                .flatMap(List::stream)
                .collect(groupingBy(path -> path.getFileName().toString().equals("package-info.java") ? JavaFileType.PackageInfoFile : JavaFileType.JavaFile));
        return new JavaFilePaths(collected.getOrDefault(JavaFileType.PackageInfoFile, List.of()), collected.getOrDefault(JavaFileType.JavaFile, List.of()));
    }

    private List<Path> collectSourcePathList(Path basePath, String suffix) {
        if (!Files.exists(basePath)) {
            jigEventRepository.register指定されたパスが存在しない(basePath);
            return List.of();
        }
        try (Stream<Path> pathStream = Files.walk(basePath)) {
            return pathStream
                    .filter(path -> path.getFileName().toString().endsWith(suffix))
                    .toList();
        } catch (IOException e) {
            jigEventRepository.registerパスの収集に失敗しました(basePath, e);
            return List.of();
        }
    }

    public LocalSource collectSources(SourceBasePaths sourceBasePaths) {
        logger.info("read paths: {}", sourceBasePaths);
        return new LocalSource(sourceBasePaths, collectJavaSources(sourceBasePaths), collectClassSources(sourceBasePaths));
    }
}
