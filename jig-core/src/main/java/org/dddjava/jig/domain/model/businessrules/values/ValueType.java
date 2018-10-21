package org.dddjava.jig.domain.model.businessrules.values;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

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
