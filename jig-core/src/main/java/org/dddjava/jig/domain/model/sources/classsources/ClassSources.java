package org.dddjava.jig.domain.model.sources.classsources;

import java.util.ArrayList;
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

    ClassSources merge(ClassSources other) {
        List<ClassSource> list = new ArrayList<>(list());
        list.addAll(other.list());
        return new ClassSources(list);
    }
}
