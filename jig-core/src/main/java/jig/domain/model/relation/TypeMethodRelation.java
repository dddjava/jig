package jig.domain.model.relation;

import jig.domain.model.definition.method.MethodDefinition;
import jig.domain.model.identifier.type.TypeIdentifier;

public class TypeMethodRelation {

    TypeIdentifier from;
    MethodDefinition to;

    public TypeMethodRelation(TypeIdentifier from, MethodDefinition to) {
        this.from = from;
        this.to = to;
    }

    public boolean typeIs(TypeIdentifier typeIdentifier) {
        return from.equals(typeIdentifier);
    }

    public MethodDefinition method() {
        return to;
    }
}
