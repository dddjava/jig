package jig.domain.model.relation;

import jig.domain.model.identifier.MethodIdentifier;

public class MethodRelation {

    MethodIdentifier from;
    MethodIdentifier to;

    public MethodRelation(MethodIdentifier from, MethodIdentifier to) {
        this.from = from;
        this.to = to;
    }

    public boolean interfaceMethodIs(MethodIdentifier methodIdentifier) {
        return to.equals(methodIdentifier);
    }

    public MethodIdentifier concreteMethod() {
        return from;
    }

    public MethodIdentifier to() {
        return to;
    }

    public boolean fromMethodIs(MethodIdentifier methodIdentifier) {
        return from.equals(methodIdentifier);
    }
}
