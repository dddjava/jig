package jig.domain.model.relation;

import jig.domain.model.definition.field.FieldDefinition;
import jig.domain.model.identifier.type.TypeIdentifier;

public class TypeRelation {

    TypeIdentifier from;
    FieldDefinition to;

    public TypeRelation(TypeIdentifier from, FieldDefinition to) {
        this.from = from;
        this.to = to;
    }

    public TypeIdentifier from() {
        return from;
    }

    public FieldDefinition field() {
        return to;
    }
}
