package org.dddjava.jig.domain.model.information.relation.types;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.relation.graph.Edge;

import java.util.Optional;

/**
 * 型の関連
 */
public record TypeRelationship(Edge<TypeId> edge, TypeRelationKind typeRelationKind) {

    static Optional<TypeRelationship> of不明(TypeId from, TypeId to) {
        // TODO ここでnormalizeしなくてよくなってるかもしれない
        TypeId normalizeFrom = from.normalize();
        TypeId normalizeTo = to.normalize();
        // 自己参照を除く
        if (normalizeFrom.equals(normalizeTo)) return Optional.empty();
        return Optional.of(of(normalizeFrom, normalizeTo, TypeRelationKind.不明));
    }

    static TypeRelationship of型引数(TypeId from, TypeId to) {
        return of(from, to, TypeRelationKind.型引数);
    }

    static TypeRelationship of使用アノテーション(TypeId from, TypeId to) {
        return of(from, to, TypeRelationKind.使用アノテーション);
    }

    static TypeRelationship of(TypeId from, TypeId to, TypeRelationKind typeRelationKind) {
        return new TypeRelationship(Edge.of(from, to), typeRelationKind);
    }

    public TypeId from() {
        return edge.from();
    }

    public TypeId to() {
        return edge.to();
    }

    public boolean toIs(TypeId typeId) {
        return to().equals(typeId);
    }

    @Override
    public String toString() {
        return from().fqn() + " -> " + to().fqn();
    }

    public String formatText() {
        return toString();
    }
}
