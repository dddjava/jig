package org.dddjava.jig.domain.model.collections;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.networks.type.TypeDependencies;

import java.util.ArrayList;
import java.util.List;

public class CollectionAngles {

    List<CollectionAngle> list;

    public CollectionAngles(TypeIdentifiers typeIdentifiers, TypeDependencies typeDependencies) {
        this.list = new ArrayList<>();
        for (TypeIdentifier typeIdentifier : typeIdentifiers.list()) {
            list.add(new CollectionAngle(typeDependencies, typeIdentifier));
        }
    }

    public List<CollectionAngle> list() {
        return list;
    }
}
