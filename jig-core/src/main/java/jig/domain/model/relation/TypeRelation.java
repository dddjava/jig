package jig.domain.model.relation;

import jig.domain.model.identifier.field.FieldIdentifier;
import jig.domain.model.identifier.type.TypeIdentifier;

public class TypeRelation {

    TypeIdentifier from;
    FieldIdentifier to;

    public TypeRelation(TypeIdentifier from, FieldIdentifier to) {
        this.from = from;
        this.to = to;
    }

    public boolean isTo(TypeIdentifier typeIdentifier) {
        return to.typeIdentifier().equals(typeIdentifier);
    }

    public TypeIdentifier from() {
        return from;
    }

    public FieldIdentifier field() {
        return to;
    }
}
