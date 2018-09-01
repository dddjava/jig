package org.dddjava.jig.domain.model.categories;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 区分一覧
 */
public class CategoryTypes {

    private final TypeIdentifiers typeIdentifiers;

    public CategoryTypes(TypeIdentifiers typeIdentifiers) {
        this.typeIdentifiers = typeIdentifiers;
    }

    public boolean contains(TypeIdentifier typeIdentifier) {
        return typeIdentifiers.contains(typeIdentifier);
    }

    public List<CategoryType> list() {
        return typeIdentifiers.list().stream().map(CategoryType::new).collect(Collectors.toList());
    }
}
