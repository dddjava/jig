package org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * パラメータ化された型
 *
 * 総称型 {@code Hoge<T>} に対する {@code Hoge<Fuga>} 。
 */
public class ParameterizedType {

    TypeIdentifier typeIdentifier;
    TypeParameters actualTypeParameters;

    public ParameterizedType(TypeIdentifier typeIdentifier, TypeParameters actualTypeParameters) {
        this.typeIdentifier = typeIdentifier;
        this.actualTypeParameters = actualTypeParameters;
    }

    public ParameterizedType(TypeIdentifier typeIdentifier) {
        // 非総称型
        this(typeIdentifier, new TypeParameters(Collections.emptyList()));
    }

    public ParameterizedType(TypeIdentifier typeIdentifier, TypeParameter typeParameter) {
        this(typeIdentifier, new TypeParameters(Collections.singletonList(typeParameter)));
    }

    public ParameterizedType(TypeIdentifier typeIdentifier, List<TypeIdentifier> actualTypeParameters) {
        this(typeIdentifier, new TypeParameters(actualTypeParameters.stream().map(TypeParameter::new).collect(Collectors.toList())));
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public String asSimpleText() {
        return typeIdentifier.asSimpleText() + actualTypeParameters.asSimpleText();
    }

    public TypeParameters typeParameters() {
        return actualTypeParameters;
    }
}
