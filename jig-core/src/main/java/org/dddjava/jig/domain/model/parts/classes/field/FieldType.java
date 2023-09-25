package org.dddjava.jig.domain.model.parts.classes.field;

import org.dddjava.jig.domain.model.parts.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.parts.classes.type.TypeArgumentList;

public class FieldType {

    ParameterizedType parameterizedType;

    private FieldType(ParameterizedType parameterizedType) {
        this.parameterizedType = parameterizedType;
    }

    public FieldType(TypeIdentifier typeIdentifier) {
        this(new ParameterizedType(typeIdentifier));
    }

    public FieldType(TypeIdentifier typeIdentifier, TypeIdentifiers typeParameters) {
        this(new ParameterizedType(typeIdentifier, new TypeArgumentList(typeParameters.list())));
    }

    public TypeArgumentList typeParameterTypeIdentifiers() {
        return parameterizedType.typeParameters();
    }

    public String asSimpleText() {
        return parameterizedType.asSimpleText();
    }

    public TypeIdentifier nonGenericTypeIdentifier() {
        return parameterizedType.typeIdentifier();
    }

    public ParameterizedType parameterizedType() {
        return parameterizedType;
    }
}
