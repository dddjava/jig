package jig.model.relation;

import jig.model.thing.Thing;

public class Relation {
    Thing from;
    Thing to;

    RelationType type = RelationType.DEPENDENCY;

    public Relation(Thing from, Thing to) {
        this.from = from;
        this.to = to;
    }

    public Thing from() {
        return from;
    }

    public Thing to() {
        return to;
    }
}
