package org.dddjava.jig.domain.model.sources.classsources;

import java.nio.file.Path;
import java.util.Collection;

/**
 * classファイル一式
 */
public record ClassFilePaths(Collection<Path> values) {

    public boolean nothing() {
        return values.isEmpty();
    }
}
