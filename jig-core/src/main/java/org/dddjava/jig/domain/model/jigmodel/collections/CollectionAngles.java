package org.dddjava.jig.domain.model.jigmodel.collections;

import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigType;
import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigTypeValueKind;
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
            JigType jigType = businessRule.jigType();
            if (jigType.toValueKind() == JigTypeValueKind.コレクション) {
                list.add(new CollectionAngle(jigType, classRelations));
            }
        }
    }

    public List<CollectionAngle> list() {
        return list.stream()
                .sorted(Comparator.comparing(collectionAngle -> collectionAngle.jigType().identifier()))
                .collect(Collectors.toList());
    }
}
