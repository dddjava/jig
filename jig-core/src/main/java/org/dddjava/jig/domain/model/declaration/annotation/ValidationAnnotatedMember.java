package org.dddjava.jig.domain.model.declaration.annotation;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

/**
 * バリデーションアノテーションのついたメンバ
 */
public class ValidationAnnotatedMember {
    final TypeIdentifier annotationType;
    final String simpleNameText;
    final TypeIdentifier declaringType;
    final AnnotationDescription description;

    public ValidationAnnotatedMember(AnnotatedField annotatedField) {
        annotationType = annotatedField.annotationType();
        simpleNameText = annotatedField.fieldDeclaration().nameText();
        declaringType = annotatedField.fieldDeclaration().declaringType();
        description = annotatedField.description();
    }

    public ValidationAnnotatedMember(AnnotatedMethod annotatedMethod) {
        annotationType = annotatedMethod.annotationType();
        simpleNameText = annotatedMethod.methodDeclaration().asSignatureSimpleText();
        declaringType = annotatedMethod.methodDeclaration().declaringType();
        description = annotatedMethod.description();
    }

    public String asSimpleNameText() {
        return simpleNameText;
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
