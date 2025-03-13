package org.dddjava.jig.domain.model.information.relation.types;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.relation.graph.Edge;

import java.util.Optional;

/**
 * 型の関連
 */
public record TypeRelationship(Edge<TypeIdentifier> edge, TypeRelationKind typeRelationKind) {

    static Optional<TypeRelationship> of不明(TypeIdentifier from, TypeIdentifier to) {
        // TODO ここでnormalizeしなくてよくなってるかもしれない
        TypeIdentifier normalizeFrom = from.normalize();
        TypeIdentifier normalizeTo = to.normalize();
        // 自己参照を除く
        if (normalizeFrom.equals(normalizeTo)) return Optional.empty();
        return Optional.of(of(normalizeFrom, normalizeTo, TypeRelationKind.不明));
    }

    static TypeRelationship of型引数(TypeIdentifier from, TypeIdentifier to) {
        return of(from, to, TypeRelationKind.型引数);
    }

    static TypeRelationship of使用アノテーション(TypeIdentifier from, TypeIdentifier to) {
        return of(from, to, TypeRelationKind.使用アノテーション);
    }

    static TypeRelationship of(TypeIdentifier from, TypeIdentifier to, TypeRelationKind typeRelationKind) {
        return new TypeRelationship(new Edge<>(from, to), typeRelationKind);
    }

    public TypeIdentifier from() {
        return edge.from();
    }

    public TypeIdentifier to() {
        return edge.to();
    }

    public boolean toIs(TypeIdentifier typeIdentifier) {
        return to().equals(typeIdentifier);
    }

    public boolean sameRelation(TypeRelationship other) {
        return from().equals(other.from()) && to().equals(other.to());
    }

    public String dotText() {
        return String.format("\"%s\" -> \"%s\";", from().fullQualifiedName(), to().fullQualifiedName());
    }

    @Override
    public String toString() {
        return from().fullQualifiedName() + " -> " + to().fullQualifiedName();
    }

    public String formatText() {
        return toString();
    }
}
