package jig.domain.model.relation;

import jig.domain.model.declaration.field.FieldDeclaration;
import jig.domain.model.identifier.type.TypeIdentifier;

public class TypeRelation {

    TypeIdentifier from;
    FieldDeclaration to;

    public TypeRelation(TypeIdentifier from, FieldDeclaration to) {
        this.from = from;
        this.to = to;
    }

    public TypeIdentifier from() {
        return from;
    }

    public FieldDeclaration field() {
        return to;
    }
}
