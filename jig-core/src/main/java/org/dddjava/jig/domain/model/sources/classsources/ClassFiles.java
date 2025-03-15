package org.dddjava.jig.domain.model.sources.classsources;

import java.util.Collection;

/**
 * classファイル一式
 */
public record ClassFiles(Collection<ClassFile> values) {

    public boolean nothing() {
        return values.isEmpty();
    }
}
