package org.dddjava.jig.domain.model.implementation.sourcecode;

import org.dddjava.jig.domain.model.japanese.JapaneseNameRepository;
import org.dddjava.jig.domain.model.japanese.TypeJapaneseName;

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
