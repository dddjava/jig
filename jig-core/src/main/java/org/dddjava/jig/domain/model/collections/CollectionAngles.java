package org.dddjava.jig.domain.model.collections;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.networks.type.TypeDependencies;

import java.util.ArrayList;
import java.util.List;

/**
 * コレクションの切り口一覧
 */
public class CollectionAngles {

    List<CollectionAngle> list;

    public CollectionAngles(TypeIdentifiers typeIdentifiers, FieldDeclarations fieldDeclarations, MethodDeclarations methodDeclarations, TypeDependencies typeDependencies) {
        this.list = new ArrayList<>();
        for (TypeIdentifier typeIdentifier : typeIdentifiers.list()) {
            list.add(new CollectionAngle(typeIdentifier, fieldDeclarations, methodDeclarations, typeDependencies));
        }
    }

    public List<CollectionAngle> list() {
        return list;
    }
}
