package org.dddjava.jig.domain.model.declaration.annotation;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

/**
 * バリデーションアノテーションのついたメンバ
 */
public class ValidationAnnotatedMember {
    final TypeIdentifier annotationType;
    final String simpleNameText;
    final TypeIdentifier declaringType;
    final TypeIdentifier memberType;
    final AnnotationDescription description;

    public ValidationAnnotatedMember(AnnotatedField annotatedField) {
        annotationType = annotatedField.annotationType();
        simpleNameText = annotatedField.fieldDeclaration().nameText();
        memberType = annotatedField.fieldDeclaration().typeIdentifier();
        declaringType = annotatedField.fieldDeclaration().declaringType();
        description = annotatedField.description();
    }

    public ValidationAnnotatedMember(AnnotatedMethod annotatedMethod) {
        annotationType = annotatedMethod.annotationType();
        simpleNameText = annotatedMethod.methodDeclaration().asSignatureSimpleText();
        memberType = annotatedMethod.methodDeclaration().returnType();
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

    public TypeIdentifier type() {
        return memberType;
    }
}
