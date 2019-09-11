package org.dddjava.jig.domain.model.declaration.method;

import org.dddjava.jig.domain.model.declaration.type.ParameterizedType;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

/**
 * メソッド戻り値
 */
public class MethodReturn {

    ParameterizedType parameterizedType;

    public MethodReturn(TypeIdentifier typeIdentifier) {
        this(new ParameterizedType(typeIdentifier));
    }

    public MethodReturn(ParameterizedType parameterizedType) {
        this.parameterizedType = parameterizedType;
    }

    public ParameterizedType parameterizedType() {
        return parameterizedType;
    }

    public TypeIdentifier typeIdentifier() {
        return parameterizedType.typeIdentifier();
    }

    public boolean isPrimitive() {
        return typeIdentifier().isPrimitive();
    }

    public boolean isBoolean() {
        return typeIdentifier().isBoolean();
    }

    public boolean isStream() {
        return typeIdentifier().isStream();
    }

    public boolean isVoid() {
        return typeIdentifier().isVoid();
    }
}
