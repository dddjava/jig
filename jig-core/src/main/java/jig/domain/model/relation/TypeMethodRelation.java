package jig.domain.model.relation;

import jig.domain.model.identifier.Identifier;
import jig.domain.model.identifier.MethodIdentifier;

public class TypeMethodRelation {

    Identifier from;
    MethodIdentifier to;

    public TypeMethodRelation(Identifier from, MethodIdentifier to) {
        this.from = from;
        this.to = to;
    }

    public boolean typeIs(Identifier identifier) {
        return from.equals(identifier);
    }

    public MethodIdentifier method() {
        return to;
    }
}
