package org.dddjava.jig.domain.model.valueobjects;

import org.dddjava.jig.domain.model.characteristic.ValueObjectType;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;

public class ValueObjectAngle {

    ValueObjectType valueObjectType;
    TypeIdentifier typeIdentifier;
    TypeIdentifiers userTypeIdentifiers;

    public ValueObjectAngle(ValueObjectType valueObjectType, TypeIdentifier typeIdentifier, TypeIdentifiers userTypeIdentifiers) {
        this.valueObjectType = valueObjectType;
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
