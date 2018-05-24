package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;

import java.util.List;

public class ValueTypes {
    TypeIdentifier typeIdentifier;
    List<ValueType> list;

    public ValueTypes(TypeIdentifier typeIdentifier, List<ValueType> list) {
        this.typeIdentifier = typeIdentifier;
        this.list = list;
    }

    public boolean contains(ValueType valueType) {
        return list.contains(valueType);
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }
}
