package org.dddjava.jig.domain.model.declaration.annotation;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

/**
 * フィールドにつけられたアノテーション
 */
public class FieldAnnotation {

    final TypeIdentifier annotationType;
    final AnnotationDescription description;

    final FieldDeclaration fieldDeclaration;

    public FieldAnnotation(FieldDeclaration fieldDeclaration, TypeIdentifier annotationType, AnnotationDescription description) {
        this.fieldDeclaration = fieldDeclaration;
        this.annotationType = annotationType;
        this.description = description;
    }

    public FieldDeclaration fieldDeclaration() {
        return fieldDeclaration;
    }

    public TypeIdentifier annotationType() {
        return annotationType;
    }

    public AnnotationDescription description() {
        return description;
    }
}
