package org.dddjava.jig.domain.model.declaration.type;

import java.util.List;

/**
 * パラメータ化された型一覧
 */
public class ParameterizedTypes {
    List<ParameterizedType> list;

    public ParameterizedTypes(List<ParameterizedType> list) {
        this.list = list;
    }

    public List<ParameterizedType> list() {
        return list;
    }

    public TypeIdentifiers identifiers() {
        return list.stream().map(ParameterizedType::typeIdentifier).collect(TypeIdentifiers.collector());
    }
}
