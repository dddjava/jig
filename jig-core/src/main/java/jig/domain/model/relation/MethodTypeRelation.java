package jig.domain.model.relation;

import jig.domain.model.identifier.MethodIdentifier;
import jig.domain.model.identifier.TypeIdentifier;

public class MethodTypeRelation {

    MethodIdentifier from;
    TypeIdentifier to;

    public MethodTypeRelation(MethodIdentifier from, TypeIdentifier to) {
        this.from = from;
        this.to = to;
    }

    public boolean methodIs(MethodIdentifier methodIdentifier) {
        return from.equals(methodIdentifier);
    }

    public MethodIdentifier method() {
        return from;
    }

    public TypeIdentifier type() {
        return to;
    }

    public boolean typeIs(TypeIdentifier typeIdentifier) {
        return to.equals(typeIdentifier);
    }
}
