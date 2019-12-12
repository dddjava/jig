package org.dddjava.jig.domain.model.jigpresentation.categories;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;

import java.util.ArrayList;
import java.util.List;

/**
 * 区分一覧
 */
public class CategoryTypes {

    private final List<CategoryType> list;

    public CategoryTypes(BusinessRules businessRules) {
        list = new ArrayList<>();
        for (BusinessRule businessRule : businessRules.listCategory()) {
            list.add(new CategoryType(businessRule.typeByteCode()));
        }
    }

    public boolean contains(TypeIdentifier typeIdentifier) {
        return list.stream().anyMatch(categoryType -> categoryType.typeIdentifier.equals(typeIdentifier));
    }

    public List<CategoryType> list() {
        return list;
    }
}
