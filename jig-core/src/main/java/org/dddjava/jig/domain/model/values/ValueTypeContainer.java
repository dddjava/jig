package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;

import java.util.ArrayList;
import java.util.List;

// TODO 一時的なコンテナ
public class ValueTypeContainer {
    private List<ValueTypes> list = new ArrayList<>();

    public void add(ValueTypes valueTypes) {
        list.add(valueTypes);
    }

    public TypeIdentifiers extract(ValueType valueType) {
        return list.stream()
                .filter(valueTypes -> valueTypes.contains(valueType))
                .map(ValueTypes::typeIdentifier)
                .collect(TypeIdentifiers.collector());
    }
}
