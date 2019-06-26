package org.dddjava.jig.domain.model.implementation.source.code.javacode;

import java.util.Collections;
import java.util.List;

/**
 * package-infoソース一覧
 */
public class PackageInfoSources {

    List<PackageInfoSource> list;

    public PackageInfoSources(List<PackageInfoSource> list) {
        this.list = list;
    }

    public PackageInfoSources() {
        this(Collections.emptyList());
    }

    public List<PackageInfoSource> list() {
        return list;
    }
}
