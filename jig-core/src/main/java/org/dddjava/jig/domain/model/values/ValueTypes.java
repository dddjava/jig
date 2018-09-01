package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

import java.util.List;

/**
 * 値の型一覧
 */
public class ValueTypes {
    private List<ValueType> list;

    public ValueTypes(List<ValueType> list) {
        this.list = list;
    }

    public TypeIdentifiers extract(ValueKind valueKind) {
        return list.stream()
                .filter(valueType -> valueType.is(valueKind))
                .map(ValueType::typeIdentifier)
                .collect(TypeIdentifiers.collector());
    }
}
