package org.dddjava.jig.domain.model.knowledge.core;

import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.information.domains.categories.CategoryType;

/**
 * 区分の切り口
 */
public class CategoryAngle {

    public CategoryType categoryType;

    public CategoryAngle(CategoryType categoryType) {
        this.categoryType = categoryType;
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
