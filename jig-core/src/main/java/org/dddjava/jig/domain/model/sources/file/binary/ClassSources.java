package org.dddjava.jig.domain.model.sources.file.binary;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

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

    public Stream<ClassSource> stream() {
        return sources.stream();
    }
}
