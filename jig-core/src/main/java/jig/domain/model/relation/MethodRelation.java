package jig.domain.model.relation;

import jig.domain.model.declaration.method.MethodDeclaration;

public class MethodRelation {

    MethodDeclaration from;
    MethodDeclaration to;

    public MethodRelation(MethodDeclaration from, MethodDeclaration to) {
        this.from = from;
        this.to = to;
    }

    public boolean interfaceMethodIs(MethodDeclaration methodDeclaration) {
        return to.equals(methodDeclaration);
    }

    public MethodDeclaration concreteMethod() {
        return from;
    }
}
