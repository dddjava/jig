package jig.domain.model.declaration.annotation;

import jig.domain.model.declaration.field.FieldDeclaration;
import jig.domain.model.identifier.type.TypeIdentifier;

public class FieldAnnotationDeclaration {

    final FieldDeclaration fieldDeclaration;
    final TypeIdentifier annotationType;

    public FieldAnnotationDeclaration(FieldDeclaration fieldDeclaration, TypeIdentifier annotationType) {
        this.fieldDeclaration = fieldDeclaration;
        this.annotationType = annotationType;
    }

    public FieldDeclaration fieldDeclaration() {
        return fieldDeclaration;
    }

    public TypeIdentifier annotationType() {
        return annotationType;
    }
}
