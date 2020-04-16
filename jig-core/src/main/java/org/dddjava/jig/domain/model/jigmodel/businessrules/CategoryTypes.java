package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

import java.util.List;

/**
 * 区分一覧
 */
public class CategoryTypes {

    private final List<CategoryType> list;

    public CategoryTypes(List<CategoryType> list) {
        this.list = list;
    }

    public boolean contains(TypeIdentifier typeIdentifier) {
        return list.stream().anyMatch(categoryType -> categoryType.typeIdentifier.equals(typeIdentifier));
    }

    public List<CategoryType> list() {
        return list;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public TypeIdentifiers typeIdentifiers() {
        return list.stream()
                .map(categoryType -> categoryType.typeIdentifier)
                .collect(TypeIdentifiers.collector());
    }
}
