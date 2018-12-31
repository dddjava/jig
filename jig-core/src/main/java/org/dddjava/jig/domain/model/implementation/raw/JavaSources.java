package org.dddjava.jig.domain.model.implementation.raw;

import java.util.List;

/**
 * *.javaソース一覧
 */
public class JavaSources {

    List<JavaSource> list;

    public JavaSources(List<JavaSource> list) {
        this.list = list;
    }

    public List<JavaSource> list() {
        return list;
    }
}
