package org.dddjava.jig.domain.model.declaration.field;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

import java.util.Collections;
import java.util.stream.Collectors;

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

    public String asSimpleText() {
        StringBuilder sb = new StringBuilder();
        sb.append(typeIdentifier.asSimpleText());
        if (typeParameters == null) {
            return sb.toString();
        }
        String typeParametersText = typeParameters.list().stream()
                .map(TypeIdentifier::asSimpleText)
                .collect(Collectors.joining(", ", "<", ">"));
        return sb.append(typeParametersText).toString();
    }
}
