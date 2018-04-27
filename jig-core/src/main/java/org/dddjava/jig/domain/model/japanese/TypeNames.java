package org.dddjava.jig.domain.model.japanese;

import java.util.List;

public class TypeNames {

    List<TypeJapaneseName> list;

    public TypeNames(List<TypeJapaneseName> list) {
        this.list = list;
    }

    public void register(JapaneseNameRepository japaneseNameRepository) {
        list.forEach(japaneseNameRepository::register);
    }
}
