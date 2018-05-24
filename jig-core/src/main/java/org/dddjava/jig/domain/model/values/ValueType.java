package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.Implementation;

/**
 * 値の型
 */
public class ValueType {
    TypeIdentifier typeIdentifier;
    Implementation implementation;

    public ValueType(TypeIdentifier typeIdentifier, Implementation implementation) {
        this.typeIdentifier = typeIdentifier;
        this.implementation = implementation;
    }

    public boolean is(ValueKind valueKind) {
        return valueKind.matches(implementation);
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }
}
