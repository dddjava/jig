package org.dddjava.jig.domain.model.categories;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.interpret.relation.class_.ClassRelations;

/**
 * 区分の切り口
 */
public class CategoryAngle {

    CategoryType categoryType;
    TypeIdentifiers userTypeIdentifiers;
    StaticFieldDeclarations constantsDeclarations;
    FieldDeclarations fieldDeclarations;

    public CategoryAngle(CategoryType categoryType, ClassRelations classRelations, FieldDeclarations fieldDeclarations, StaticFieldDeclarations staticFieldDeclarations) {
        this.categoryType = categoryType;
        this.userTypeIdentifiers = classRelations.collectTypeIdentifierWhichRelationTo(categoryType.typeIdentifier);
        this.constantsDeclarations = staticFieldDeclarations.filterDeclareTypeIs(categoryType.typeIdentifier);
        this.fieldDeclarations = fieldDeclarations.filterDeclareTypeIs(categoryType.typeIdentifier);
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
