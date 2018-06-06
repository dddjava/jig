package org.dddjava.jig.domain.model.categories;

import org.dddjava.jig.domain.model.characteristic.Characteristics;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.networks.TypeDependencies;

/**
 * 区分の切り口
 */
public class CategoryAngle {

    Characteristics characteristics;
    TypeIdentifier typeIdentifier;
    TypeIdentifiers userTypeIdentifiers;
    StaticFieldDeclarations constantsDeclarations;
    FieldDeclarations fieldDeclarations;

    public CategoryAngle(Characteristics characteristics, TypeIdentifier typeIdentifier, TypeIdentifiers userTypeIdentifiers, StaticFieldDeclarations constantsDeclarations, FieldDeclarations fieldDeclarations) {
        this.characteristics = characteristics;
        this.typeIdentifier = typeIdentifier;
        this.userTypeIdentifiers = userTypeIdentifiers;
        this.constantsDeclarations = constantsDeclarations;
        this.fieldDeclarations = fieldDeclarations;
    }

    public static CategoryAngle of(TypeIdentifier typeIdentifier, CharacterizedTypes characterizedTypes, TypeDependencies allTypeDependencies, FieldDeclarations allFieldDeclarations, StaticFieldDeclarations allStaticFieldDeclarations) {
        Characteristics characteristics = characterizedTypes.stream()
                .pickup(typeIdentifier)
                .characteristics();
        TypeIdentifiers userTypeIdentifiers = allTypeDependencies.stream()
                .filterTo(typeIdentifier)
                .removeSelf()
                .fromTypeIdentifiers();
        StaticFieldDeclarations constantsDeclarations = allStaticFieldDeclarations
                .filterDeclareTypeIs(typeIdentifier);
        FieldDeclarations fieldDeclarations = allFieldDeclarations
                .filterDeclareTypeIs(typeIdentifier);
        return new CategoryAngle(characteristics, typeIdentifier, userTypeIdentifiers, constantsDeclarations, fieldDeclarations);
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

    public StaticFieldDeclarations constantsDeclarations() {
        return constantsDeclarations;
    }

    public FieldDeclarations fieldDeclarations() {
        return fieldDeclarations;
    }
}
