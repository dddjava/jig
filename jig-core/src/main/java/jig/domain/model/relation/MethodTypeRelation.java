package jig.domain.model.relation;

import jig.domain.model.identifier.Identifier;
import jig.domain.model.identifier.MethodIdentifier;

public class MethodTypeRelation {

    MethodIdentifier from;
    Identifier to;

    public MethodTypeRelation(MethodIdentifier from, Identifier to) {
        this.from = from;
        this.to = to;
    }

    public boolean methodIs(MethodIdentifier methodIdentifier) {
        return from.equals(methodIdentifier);
    }

    public MethodIdentifier method() {
        return from;
    }

    public Identifier type() {
        return to;
    }

    public boolean typeIs(Identifier identifier) {
        return to.equals(identifier);
    }
}
