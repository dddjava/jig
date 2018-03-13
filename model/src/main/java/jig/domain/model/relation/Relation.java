package jig.domain.model.relation;

import jig.domain.model.thing.Name;

public class Relation {
    Name from;
    Name to;
    RelationType relationType;

    public Relation(Name from, Name to, RelationType relationType) {
        this.from = from;
        this.to = to;
        this.relationType = relationType;
    }

    public Name from() {
        return from;
    }

    public Name to() {
        return to;
    }

    public RelationType relationType() {
        return relationType;
    }
}
