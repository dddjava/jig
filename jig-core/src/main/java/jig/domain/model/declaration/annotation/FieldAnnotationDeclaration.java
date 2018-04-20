package jig.domain.model.declaration.annotation;

import jig.domain.model.declaration.field.FieldDeclaration;
import jig.domain.model.identifier.type.TypeIdentifier;

public class FieldAnnotationDeclaration {

    final FieldDeclaration declaringMethod;
    final TypeIdentifier annotationType;

    public FieldAnnotationDeclaration(FieldDeclaration declaringMethod, TypeIdentifier annotationType) {
        this.declaringMethod = declaringMethod;
        this.annotationType = annotationType;
    }
}
