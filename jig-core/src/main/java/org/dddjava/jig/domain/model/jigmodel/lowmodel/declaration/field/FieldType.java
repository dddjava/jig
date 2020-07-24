package org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.ParameterizedType;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeParameters;

public class FieldType {

    ParameterizedType parameterizedType;

    public FieldType(ParameterizedType parameterizedType) {
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
