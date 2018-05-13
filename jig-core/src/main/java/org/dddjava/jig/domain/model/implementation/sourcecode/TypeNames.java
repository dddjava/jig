package org.dddjava.jig.domain.model.implementation.sourcecode;

import org.dddjava.jig.domain.model.japanese.MethodJapaneseName;
import org.dddjava.jig.domain.model.japanese.TypeJapaneseName;

import java.util.List;

public class TypeNames {

    List<TypeJapaneseName> list;
    List<MethodJapaneseName> methodList;

    public TypeNames(List<TypeJapaneseName> list, List<MethodJapaneseName> methodList) {
        this.list = list;
        this.methodList = methodList;
    }

    public List<TypeJapaneseName> list() {
        return list;
    }

    public List<MethodJapaneseName> methodList() {
        return methodList;
    }
}
