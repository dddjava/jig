package org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

/**
 * フィールドにつけられたアノテーション
 */
public class FieldAnnotation {

    final Annotation annotation;
    final FieldDeclaration fieldDeclaration;

    public FieldAnnotation(Annotation annotation, FieldDeclaration fieldDeclaration) {
        this.annotation = annotation;
        this.fieldDeclaration = fieldDeclaration;
    }

    public FieldDeclaration fieldDeclaration() {
        return fieldDeclaration;
    }

    public TypeIdentifier annotationType() {
        return annotation.typeIdentifier;
    }

    public AnnotationDescription description() {
        return annotation.description;
    }
}
