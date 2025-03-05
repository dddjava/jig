package org.dddjava.jig.domain.model.sources.classsources;

import java.util.List;

/**
 * バイナリソース一覧
 */
public record ClassSources(List<ClassSource> list) {

    public boolean nothing() {
        return list.isEmpty();
    }

}
