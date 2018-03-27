package jig.domain.model.relation;

import jig.domain.model.identifier.TypeIdentifier;

public class TypeRelation {

    TypeIdentifier from;
    TypeIdentifier to;

    public TypeRelation(TypeIdentifier from, TypeIdentifier to) {
        this.from = from;
        this.to = to;
    }

    public boolean isTo(TypeIdentifier typeIdentifier) {
        return to.equals(typeIdentifier);
    }

    public TypeIdentifier from() {
        return from;
    }
}
