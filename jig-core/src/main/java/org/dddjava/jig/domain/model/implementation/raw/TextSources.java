package org.dddjava.jig.domain.model.implementation.raw;

import java.util.List;

public class TextSources {

    List<TextSource> list;

    public TextSources(List<TextSource> list) {
        this.list = list;
    }

    public TextSource toTextSource() {
        return list.stream()
                .reduce(new TextSource(), TextSource::merge);
    }
}
