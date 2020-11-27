package org.dddjava.jig.domain.model.jigmodel.categories;

import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigTypeValueKind;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifiers;

import java.util.List;
import java.util.stream.Collectors;

import static org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifiers.collector;

/**
 * 区分一覧
 */
public class CategoryTypes {

    private final List<CategoryType> list;

    public CategoryTypes(List<CategoryType> list) {
        this.list = list;
    }

    public static CategoryTypes from(BusinessRules businessRules) {
        List<CategoryType> list = businessRules.list().stream()
                .filter(businessRule -> businessRule.toValueKind() == JigTypeValueKind.区分)
                .map(businessRule -> new CategoryType(businessRule))
                .collect(Collectors.toList());
        return new CategoryTypes(list);
    }

    public boolean contains(TypeIdentifier typeIdentifier) {
        return list.stream()
                .anyMatch(categoryType -> categoryType.typeIdentifier().equals(typeIdentifier));
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
