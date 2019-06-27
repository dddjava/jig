package org.dddjava.jig.domain.model.categories;

import org.dddjava.jig.domain.model.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 区分一覧
 */
public class CategoryTypes {

    private final List<CategoryType> list;

    public CategoryTypes(BusinessRules businessRules) {
        list = new ArrayList<>();
        for (BusinessRule businessRule: businessRules.list()) {
            TypeByteCode typeByteCode = businessRule.typeByteCode();
            if (typeByteCode.isEnum()) {
                list.add(new CategoryType(typeByteCode));
            }
        }
    }

    public boolean contains(TypeIdentifier typeIdentifier) {
        return list.stream().anyMatch(categoryType -> categoryType.typeIdentifier.equals(typeIdentifier));
    }

    public List<CategoryType> list() {
        return list;
    }
}
