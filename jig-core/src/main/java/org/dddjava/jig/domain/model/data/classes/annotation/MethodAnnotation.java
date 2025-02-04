package org.dddjava.jig.domain.model.data.classes.annotation;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

/**
 * メソッドにつけられたアノテーション
 */
public record MethodAnnotation(Annotation annotation, MethodDeclaration methodDeclaration) {

    public TypeIdentifier annotationType() {
        return annotation.typeIdentifier;
    }

    public MethodDeclaration methodDeclaration() {
        return methodDeclaration;
    }

    public AnnotationDescription description() {
        return annotation.description;
    }
}
