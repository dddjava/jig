package jig.domain.model.relation;

import jig.domain.model.thing.Thing;

public enum RelationType {
    DEPENDENCY,
    FIELD;

    public Relation create(Thing from, Thing to) {
        return new Relation(from, to, this);
    }
}
