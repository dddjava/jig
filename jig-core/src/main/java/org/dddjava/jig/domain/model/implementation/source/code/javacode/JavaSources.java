package org.dddjava.jig.domain.model.implementation.source.code.javacode;

import org.dddjava.jig.domain.model.implementation.raw.SourceCodes;

import java.util.Collections;
import java.util.List;

/**
 * *.javaソース一覧
 */
public class JavaSources implements SourceCodes<JavaSource> {

    List<JavaSource> list;

    public JavaSources(List<JavaSource> list) {
        this.list = list;
    }

    public JavaSources() {
        this(Collections.emptyList());
    }

    @Override
    public List<JavaSource> list() {
        return list;
    }
}
