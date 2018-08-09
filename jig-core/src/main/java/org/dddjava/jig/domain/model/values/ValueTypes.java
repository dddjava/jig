package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;

import java.util.ArrayList;
import java.util.List;

/**
 * 値の型一覧
 */
public class ValueTypes {
    private List<ValueType> list;

    public ValueTypes(TypeByteCodes typeByteCodes) {
        list = new ArrayList<>();

        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            list.add(new ValueType(typeByteCode));
        }
    }

    public TypeIdentifiers extract(ValueKind valueKind) {
        return list.stream()
                .filter(valueType -> valueType.is(valueKind))
                .map(ValueType::typeIdentifier)
                .collect(TypeIdentifiers.collector());
    }
}
