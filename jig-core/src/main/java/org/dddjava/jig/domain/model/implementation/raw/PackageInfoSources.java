package org.dddjava.jig.domain.model.implementation.raw;

import java.util.List;

/**
 * package-infoソース一覧
 */
public class PackageInfoSources {

    List<PackageInfoSource> list;

    public PackageInfoSources(List<PackageInfoSource> list) {
        this.list = list;
    }

    public List<PackageInfoSource> list() {
        return list;
    }
}
