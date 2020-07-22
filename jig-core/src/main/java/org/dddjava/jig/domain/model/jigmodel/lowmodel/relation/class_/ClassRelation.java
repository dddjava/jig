package org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

import java.util.Objects;

/**
 * 型の依存関係
 */
public class ClassRelation {

    final TypeIdentifier from;
    final TypeIdentifier to;

    public ClassRelation(TypeIdentifier from, TypeIdentifier to) {
        this.from = from.normalize();
        this.to = to.normalize();
    }

    public boolean toIs(TypeIdentifier typeIdentifier) {
        return to.equals(typeIdentifier);
    }

    public TypeIdentifier from() {
        return from;
    }

    public boolean selfRelation() {
        return from.normalize().equals(to.normalize());
    }

    public TypeIdentifier to() {
        return to;
    }

    public String dotText() {
        return String.format("\"%s\" -> \"%s\";", from.fullQualifiedName(), to.fullQualifiedName());
    }

    @Override
    public String toString() {
        return from.fullQualifiedName() + " -> " + to.fullQualifiedName();
    }

    public boolean sameRelation(ClassRelation other) {
        return from.equals(other.from) && to.equals(other.to);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassRelation that = (ClassRelation) o;
        return Objects.equals(from, that.from) &&
                Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
