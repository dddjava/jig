package org.dddjava.jig.domain.model.collections;

import org.dddjava.jig.domain.model.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.businessrules.BusinessRules;

import java.util.ArrayList;
import java.util.List;

/**
 * コレクション一覧
 */
public class CollectionTypes {

    List<CollectionType> list;

    public CollectionTypes(BusinessRules businessRules) {
        list = new ArrayList<>();
        for (BusinessRule businessRule : businessRules.listCollection()) {
            list.add(new CollectionType(businessRule, new CollectionField(businessRule.fields())));
        }
    }

    public List<CollectionType> list() {
        return list;
    }
}
