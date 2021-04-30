package org.dddjava.jig.domain.model.parts.alias;

import java.util.List;

/**
 * パッケージ名一覧
 */
public class PackageAliases {
    List<PackageAlias> list;

    public PackageAliases(List<PackageAlias> list) {
        this.list = list;
    }

    public List<PackageAlias> list() {
        return list;
    }
}
