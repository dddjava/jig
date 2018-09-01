package org.dddjava.jig.domain.model.categories;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

/**
 * 区分一覧
 */
public class Categories {

    private final TypeIdentifiers typeIdentifiers;

    public Categories(TypeIdentifiers typeIdentifiers) {
        this.typeIdentifiers = typeIdentifiers;
    }

    public boolean contains(TypeIdentifier typeIdentifier) {
        return typeIdentifiers.contains(typeIdentifier);
    }
}
