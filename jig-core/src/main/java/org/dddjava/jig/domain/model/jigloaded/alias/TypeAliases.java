package org.dddjava.jig.domain.model.jigloaded.alias;

import java.util.Collections;
import java.util.List;

/**
 * 型名一覧
 */
public class TypeAliases {

    List<TypeAlias> list;
    List<MethodAlias> methodList;

    public TypeAliases(List<TypeAlias> list, List<MethodAlias> methodList) {
        this.list = list;
        this.methodList = methodList;
    }

    public static TypeAliases empty() {
        return new TypeAliases(Collections.emptyList(), Collections.emptyList());
    }

    public List<TypeAlias> list() {
        return list;
    }

    public List<MethodAlias> methodList() {
        return methodList;
    }
}
