package org.dddjava.jig.domain.model.implementation.source.code.kotlincode;

import org.dddjava.jig.domain.model.implementation.raw.SourceCodes;

import java.util.Collections;
import java.util.List;

/**
 * .ktソース一覧
 */
public class KotlinSources implements SourceCodes<KotlinSource> {

    List<KotlinSource> list;

    public KotlinSources(List<KotlinSource> list) {
        this.list = list;
    }

    public KotlinSources() {
        this(Collections.emptyList());
    }

    @Override
    public List<KotlinSource> list() {
        return list;
    }
}
