package jig.domain.model.identifier.field;

import jig.domain.model.identifier.type.TypeIdentifier;

public class FieldIdentifier {

    String name;
    TypeIdentifier typeIdentifier;

    public FieldIdentifier(String name, TypeIdentifier typeIdentifier) {
        this.name = name;
        this.typeIdentifier = typeIdentifier;
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public String nameText() {
        return name;
    }

    public String typeAndNameText() {
        return String.format("%s %s", typeIdentifier.asSimpleText() ,name);
    }
}
