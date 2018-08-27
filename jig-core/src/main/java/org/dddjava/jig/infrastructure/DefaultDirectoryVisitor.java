package org.dddjava.jig.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class DefaultDirectoryVisitor extends SimpleFileVisitor<Path> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLayout.class);

    Predicate<Path> filter;
    List<Path> paths = new ArrayList<>();

    public DefaultDirectoryVisitor(Predicate<Path> filter) {
        this.filter = filter;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (filter.test(dir)) {
            paths.add(dir);
            LOGGER.info("classes: {}", dir);
            return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        LOGGER.warn("アクセスできないディレクトリです。{}", file);
        return FileVisitResult.CONTINUE;
    }

    public Path[] asArray() {
        return paths.toArray(new Path[0]);
    }


}
