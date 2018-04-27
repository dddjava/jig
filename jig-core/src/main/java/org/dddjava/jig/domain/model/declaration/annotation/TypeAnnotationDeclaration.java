package org.dddjava.jig.domain.model.declaration.annotation;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;

public class TypeAnnotationDeclaration {

    final TypeIdentifier declaringType;
    final TypeIdentifier annotationType;

    public TypeAnnotationDeclaration(TypeIdentifier declaringType, TypeIdentifier annotationType) {
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
