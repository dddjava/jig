package jig.domain.model.relation;

import jig.domain.model.identifier.Identifier;

import java.util.Objects;

public class Relation {
    Identifier from;
    Identifier to;
    RelationType relationType;

    public Relation(Identifier from, Identifier to, RelationType relationType) {
        this.from = from;
        this.to = to;
        this.relationType = relationType;
    }

    public Identifier from() {
        return from;
    }

    public Identifier to() {
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
}
