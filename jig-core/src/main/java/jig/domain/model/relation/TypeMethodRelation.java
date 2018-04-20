package jig.domain.model.relation;

import jig.domain.model.declaration.method.MethodDeclaration;
import jig.domain.model.identifier.type.TypeIdentifier;

public class TypeMethodRelation {

    TypeIdentifier from;
    MethodDeclaration to;

    public TypeMethodRelation(TypeIdentifier from, MethodDeclaration to) {
        this.from = from;
        this.to = to;
    }

    public boolean typeIs(TypeIdentifier typeIdentifier) {
        return from.equals(typeIdentifier);
    }

    public MethodDeclaration method() {
        return to;
    }
}
