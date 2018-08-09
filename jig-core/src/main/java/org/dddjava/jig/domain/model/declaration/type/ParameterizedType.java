package org.dddjava.jig.domain.model.declaration.type;

import java.util.Collections;

/**
 * パラメータ化された型
 *
 * 総称型 {@code Hoge<T>} に対する {@code Hoge<Fuga>} 。
 */
public class ParameterizedType {

    TypeIdentifier typeIdentifier;
    TypeParameters typeParameters;

    public ParameterizedType(TypeIdentifier typeIdentifier, TypeParameters typeParameters) {
        this.typeIdentifier = typeIdentifier;
        this.typeParameters = typeParameters;
    }

    public ParameterizedType(TypeIdentifier typeIdentifier) {
        // 非総称型
        this(typeIdentifier, new TypeParameters(Collections.emptyList()));
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public TypeParameters typeParameters() {
        return typeParameters;
    }

    public String asSimpleText() {
        return typeIdentifier.asSimpleText() + typeParameters.asSimpleText();
    }
}
