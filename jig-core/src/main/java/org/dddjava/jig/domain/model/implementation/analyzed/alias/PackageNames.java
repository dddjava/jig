package org.dddjava.jig.domain.model.implementation.analyzed.alias;

import java.util.List;

/**
 * パッケージ名一覧
 */
public class PackageNames {
    List<PackageAlias> list;

    public PackageNames(List<PackageAlias> list) {
        this.list = list;
    }

    public void register(AliasRepository aliasRepository) {
        list.forEach(aliasRepository::register);
    }
}
