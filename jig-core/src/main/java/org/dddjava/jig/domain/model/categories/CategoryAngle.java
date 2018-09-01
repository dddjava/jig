package org.dddjava.jig.domain.model.categories;

import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.characteristic.Characteristics;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.networks.type.TypeDependencies;

/**
 * 区分の切り口
 */
public class CategoryAngle {

    Characteristics characteristics;
    CategoryType categoryType;
    TypeIdentifiers userTypeIdentifiers;
    StaticFieldDeclarations constantsDeclarations;
    FieldDeclarations fieldDeclarations;

    CategoryAngle(Characteristics characteristics, CategoryType categoryType, TypeIdentifiers userTypeIdentifiers, StaticFieldDeclarations constantsDeclarations, FieldDeclarations fieldDeclarations) {
        this.characteristics = characteristics;
        this.categoryType = categoryType;
        this.userTypeIdentifiers = userTypeIdentifiers;
        this.constantsDeclarations = constantsDeclarations;
        this.fieldDeclarations = fieldDeclarations;
    }

    public CategoryAngle(CategoryType categoryType, CharacterizedTypes characterizedTypes, TypeDependencies typeDependencies, FieldDeclarations fieldDeclarations, StaticFieldDeclarations staticFieldDeclarations) {
        this(characterizedTypes.stream()
                        .pickup(categoryType.typeIdentifier)
                        .characteristics(),
                categoryType,
                typeDependencies.stream()
                        .filterTo(categoryType.typeIdentifier)
                        .removeSelf()
                        .fromTypeIdentifiers()
                        .normalize(),
                staticFieldDeclarations
                        .filterDeclareTypeIs(categoryType.typeIdentifier),
                fieldDeclarations
                        .filterDeclareTypeIs(categoryType.typeIdentifier)
        );
    }

    public TypeIdentifier typeIdentifier() {
        return categoryType.typeIdentifier;
    }

    public String constantsDeclarationsName() {
        return constantsDeclarations().toNameText();
    }

    public StaticFieldDeclarations constantsDeclarations() {
        return constantsDeclarations.filterTypeSafeConstants();
    }

    public String fieldDeclarations() {
        return fieldDeclarations.toSignatureText();
    }

    public TypeIdentifiers userTypeIdentifiers() {
        return userTypeIdentifiers;
    }

    public boolean hasParameter() {
        return characteristics.has(Characteristic.ENUM_PARAMETERIZED);
    }

    public boolean hasBehaviour() {
        return characteristics.has(Characteristic.ENUM_BEHAVIOUR);
    }

    public boolean isPolymorphism() {
        return characteristics.has(Characteristic.ENUM_POLYMORPHISM);
    }
}
