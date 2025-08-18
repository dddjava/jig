package org.dddjava.jig.domain.model.sources.classsources;

import java.io.IOException;
import java.nio.file.Path;

/**
 * classファイル
 */
public record ClassFilePath(Path path) {

    public static ClassFilePath readFromPath(Path path) throws IOException {
        return new ClassFilePath(path);
    }
}
