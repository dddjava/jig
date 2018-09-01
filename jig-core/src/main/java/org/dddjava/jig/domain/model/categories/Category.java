package org.dddjava.jig.domain.model.categories;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

/**
 * 区分
 */
public class Category {
    TypeIdentifier typeIdentifier;

    public Category(TypeIdentifier typeIdentifier) {
        this.typeIdentifier = typeIdentifier;
    }

    // TODO Characteristics.ENUM_BEHAVIOURなどはここに持ってくる
}
