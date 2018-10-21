package org.dddjava.jig.domain.model.businessrules.values;

import org.dddjava.jig.domain.model.architecture.Architecture;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;

import java.util.ArrayList;
import java.util.List;

/**
 * 値の型一覧
 */
public class ValueTypes {
    private List<ValueType> list;

    public ValueTypes(TypeByteCodes typeByteCodes, ValueKind valueKind, Architecture architecture) {
        list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            if (!architecture.isBusinessRule(typeByteCode.typeIdentifier())) {
                continue;
            }
            if (typeByteCode.isEnum()) {
                continue;
            }
            if (valueKind.matches(typeByteCode.fieldDeclarations())) {
                list.add(new ValueType(typeByteCode.typeIdentifier()));
            }
        }
    }

    public List<ValueType> list() {
        return list;
    }
}
