package jig.domain.model.annotation;

import jig.domain.model.identifier.type.TypeIdentifier;

public class AnnotationDefinition {

    final TypeIdentifier declaringType;
    final TypeIdentifier annotationType;

    public AnnotationDefinition(TypeIdentifier declaringType, TypeIdentifier annotationType) {
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
