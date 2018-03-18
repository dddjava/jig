package jig.infrastructure;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;

public class RecursiveFileVisitor {

    private final Consumer<Path> consumer;

    public RecursiveFileVisitor(Consumer<Path> consumer) {
        this.consumer = consumer;
    }

    public void visitAllDirectories(Path... paths) {
        for (Path path : paths) {
            visitAllFile(path);
        }
    }

    private void visitAllFile(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    consumer.accept(file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
