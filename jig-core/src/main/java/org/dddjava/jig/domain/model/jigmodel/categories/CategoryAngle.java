package org.dddjava.jig.domain.model.jigmodel.categories;

import org.dddjava.jig.domain.model.parts.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifiers;

/**
 * 区分の切り口
 */
public class CategoryAngle {

    public CategoryType categoryType;
    ClassRelations classRelations;

    public CategoryAngle(CategoryType categoryType, ClassRelations classRelations) {
        this.categoryType = categoryType;
        this.classRelations = classRelations;
    }

    public TypeIdentifier typeIdentifier() {
        return categoryType.typeIdentifier();
    }

    public String constantsDeclarationsName() {
        return categoryType.values().toNameText();
    }

    public String fieldDeclarations() {
        return categoryType.fieldDeclarations().toSignatureText();
    }

    public TypeIdentifiers userTypeIdentifiers() {
        return classRelations.collectTypeIdentifierWhichRelationTo(categoryType.typeIdentifier());
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

    public String nodeLabel(String delimiter) {
        return categoryType.nodeLabel(delimiter);
    }
}
