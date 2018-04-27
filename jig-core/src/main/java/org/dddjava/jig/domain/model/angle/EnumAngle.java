package org.dddjava.jig.domain.model.angle;

import org.dddjava.jig.domain.model.characteristic.Characteristics;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;

public class EnumAngle {

    Characteristics characteristics;
    TypeIdentifier typeIdentifier;
    TypeIdentifiers userTypeIdentifiers;
    FieldDeclarations constantsDeclarations;
    FieldDeclarations fieldDeclarations;

    public EnumAngle(Characteristics characteristics, TypeIdentifier typeIdentifier, TypeIdentifiers userTypeIdentifiers, FieldDeclarations constantsDeclarations, FieldDeclarations fieldDeclarations) {
        this.characteristics = characteristics;
        this.typeIdentifier = typeIdentifier;
        this.userTypeIdentifiers = userTypeIdentifiers;
        this.constantsDeclarations = constantsDeclarations;
        this.fieldDeclarations = fieldDeclarations;
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public Characteristics characteristics() {
        return characteristics;
    }

    public TypeIdentifiers userTypeIdentifiers() {
        return userTypeIdentifiers;
    }

    public FieldDeclarations constantsDeclarations() {
        return constantsDeclarations;
    }

    public FieldDeclarations fieldDeclarations() {
        return fieldDeclarations;
    }
}
