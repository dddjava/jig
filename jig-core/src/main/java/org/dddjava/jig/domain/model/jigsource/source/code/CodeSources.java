package org.dddjava.jig.domain.model.jigsource.source.code;

import java.util.List;

/**
 * テキストソース一覧
 */
public class CodeSources {

    List<CodeSource> list;

    public CodeSources(List<CodeSource> list) {
        this.list = list;
    }

    public AliasSource aliasSource() {
        return list.stream()
                .map(codeSource -> new AliasSource(codeSource))
                .reduce(new AliasSource(), AliasSource::merge);
    }

    public boolean nothing() {
        return list.isEmpty();
    }
}
