package jig.domain.model.relation;

import jig.domain.model.definition.method.MethodDefinition;

public class MethodRelation {

    MethodDefinition from;
    MethodDefinition to;

    public MethodRelation(MethodDefinition from, MethodDefinition to) {
        this.from = from;
        this.to = to;
    }

    public boolean interfaceMethodIs(MethodDefinition methodDefinition) {
        return to.equals(methodDefinition);
    }

    public MethodDefinition concreteMethod() {
        return from;
    }

    public MethodDefinition to() {
        return to;
    }

    public boolean fromMethodIs(MethodDefinition methodDefinition) {
        return from.equals(methodDefinition);
    }
}
