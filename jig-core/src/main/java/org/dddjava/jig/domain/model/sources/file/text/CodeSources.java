package org.dddjava.jig.domain.model.sources.file.text;

import java.util.List;

/**
 * テキストソース一覧
 */
public class CodeSources {

    List<CodeSource> list;

    public CodeSources(List<CodeSource> list) {
        this.list = list;
    }

    public TextSources aliasSource() {
        return list.stream()
                .map(codeSource -> new TextSources(codeSource))
                .reduce(new TextSources(), TextSources::merge);
    }

    public boolean nothing() {
        return list.isEmpty();
    }
}
