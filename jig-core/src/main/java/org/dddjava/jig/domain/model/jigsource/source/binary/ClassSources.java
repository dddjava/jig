package org.dddjava.jig.domain.model.jigsource.source.binary;

import java.util.Collections;
import java.util.List;

/**
 * classソース一覧
 */
public class ClassSources {

    List<ClassSource> sources;

    public ClassSources(List<ClassSource> sources) {
        this.sources = sources;
    }

    public ClassSources() {
        this(Collections.emptyList());
    }

    public List<ClassSource> list() {
        return sources;
    }

    public boolean notFound() {
        return sources.isEmpty();
    }
}
