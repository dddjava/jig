package org.dddjava.jig.domain.model.implementation.sourcecode;

import org.dddjava.jig.domain.model.japanese.JapaneseNameRepository;
import org.dddjava.jig.domain.model.japanese.PackageJapaneseName;

import java.util.List;

/**
 * パッケージ名一覧
 */
public class PackageNames {
    List<PackageJapaneseName> list;

    public PackageNames(List<PackageJapaneseName> list) {
        this.list = list;
    }

    public void register(JapaneseNameRepository japaneseNameRepository) {
        list.forEach(japaneseNameRepository::register);
    }
}
