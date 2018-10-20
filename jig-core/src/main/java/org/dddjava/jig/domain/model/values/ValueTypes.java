package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.ProjectData;

import java.util.ArrayList;
import java.util.List;

/**
 * 値の型一覧
 */
public class ValueTypes {
    private List<ValueType> list;

    public ValueTypes(ProjectData typeByteCodes, ValueKind valueKind) {
        list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            if (valueKind.matches(typeByteCode.fieldDeclarations())) {
                list.add(new ValueType(typeByteCode.typeIdentifier()));
            }
        }
    }

    public List<ValueType> list() {
        return list;
    }
}
