package org.dddjava.jig.domain.model.implementation.analyzed.declaration.type;

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

    public ParameterizedType(TypeIdentifier typeIdentifier, TypeParameter typeParameter) {
        this(typeIdentifier, new TypeParameters(Collections.singletonList(typeParameter)));
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public String asSimpleText() {
        return typeIdentifier.asSimpleText() + typeParameters.asSimpleText();
    }
}
