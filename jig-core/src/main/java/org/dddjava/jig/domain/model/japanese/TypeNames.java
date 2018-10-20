package org.dddjava.jig.domain.model.japanese;

import java.util.List;

/**
 * 型名一覧
 */
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
