package org.dddjava.jig.domain.model.sources.classsources;

import java.util.Collection;

/**
 * classファイル一式
 */
public record ClassFilePaths(Collection<ClassFilePath> values) {

    public boolean nothing() {
        return values.isEmpty();
    }
}
