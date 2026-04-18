package org.dddjava.jig.domain.model.information.relation.types;

import org.dddjava.jig.domain.model.data.types.TypeId;

/**
 * 型の関係
 */
public record TypeRelationship(TypeId from, TypeId to, TypeRelationKind typeRelationKind) {

    static TypeRelationship of(TypeId from, TypeId to, TypeRelationKind typeRelationKind) {
        return new TypeRelationship(from, to, typeRelationKind);
    }

    public boolean toIs(TypeId typeId) {
        return to().equals(typeId);
    }

    @Override
    public String toString() {
        return from().fqn() + " -> " + to().fqn();
    }

}
