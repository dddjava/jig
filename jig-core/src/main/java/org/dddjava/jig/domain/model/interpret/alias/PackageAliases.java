package org.dddjava.jig.domain.model.interpret.alias;

import java.util.List;

/**
 * パッケージ名一覧
 */
public class PackageAliases {
    List<PackageAlias> list;

    public PackageAliases(List<PackageAlias> list) {
        this.list = list;
    }

    public void register(AliasRepository aliasRepository) {
        list.forEach(aliasRepository::register);
    }
}
