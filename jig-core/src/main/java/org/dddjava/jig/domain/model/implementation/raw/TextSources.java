package org.dddjava.jig.domain.model.implementation.raw;

import java.util.List;

/**
 * テキストソース一覧
 */
public class TextSources {

    List<TextSource> list;

    public TextSources(List<TextSource> list) {
        this.list = list;
    }

    public TextSource toTextSource() {
        return list.stream()
                .reduce(new TextSource(), TextSource::merge);
    }

    public boolean nothing() {
        return list.isEmpty();
    }
}
