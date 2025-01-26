package org.dddjava.jig.domain.model.sources.classsources;

import java.util.List;

/**
 * classソース一覧
 */
public class ClassSources {

    List<ClassSource> sources;

    public ClassSources(List<ClassSource> sources) {
        this.sources = sources;
    }

    public List<ClassSource> list() {
        return sources;
    }
}
