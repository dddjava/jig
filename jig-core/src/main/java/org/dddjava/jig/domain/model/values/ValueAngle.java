package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;

public class ValueAngle {

    ValueKind valueKind;
    TypeIdentifier typeIdentifier;
    TypeIdentifiers userTypeIdentifiers;

    public ValueAngle(ValueKind valueKind, TypeIdentifier typeIdentifier, TypeIdentifiers userTypeIdentifiers) {
        this.valueKind = valueKind;
        this.typeIdentifier = typeIdentifier;
        this.userTypeIdentifiers = userTypeIdentifiers;
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public TypeIdentifiers userTypeIdentifiers() {
        return userTypeIdentifiers;
    }
}
