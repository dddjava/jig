package org.dddjava.jig.domain.model.implementation.sourcecode;

import java.nio.file.Path;
import java.util.List;

/**
 * 型名のソース
 */
public class TypeNameSources {

    List<Path> list;

    public TypeNameSources(List<Path> list) {
        this.list = list;
    }

    public List<Path> list() {
        return list;
    }
}
