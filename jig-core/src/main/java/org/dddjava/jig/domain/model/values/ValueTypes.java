package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;

import java.util.ArrayList;
import java.util.List;

public class ValueTypes {
    private List<ValueType> list = new ArrayList<>();

    public void add(ValueType valueType) {
        list.add(valueType);
    }

    public TypeIdentifiers extract(ValueKind valueKind) {
        return list.stream()
                .filter(valueType -> valueType.is(valueKind))
                .map(ValueType::typeIdentifier)
                .collect(TypeIdentifiers.collector());
    }
}
