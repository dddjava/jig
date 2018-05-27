package org.dddjava.jig.domain.model.declaration.annotation;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;

/**
 * バリデーションアノテーションのついたメンバ
 */
public class ValidationAnnotationDeclaration {
    final TypeIdentifier annotationType;
    final String annotateSimpleName;
    final TypeIdentifier declaringType;
    final AnnotationDescription description;

    public ValidationAnnotationDeclaration(AnnotatedField annotatedField) {
        annotationType = annotatedField.annotationType();
        annotateSimpleName = annotatedField.fieldDeclaration().nameText();
        declaringType = annotatedField.fieldDeclaration().declaringType();
        description = annotatedField.description();
    }

    public ValidationAnnotationDeclaration(AnnotatedMethod annotatedMethod) {
        annotationType = annotatedMethod.annotationType();
        annotateSimpleName = annotatedMethod.methodDeclaration().asSignatureSimpleText();
        declaringType = annotatedMethod.methodDeclaration().declaringType();
        description = annotatedMethod.description();
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
