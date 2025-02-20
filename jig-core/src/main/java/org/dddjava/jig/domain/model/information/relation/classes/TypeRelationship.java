package org.dddjava.jig.domain.model.information.relation.classes;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Optional;

/**
 * 型の関連
 */
public record TypeRelationship(TypeIdentifier from, TypeIdentifier to, TypeRelationKind typeRelationKind) {

    public static Optional<TypeRelationship> from(TypeIdentifier from, TypeIdentifier to) {
        // TODO ここでnormalizeしなくてよくなってるかもしれない
        TypeIdentifier normalizeFrom = from.normalize();
        TypeIdentifier normalizeTo = to.normalize();
        // 自己参照を除く
        if (normalizeFrom.equals(normalizeTo)) return Optional.empty();
        return Optional.of(new TypeRelationship(normalizeFrom, normalizeTo, TypeRelationKind.不明));
    }

    public boolean toIs(TypeIdentifier typeIdentifier) {
        return to.equals(typeIdentifier);
    }

    public boolean sameRelation(TypeRelationship other) {
        return from.equals(other.from) && to.equals(other.to);
    }

    public String dotText() {
        return String.format("\"%s\" -> \"%s\";", from.fullQualifiedName(), to.fullQualifiedName());
    }

    @Override
    public String toString() {
        return from.fullQualifiedName() + " -> " + to.fullQualifiedName();
    }

    public String formatText() {
        return toString();
    }
}
