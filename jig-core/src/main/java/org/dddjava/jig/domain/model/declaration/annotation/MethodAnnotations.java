package org.dddjava.jig.domain.model.declaration.annotation;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * メソッドにつけられたアノテーション一覧
 */
public class MethodAnnotations {

    List<MethodAnnotation> list;

    public MethodAnnotations(List<MethodAnnotation> list) {
        this.list = list;
    }

    public List<MethodAnnotation> list() {
        return list;
    }

    public Annotations annotations() {
        return new Annotations(list.stream().map(e -> e.annotation).collect(toList()));
    }

    public Optional<MethodAnnotation> findOne(MethodDeclaration methodDeclaration) {
        return list.stream()
                .filter(e -> e.methodDeclaration.sameIdentifier(methodDeclaration))
                .findFirst();
    }
}
