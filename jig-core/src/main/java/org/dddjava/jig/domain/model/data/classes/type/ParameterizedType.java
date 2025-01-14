package org.dddjava.jig.domain.model.data.classes.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * パラメータ化された型
 *
 * 総称型 {@code Hoge<T>} に対する {@code Hoge<Fuga>} 。
 */
public record ParameterizedType(TypeIdentifier typeIdentifier, TypeArgumentList actualTypeArgumentList) {

    public ParameterizedType(TypeIdentifier typeIdentifier) {
        // 非総称型
        this(typeIdentifier, new TypeArgumentList(Collections.emptyList()));
    }

    public ParameterizedType(TypeIdentifier typeIdentifier, TypeIdentifier typeParameter) {
        this(typeIdentifier, new TypeArgumentList(Collections.singletonList(typeParameter)));
    }

    public ParameterizedType(TypeIdentifier typeIdentifier, List<TypeIdentifier> actualTypeParameters) {
        this(typeIdentifier, new TypeArgumentList(actualTypeParameters));
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public String asSimpleText() {
        if (actualTypeArgumentList.empty()) {
            return typeIdentifier.asSimpleText();
        }
        return typeIdentifier.asSimpleText() + actualTypeArgumentList.asSimpleText();
    }

    public TypeArgumentList typeParameters() {
        return actualTypeArgumentList;
    }

    List<TypeIdentifier> listTypeIdentifiers() {
        List<TypeIdentifier> list = new ArrayList<>();
        list.add(typeIdentifier);
        list.addAll(typeParameters().list());
        return list;
    }
}
