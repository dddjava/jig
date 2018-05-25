package org.dddjava.jig.domain.model.implementation.bytecode;

import java.nio.file.Path;

/**
 * バイトコードのソース（classファイル）
 */
public class ByteCodeSource {
    private final Path path;

    public ByteCodeSource(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }
}
