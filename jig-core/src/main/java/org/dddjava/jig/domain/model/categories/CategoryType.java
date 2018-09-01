package org.dddjava.jig.domain.model.categories;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

/**
 * 区分
 */
public class CategoryType {
    TypeIdentifier typeIdentifier;
    CategoryCharacteristics categoryCharacteristics;

    public CategoryType(TypeIdentifier typeIdentifier, CategoryCharacteristics categoryCharacteristics) {
        this.typeIdentifier = typeIdentifier;
        this.categoryCharacteristics = categoryCharacteristics;
    }

    public boolean hasParameter() {
        return categoryCharacteristics.contains(CategoryCharacteristic.PARAMETERIZED);
    }

    public boolean hasBehaviour() {
        return categoryCharacteristics.contains(CategoryCharacteristic.BEHAVIOUR);
    }

    public boolean isPolymorphism() {
        return categoryCharacteristics.contains(CategoryCharacteristic.POLYMORPHISM);
    }
}
