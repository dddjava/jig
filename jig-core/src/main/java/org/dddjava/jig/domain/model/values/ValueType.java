package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCode;

/**
 * 値の型
 */
public class ValueType {
    ByteCode byteCode;

    public ValueType(ByteCode byteCode) {
        this.byteCode = byteCode;
    }

    public boolean is(ValueKind valueKind) {
        return valueKind.matches(byteCode);
    }

    public TypeIdentifier typeIdentifier() {
        return byteCode.typeIdentifier();
    }
}
