package org.dddjava.jig.domain.model.sources.classsources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * classファイルの中身
 */
public record ClassFile(byte[] bytes, Path path) {

    public static ClassFile readFromPath(Path path) throws IOException {
        return new ClassFile(Files.readAllBytes(path), path);
    }

    @Override
    public String toString() {
        return "ClassSource{bytes.length=" + bytes.length + '}';
    }
}
