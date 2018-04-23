package jig.domain.model.angle;

import jig.domain.model.characteristic.Characteristics;
import jig.domain.model.declaration.field.FieldDeclarations;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;

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
}
