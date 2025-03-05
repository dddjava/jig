package org.dddjava.jig.domain.model.sources.classsources;

import java.util.Collection;

/**
 * バイナリソース一覧
 */
public record ClassSources(Collection<ClassSource> values) {

    public boolean nothing() {
        return values.isEmpty();
    }
}
