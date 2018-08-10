package org.dddjava.jig.domain.model.declaration.annotation;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

/**
 * 型につけられたアノテーション
 */
public class TypeAnnotation {

    final Annotation annotation;
    final TypeIdentifier declaringType;

    public TypeAnnotation(Annotation annotation, TypeIdentifier declaringType) {
        this.declaringType = declaringType;
        this.annotation = annotation;
    }

    public boolean typeIs(TypeIdentifier annotationType) {
        return annotation.annotationType.equals(annotationType);
    }

    public boolean declaringAt(TypeIdentifier typeIdentifier) {
        return declaringType.equals(typeIdentifier);
    }

    public TypeIdentifier type() {
        return annotation.annotationType;
    }

    public TypeIdentifier declaringType() {
        return declaringType;
    }

    public Annotation annotation() {
        return annotation;
    }
}
