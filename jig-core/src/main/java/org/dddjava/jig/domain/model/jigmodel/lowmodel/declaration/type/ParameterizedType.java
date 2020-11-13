package org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public ParameterizedType(TypeIdentifier typeIdentifier, TypeIdentifier typeParameter) {
        this(typeIdentifier, new TypeParameters(Collections.singletonList(typeParameter)));
    }

    public ParameterizedType(TypeIdentifier typeIdentifier, List<TypeIdentifier> actualTypeParameters) {
        this(typeIdentifier, new TypeParameters(actualTypeParameters));
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public String asSimpleText() {
        if (actualTypeParameters.empty()) {
            return typeIdentifier.asSimpleText();
        }
        return typeIdentifier.asSimpleText() + actualTypeParameters.asSimpleText();
    }

    public TypeParameters typeParameters() {
        return actualTypeParameters;
    }

    List<TypeIdentifier> listTypeIdentifiers() {
        List<TypeIdentifier> list = new ArrayList<>();
        list.add(typeIdentifier);
        list.addAll(typeParameters().list());
        return list;
    }
}
