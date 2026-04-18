package org.dddjava.jig.domain.model.information.relation.types;

import org.dddjava.jig.domain.model.data.types.TypeId;

/**
 * 型の関係
 */
public record TypeRelationship(TypeId from, TypeId to, TypeRelationKind typeRelationKind) {

    static TypeRelationship of型引数(TypeId from, TypeId to) {
        return of(from, to, TypeRelationKind.型引数);
    }

    static TypeRelationship of使用アノテーション(TypeId from, TypeId to) {
        return of(from, to, TypeRelationKind.使用アノテーション);
    }

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
