package org.dddjava.jig.domain.model.jigdocumenter.collections;

import org.dddjava.jig.domain.model.jigmodel.relation.class_.ClassRelations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * コレクションの切り口一覧
 */
public class CollectionAngles {

    List<CollectionAngle> list;

    public CollectionAngles(CollectionTypes collectionTypes, ClassRelations classRelations) {
        this.list = new ArrayList<>();
        for (CollectionType collectionType : collectionTypes.list()) {
            list.add(new CollectionAngle(collectionType, classRelations));
        }
    }

    public List<CollectionAngle> list() {
        return list.stream()
                .sorted(Comparator.comparing(collectionAngle -> collectionAngle.typeIdentifier()))
                .collect(Collectors.toList());
    }
}
