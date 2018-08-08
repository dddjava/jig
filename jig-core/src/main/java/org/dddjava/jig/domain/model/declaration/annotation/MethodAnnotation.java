package org.dddjava.jig.domain.model.declaration.annotation;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

/**
 * メソッドにつけられたアノテーション
 */
public class MethodAnnotation {

    final TypeIdentifier annotationType;
    final AnnotationDescription description;

    final MethodDeclaration methodDeclaration;

    public MethodAnnotation(MethodDeclaration methodDeclaration, TypeIdentifier annotationType, AnnotationDescription description) {
        this.methodDeclaration = methodDeclaration;
        this.annotationType = annotationType;
        this.description = description;
    }

    public TypeIdentifier annotationType() {
        return annotationType;
    }

    public MethodDeclaration methodDeclaration() {
        return methodDeclaration;
    }

    public AnnotationDescription description() {
        return description;
    }
}
