package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;

/**
 * 値の型
 */
public class ValueType {
    TypeByteCode typeByteCode;

    public ValueType(TypeByteCode typeByteCode) {
        this.typeByteCode = typeByteCode;
    }

    public boolean is(ValueKind valueKind) {
        return valueKind.matches(typeByteCode);
    }

    public TypeIdentifier typeIdentifier() {
        return typeByteCode.typeIdentifier();
    }
}
