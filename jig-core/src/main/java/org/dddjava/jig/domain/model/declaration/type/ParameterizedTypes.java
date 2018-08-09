package org.dddjava.jig.domain.model.declaration.type;

import java.util.List;
import java.util.Optional;

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

    public Optional<ParameterizedType> findOne(TypeIdentifier typeIdentifier) {
        return list.stream().filter(e -> e.typeIdentifier.equals(typeIdentifier)).findFirst();
    }
}
