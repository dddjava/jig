package org.dddjava.jig.domain.model.jigmodel.collections;

import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * コレクションの切り口一覧
 */
public class CollectionAngles {

    List<CollectionAngle> list;

    public CollectionAngles(BusinessRules businessRules, ClassRelations classRelations) {
        this.list = new ArrayList<>();
        for (BusinessRule businessRule : businessRules.list()) {
            list.add(new CollectionAngle(businessRule.jigType(), classRelations));
        }
    }

    public List<CollectionAngle> list() {
        return list.stream()
                .sorted(Comparator.comparing(collectionAngle -> collectionAngle.typeIdentifier()))
                .collect(Collectors.toList());
    }
}
