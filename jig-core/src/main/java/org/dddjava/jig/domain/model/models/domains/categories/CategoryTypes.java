package org.dddjava.jig.domain.model.models.domains.categories;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifiers;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifiers.collector;

/**
 * 区分一覧
 */
public class CategoryTypes {

    private final List<CategoryType> list;

    public CategoryTypes(List<CategoryType> list) {
        this.list = list;
    }

    public static CategoryTypes from(JigTypes jigTypes) {
        return jigTypes.listCategoryType().stream()
                .map(CategoryType::new)
                .collect(collectingAndThen(Collectors.toList(), CategoryTypes::new));
    }

    public List<CategoryType> list() {
        return list;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public TypeIdentifiers typeIdentifiers() {
        return list.stream()
                .map(CategoryType::typeIdentifier)
                .collect(collector());
    }
}
