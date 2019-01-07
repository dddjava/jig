package org.dddjava.jig.domain.model.collections;

import org.dddjava.jig.domain.model.implementation.analyzed.networks.type.TypeRelations;

import java.util.ArrayList;
import java.util.List;

/**
 * コレクションの切り口一覧
 */
public class CollectionAngles {

    List<CollectionAngle> list;

    public CollectionAngles(CollectionTypes collectionTypes, TypeRelations typeRelations) {
        this.list = new ArrayList<>();
        for (CollectionType collectionType : collectionTypes.list()) {
            list.add(new CollectionAngle(collectionType, typeRelations));
        }
    }

    public List<CollectionAngle> list() {
        return list;
    }
}
