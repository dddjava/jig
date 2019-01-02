package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;

/**
 * 値の型
 */
public class ValueType {
    private final TypeIdentifier typeIdentifier;

    public ValueType(TypeIdentifier typeIdentifier) {

        this.typeIdentifier = typeIdentifier;
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }
}
