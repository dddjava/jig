package jig.domain.model.declaration.annotation;

import jig.domain.model.identifier.type.TypeIdentifier;

public class ValidationAnnotationDeclaration {
    final TypeIdentifier annotationType;
    final String annotateSimpleName;
    final TypeIdentifier declaringType;

    public ValidationAnnotationDeclaration(FieldAnnotationDeclaration fieldAnnotationDeclaration) {
        annotationType = fieldAnnotationDeclaration.annotationType();
        annotateSimpleName = fieldAnnotationDeclaration.fieldDeclaration().nameText();
        declaringType = fieldAnnotationDeclaration.fieldDeclaration().declaringType();
    }

    public ValidationAnnotationDeclaration(MethodAnnotationDeclaration methodAnnotationDeclaration) {
        annotationType = methodAnnotationDeclaration.annotationType();
        annotateSimpleName = methodAnnotationDeclaration.methodDeclaration().asSimpleText();
        declaringType = methodAnnotationDeclaration.methodDeclaration().declaringType();
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
}
