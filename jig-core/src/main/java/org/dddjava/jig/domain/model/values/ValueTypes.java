package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodes;

import java.util.ArrayList;
import java.util.List;

/**
 * 値の型一覧
 */
public class ValueTypes {
    private List<ValueType> list;

    public ValueTypes(ByteCodes byteCodes) {
        list = new ArrayList<>();

        for (ByteCode byteCode : byteCodes.list()) {
            list.add(new ValueType(byteCode));
        }
    }

    public TypeIdentifiers extract(ValueKind valueKind) {
        return list.stream()
                .filter(valueType -> valueType.is(valueKind))
                .map(ValueType::typeIdentifier)
                .collect(TypeIdentifiers.collector());
    }
}
