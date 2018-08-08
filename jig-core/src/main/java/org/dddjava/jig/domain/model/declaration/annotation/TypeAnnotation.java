package org.dddjava.jig.domain.model.declaration.annotation;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

/**
 * 型につけられたアノテーション
 */
public class TypeAnnotation {

    final TypeIdentifier annotationType;

    final TypeIdentifier declaringType;

    public TypeAnnotation(TypeIdentifier declaringType, TypeIdentifier annotationType) {
        this.declaringType = declaringType;
        this.annotationType = annotationType;
    }

    public boolean typeIs(TypeIdentifier annotationType) {
        return this.annotationType.equals(annotationType);
    }

    public TypeIdentifier type() {
        return annotationType;
    }
}
