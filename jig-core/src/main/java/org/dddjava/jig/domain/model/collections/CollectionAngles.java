package org.dddjava.jig.domain.model.collections;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.networks.type.TypeDependencies;
import org.dddjava.jig.domain.model.unit.method.Methods;

import java.util.ArrayList;
import java.util.List;

/**
 * コレクションの切り口一覧
 */
public class CollectionAngles {

    List<CollectionAngle> list;

    public CollectionAngles(TypeIdentifiers typeIdentifiers, FieldDeclarations fieldDeclarations, Methods methods, TypeDependencies typeDependencies) {
        this.list = new ArrayList<>();
        for (TypeIdentifier typeIdentifier : typeIdentifiers.list()) {
            list.add(new CollectionAngle(typeIdentifier, fieldDeclarations, methods, typeDependencies));
        }
    }

    public List<CollectionAngle> list() {
        return list;
    }
}
