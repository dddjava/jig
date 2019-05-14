package org.dddjava.jig.domain.model.implementation.analyzed.alias;

import java.util.List;

/**
 * 型名一覧
 */
public class TypeNames {

    List<TypeAlias> list;
    List<MethodAlias> methodList;

    public TypeNames(List<TypeAlias> list, List<MethodAlias> methodList) {
        this.list = list;
        this.methodList = methodList;
    }

    public List<TypeAlias> list() {
        return list;
    }

    public List<MethodAlias> methodList() {
        return methodList;
    }
}
