package org.dddjava.jig.domain.model.declaration.annotation;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;

/**
 * アノテーションのついた型
 */
public class AnnotatedType {

    final TypeIdentifier declaringType;
    final TypeIdentifier annotationType;

    public AnnotatedType(TypeIdentifier declaringType, TypeIdentifier annotationType) {
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
