package org.dddjava.jig.domain.model.categories;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

/**
 * 区分
 */
public class CategoryType {
    TypeIdentifier typeIdentifier;

    public CategoryType(TypeIdentifier typeIdentifier) {
        this.typeIdentifier = typeIdentifier;
    }

    // TODO Characteristics.ENUM_BEHAVIOURなどはここに持ってくる
}
