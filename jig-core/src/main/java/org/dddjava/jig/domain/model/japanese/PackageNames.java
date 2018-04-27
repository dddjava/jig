package org.dddjava.jig.domain.model.japanese;

import java.util.List;

public class PackageNames {
    List<PackageJapaneseName> list;

    public PackageNames(List<PackageJapaneseName> list) {
        this.list = list;
    }

    public void register(JapaneseNameRepository japaneseNameRepository) {
        list.forEach(japaneseNameRepository::register);
    }
}
