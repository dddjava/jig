package org.dddjava.jig.cli;

import org.dddjava.jig.domain.model.sources.CodeSourcePaths;
import org.dddjava.jig.domain.model.sources.SourcePaths;
import org.dddjava.jig.domain.model.sources.classsources.BinarySourcePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class DirectoryCollector implements FileVisitor<Path> {
    private static final Logger logger = LoggerFactory.getLogger(DirectoryCollector.class);

    private final String directoryClasses;
    private final String directoryResources;
    private final String directorySources;

    private final List<Path> binarySourcePaths = new ArrayList<>();
    private final List<Path> textSourcesPaths = new ArrayList<>();
    
    private final AtomicLong visitCounter = new AtomicLong();

    public DirectoryCollector(String directoryClasses, String directoryResources, String directorySources) {
        this.directoryClasses = directoryClasses;
        this.directoryResources = directoryResources;
        this.directorySources = directorySources;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) {
        visitCounter.incrementAndGet();

        // pathの名前を取得する
        String pathName = path.getFileName().toString();
        if (pathName.startsWith(".")) {
            // . で始まるディレクトリは中身を見ない
            logger.debug("skip dot directory: {}", path);
            return FileVisitResult.SKIP_SUBTREE;
        }

        if (path.endsWith(directoryClasses) || path.endsWith(directoryResources)) {
            // 末尾が合致するディレクトリを見つけたら採用する
            binarySourcePaths.add(path);
            logger.debug("binary: {}", path);
            // このディレクトリの中は見る必要がないのでSKIP
            return FileVisitResult.SKIP_SUBTREE;
        }
        if (path.endsWith(directorySources)) {
            textSourcesPaths.add(path);
            logger.debug("text: {}", path);
            // このディレクトリの中は見る必要がないのでSKIP
            return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        visitCounter.incrementAndGet();
        // ファイルに対しては何もしない
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        if (exc != null) {
            logger.warn("skipped '{}'. (type={}, message={})", dir, exc.getClass().getName(), exc.getMessage());
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        logger.warn("skipped '{}'. (type={}, message={})", file, exc.getClass().getName(), exc.getMessage());
        return FileVisitResult.CONTINUE;
    }

    public SourcePaths toSourcePaths() {
        logger.debug("visitCount: {}", visitCounter.get());
        return new SourcePaths(
                new BinarySourcePaths(binarySourcePaths),
                new CodeSourcePaths(textSourcesPaths));
    }
}
