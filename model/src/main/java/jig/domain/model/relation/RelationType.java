package jig.domain.model.relation;

import jig.domain.model.thing.Name;
import jig.domain.model.thing.Thing;

public enum RelationType {
    DEPENDENCY,
    FIELD;

    public Relation create(Name from, Name to) {
        return new Relation(from, to, this);
    }
}
