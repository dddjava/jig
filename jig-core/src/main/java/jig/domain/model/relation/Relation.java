package jig.domain.model.relation;

import jig.domain.model.identifier.TypeIdentifier;

import java.util.Objects;

public class Relation {
    TypeIdentifier from;
    TypeIdentifier to;
    RelationType relationType;

    public Relation(TypeIdentifier from, TypeIdentifier to, RelationType relationType) {
        this.from = from;
        this.to = to;
        this.relationType = relationType;
    }

    public TypeIdentifier from() {
        return from;
    }

    public TypeIdentifier to() {
        return to;
    }

    public RelationType relationType() {
        return relationType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relation relation = (Relation) o;
        return Objects.equals(from, relation.from) &&
                Objects.equals(to, relation.to) &&
                relationType == relation.relationType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, relationType);
    }

    public Relation applyDepth(Depth depth) {
        return new Relation(from.applyDepth(depth), to.applyDepth(depth), relationType);
    }

    public boolean notSelfRelation() {
        return !from.equals(to);
    }
}
