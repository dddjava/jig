package org.dddjava.jig.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class DirectoryCollector extends SimpleFileVisitor<Path> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryCollector.class);

    Predicate<Path> filter;
    List<Path> paths = new ArrayList<>();

    public DirectoryCollector(Predicate<Path> filter) {
        this.filter = filter;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if (filter.test(dir)) {
            paths.add(dir);
            LOGGER.info("classes: {}", dir);
            return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        Objects.requireNonNull(dir);
        if (exc != null) {
            LOGGER.warn("skipped '{}'. (type={}, message={})", dir, exc.getClass().getName(), exc.getMessage());
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        Objects.requireNonNull(file);
        LOGGER.warn("skipped '{}'. (type={}, message={})", file, exc.getClass().getName(), exc.getMessage());
        return FileVisitResult.CONTINUE;
    }

    public List<Path> listPath() {
        return paths;
    }
}
