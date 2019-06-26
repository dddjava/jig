package org.dddjava.jig.domain.model.implementation.raw.textfile;

import java.util.List;

/**
 * テキストソース一覧
 */
public class CodeSources {

    List<AliasSource> list;

    public CodeSources(List<AliasSource> list) {
        this.list = list;
    }

    public AliasSource aliasSource() {
        return list.stream()
                .reduce(new AliasSource(), AliasSource::merge);
    }

    public boolean nothing() {
        return list.isEmpty();
    }
}
