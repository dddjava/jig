package jig.domain.model.relation;

import jig.domain.model.definition.method.MethodDefinition;
import jig.domain.model.identifier.type.TypeIdentifier;

public class MethodTypeRelation {

    MethodDefinition from;
    TypeIdentifier to;

    public MethodTypeRelation(MethodDefinition from, TypeIdentifier to) {
        this.from = from;
        this.to = to;
    }

    public boolean methodIs(MethodDefinition methodDefinition) {
        return from.equals(methodDefinition);
    }

    public MethodDefinition method() {
        return from;
    }

    public TypeIdentifier type() {
        return to;
    }

    public boolean typeIs(TypeIdentifier typeIdentifier) {
        return to.equals(typeIdentifier);
    }
}
