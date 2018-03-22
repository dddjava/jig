package jig.domain.model.relation;

import jig.domain.model.identifier.Identifier;

public class TypeRelation {

    Identifier from;
    Identifier to;

    public TypeRelation(Identifier from, Identifier to) {
        this.from = from;
        this.to = to;
    }

    public boolean isTo(Identifier identifier) {
        return to.equals(identifier);
    }

    public Identifier from() {
        return from;
    }
}
