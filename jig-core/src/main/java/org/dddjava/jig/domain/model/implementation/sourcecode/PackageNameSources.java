package org.dddjava.jig.domain.model.implementation.sourcecode;

import java.nio.file.Path;
import java.util.List;

/**
 * パッケージ名のソース
 */
public class PackageNameSources {

    List<Path> list;

    public PackageNameSources(List<Path> list) {
        this.list = list;
    }

    public List<Path> list() {
        return list;
    }
}
