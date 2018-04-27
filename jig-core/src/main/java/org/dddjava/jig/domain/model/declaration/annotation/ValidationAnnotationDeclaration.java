package org.dddjava.jig.domain.model.declaration.annotation;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;

public class ValidationAnnotationDeclaration {
    final TypeIdentifier annotationType;
    final String annotateSimpleName;
    final TypeIdentifier declaringType;
    final AnnotationDescription description;

    public ValidationAnnotationDeclaration(FieldAnnotationDeclaration fieldAnnotationDeclaration) {
        annotationType = fieldAnnotationDeclaration.annotationType();
        annotateSimpleName = fieldAnnotationDeclaration.fieldDeclaration().nameText();
        declaringType = fieldAnnotationDeclaration.fieldDeclaration().declaringType();
        description = fieldAnnotationDeclaration.description();
    }

    public ValidationAnnotationDeclaration(MethodAnnotationDeclaration methodAnnotationDeclaration) {
        annotationType = methodAnnotationDeclaration.annotationType();
        annotateSimpleName = methodAnnotationDeclaration.methodDeclaration().asSimpleText();
        declaringType = methodAnnotationDeclaration.methodDeclaration().declaringType();
        description = methodAnnotationDeclaration.description();
    }

    public String annotateSimpleName() {
        return annotateSimpleName;
    }

    public TypeIdentifier declaringType() {
        return declaringType;
    }

    public TypeIdentifier annotationType() {
        return annotationType;
    }

    public AnnotationDescription annotationDescription() {
        return description;
    }
}
