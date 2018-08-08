package org.dddjava.jig.domain.model.declaration.annotation;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 型につけられたアノテーション一覧
 */
public class TypeAnnotations {

    List<TypeAnnotation> list;

    public TypeAnnotations(List<TypeAnnotation> list) {
        this.list = list;
    }

    public List<TypeAnnotation> list() {
        return list;
    }

    public TypeAnnotations filter(TypeIdentifier declaringType) {
        List<TypeAnnotation> list = this.list.stream()
                .filter(e -> e.declaringAt(declaringType))
                .collect(Collectors.toList());
        return new TypeAnnotations(list);
    }
}
