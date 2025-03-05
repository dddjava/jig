package org.dddjava.jig.domain.model.sources.classsources;

import java.util.Collection;

/**
 * classファイル一式
 */
public record ClassSources(Collection<ClassSource> values) {

    public boolean nothing() {
        return values.isEmpty();
    }
}
