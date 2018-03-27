package jig.domain.model.relation;

import jig.domain.model.identifier.PackageIdentifier;

import java.util.Objects;

public class Relation {
    PackageIdentifier from;
    PackageIdentifier to;

    public Relation(PackageIdentifier from, PackageIdentifier to) {
        this.from = from;
        this.to = to;
    }

    public PackageIdentifier from() {
        return from;
    }

    public PackageIdentifier to() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relation relation = (Relation) o;
        return Objects.equals(from, relation.from) &&
                Objects.equals(to, relation.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    public Relation applyDepth(Depth depth) {
        return new Relation(from.applyDepth(depth), to.applyDepth(depth));
    }

    public boolean notSelfRelation() {
        return !from.equals(to);
    }
}
