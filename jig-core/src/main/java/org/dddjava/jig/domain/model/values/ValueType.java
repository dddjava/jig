package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.Implementation;

/**
 * 値の型
 */
public class ValueType {
    Implementation implementation;

    public ValueType(Implementation implementation) {
        this.implementation = implementation;
    }

    public boolean is(ValueKind valueKind) {
        return valueKind.matches(implementation);
    }

    public TypeIdentifier typeIdentifier() {
        return implementation.typeIdentifier();
    }
}
