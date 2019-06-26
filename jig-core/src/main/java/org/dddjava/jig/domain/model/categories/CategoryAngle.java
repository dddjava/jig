package org.dddjava.jig.domain.model.categories;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.class_.ClassRelations;

/**
 * 区分の切り口
 */
public class CategoryAngle {

    CategoryType categoryType;
    TypeIdentifiers userTypeIdentifiers;
    StaticFieldDeclarations constantsDeclarations;
    FieldDeclarations fieldDeclarations;

    CategoryAngle(CategoryType categoryType, TypeIdentifiers userTypeIdentifiers, StaticFieldDeclarations constantsDeclarations, FieldDeclarations fieldDeclarations) {
        this.categoryType = categoryType;
        this.userTypeIdentifiers = userTypeIdentifiers;
        this.constantsDeclarations = constantsDeclarations;
        this.fieldDeclarations = fieldDeclarations;
    }

    public CategoryAngle(CategoryType categoryType, ClassRelations classRelations, FieldDeclarations fieldDeclarations, StaticFieldDeclarations staticFieldDeclarations) {
        this(categoryType,
                classRelations.collectTypeIdentifierWhichRelationTo(categoryType.typeIdentifier),
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
        return categoryType.hasParameter();
    }

    public boolean hasBehaviour() {
        return categoryType.hasBehaviour();
    }

    public boolean isPolymorphism() {
        return categoryType.isPolymorphism();
    }
}
