package org.dddjava.jig.domain.model.angle;

import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;

public class GenericModelAngle {

    Characteristic characteristic;
    TypeIdentifier typeIdentifier;
    TypeIdentifiers userTypeIdentifiers;

    public GenericModelAngle(Characteristic characteristic, TypeIdentifier typeIdentifier, TypeIdentifiers userTypeIdentifiers) {
        this.characteristic = characteristic;
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
