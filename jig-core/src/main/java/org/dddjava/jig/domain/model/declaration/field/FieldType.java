package org.dddjava.jig.domain.model.declaration.field;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

import java.util.Collections;

public class FieldType {

    TypeIdentifier typeIdentifier;
    TypeIdentifiers typeParameters;

    public FieldType(TypeIdentifier typeIdentifier) {
        this(typeIdentifier, null);
    }

    public FieldType(TypeIdentifier typeIdentifier, TypeIdentifiers typeParameters) {
        this.typeIdentifier = typeIdentifier;
        this.typeParameters = typeParameters;
    }

    public TypeIdentifiers typeParameterTypeIdentifiers() {
        if (typeParameters == null) {
            return new TypeIdentifiers(Collections.emptyList());
        }
        return typeParameters;
    }
}
