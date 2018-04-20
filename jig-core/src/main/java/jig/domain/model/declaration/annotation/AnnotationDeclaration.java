package jig.domain.model.declaration.annotation;

import jig.domain.model.identifier.type.TypeIdentifier;

public class AnnotationDeclaration {

    final TypeIdentifier declaringType;
    final TypeIdentifier annotationType;

    public AnnotationDeclaration(TypeIdentifier declaringType, TypeIdentifier annotationType) {
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
