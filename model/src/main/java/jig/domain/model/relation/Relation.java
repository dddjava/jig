package jig.domain.model.relation;

import jig.domain.model.thing.Thing;

public class Relation {
    Thing from;
    Thing to;
    RelationType relationType;

    public Relation(Thing from, Thing to, RelationType relationType) {
        this.from = from;
        this.to = to;
        this.relationType = relationType;
    }

    public Thing from() {
        return from;
    }

    public Thing to() {
        return to;
    }
}
