package org.dddjava.jig.domain.model.collections;

import org.dddjava.jig.domain.model.networks.type.TypeDependencies;

import java.util.ArrayList;
import java.util.List;

/**
 * コレクションの切り口一覧
 */
public class CollectionAngles {

    List<CollectionAngle> list;

    public CollectionAngles(CollectionTypes collectionTypes, TypeDependencies typeDependencies) {
        this.list = new ArrayList<>();
        for (CollectionType collectionType : collectionTypes.list()) {
            list.add(new CollectionAngle(collectionType, typeDependencies));
        }
    }

    public List<CollectionAngle> list() {
        return list;
    }
}
