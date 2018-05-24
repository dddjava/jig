package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.bytecode.Implementation;
import org.dddjava.jig.domain.model.implementation.bytecode.Implementations;

import java.util.ArrayList;
import java.util.List;

public class ValueTypes {
    private List<ValueType> list;

    public ValueTypes(Implementations implementations) {
        list = new ArrayList<>();

        for (Implementation implementation : implementations.list()) {
            list.add(new ValueType(implementation));
        }
    }

    public TypeIdentifiers extract(ValueKind valueKind) {
        return list.stream()
                .filter(valueType -> valueType.is(valueKind))
                .map(ValueType::typeIdentifier)
                .collect(TypeIdentifiers.collector());
    }
}
