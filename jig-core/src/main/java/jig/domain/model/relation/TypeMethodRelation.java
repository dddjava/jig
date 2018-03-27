package jig.domain.model.relation;

import jig.domain.model.identifier.TypeIdentifier;
import jig.domain.model.identifier.MethodIdentifier;

public class TypeMethodRelation {

    TypeIdentifier from;
    MethodIdentifier to;

    public TypeMethodRelation(TypeIdentifier from, MethodIdentifier to) {
        this.from = from;
        this.to = to;
    }

    public boolean typeIs(TypeIdentifier typeIdentifier) {
        return from.equals(typeIdentifier);
    }

    public MethodIdentifier method() {
        return to;
    }
}
