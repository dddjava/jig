package org.dddjava.jig.domain.model.jigsource.source.code.kotlincode;

import java.util.Collections;
import java.util.List;

/**
 * .ktソース一覧
 */
public class KotlinSources {

    List<KotlinSource> list;

    public KotlinSources(List<KotlinSource> list) {
        this.list = list;
    }

    public KotlinSources() {
        this(Collections.emptyList());
    }

    public List<KotlinSource> list() {
        return list;
    }
}
