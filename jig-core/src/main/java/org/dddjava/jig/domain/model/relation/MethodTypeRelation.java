package org.dddjava.jig.domain.model.relation;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;

public class MethodTypeRelation {

    MethodDeclaration from;
    TypeIdentifier to;

    public MethodTypeRelation(MethodDeclaration from, TypeIdentifier to) {
        this.from = from;
        this.to = to;
    }

    public boolean methodIs(MethodDeclaration methodDeclaration) {
        return from.equals(methodDeclaration);
    }

    public MethodDeclaration method() {
        return from;
    }

    public TypeIdentifier type() {
        return to;
    }

    public boolean typeIs(TypeIdentifier typeIdentifier) {
        return to.equals(typeIdentifier);
    }
}
