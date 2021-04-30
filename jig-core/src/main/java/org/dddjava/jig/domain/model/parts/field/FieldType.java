package org.dddjava.jig.domain.model.parts.field;

import org.dddjava.jig.domain.model.parts.type.ParameterizedType;
import org.dddjava.jig.domain.model.parts.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.parts.type.TypeParameters;

public class FieldType {

    ParameterizedType parameterizedType;

    private FieldType(ParameterizedType parameterizedType) {
        this.parameterizedType = parameterizedType;
    }

    public FieldType(TypeIdentifier typeIdentifier) {
        this(new ParameterizedType(typeIdentifier));
    }

    public FieldType(TypeIdentifier typeIdentifier, TypeIdentifiers typeParameters) {
        this(new ParameterizedType(typeIdentifier, new TypeParameters(typeParameters.list())));
    }

    public TypeParameters typeParameterTypeIdentifiers() {
        return parameterizedType.typeParameters();
    }

    public String asSimpleText() {
        return parameterizedType.asSimpleText();
    }

    public TypeIdentifier nonGenericTypeIdentifier() {
        return parameterizedType.typeIdentifier();
    }
}
