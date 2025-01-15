package org.dddjava.jig.domain.model.data.classes.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * パラメータ化された型
 */
public record ParameterizedType(TypeIdentifier typeIdentifier, List<ParameterizedType> _typeParameters) {

    public ParameterizedType(TypeIdentifier typeIdentifier) {
        // 非総称型
        this(typeIdentifier, Collections.emptyList());
    }

    public ParameterizedType(TypeIdentifier typeIdentifier, TypeIdentifier typeParameter) {
        this(typeIdentifier, List.of(new ParameterizedType(typeParameter)));
    }

    public static ParameterizedType convert(TypeIdentifier typeIdentifier, List<TypeIdentifier> list) {
        return new ParameterizedType(typeIdentifier, list.stream().map(ParameterizedType::new).toList());
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public String asSimpleText() {
        if (_typeParameters.isEmpty()) {
            return typeIdentifier.asSimpleText();
        }
        return typeIdentifier.asSimpleText() + _typeParameters.stream()
                .map(ParameterizedType::asSimpleText)
                .collect(Collectors.joining(", ", "<", ">"));
    }

    public TypeArgumentList typeParameters() {
        return new TypeArgumentList(_typeParameters.stream().map(ParameterizedType::typeIdentifier).toList());
    }

    List<TypeIdentifier> listTypeIdentifiers() {
        List<TypeIdentifier> list = new ArrayList<>();
        list.add(typeIdentifier);
        list.addAll(typeParameters().list());
        return list;
    }
}
