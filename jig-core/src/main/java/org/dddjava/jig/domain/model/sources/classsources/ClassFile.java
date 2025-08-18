package org.dddjava.jig.domain.model.sources.classsources;

import java.io.IOException;
import java.nio.file.Path;

/**
 * classファイル
 */
public record ClassFile(Path path) {

    public static ClassFile readFromPath(Path path) throws IOException {
        return new ClassFile(path);
    }
}
