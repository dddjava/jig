package org.dddjava.jig.domain.model.models.jigobject.member;

import org.dddjava.jig.domain.model.parts.annotation.AnnotationDescription;
import org.dddjava.jig.domain.model.parts.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.parts.annotation.MethodAnnotation;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;

/**
 * バリデーションアノテーションのついたメンバ
 */
public class ValidationAnnotatedMember {
    final TypeIdentifier annotationType;
    final String simpleNameText;
    final TypeIdentifier declaringType;
    final TypeIdentifier memberType;
    final AnnotationDescription description;

    public ValidationAnnotatedMember(FieldAnnotation fieldAnnotation) {
        annotationType = fieldAnnotation.annotationType();
        simpleNameText = fieldAnnotation.fieldDeclaration().nameText();
        memberType = fieldAnnotation.fieldDeclaration().typeIdentifier();
        declaringType = fieldAnnotation.fieldDeclaration().declaringType();
        description = fieldAnnotation.description();
    }

    public ValidationAnnotatedMember(MethodAnnotation methodAnnotation) {
        annotationType = methodAnnotation.annotationType();
        simpleNameText = methodAnnotation.methodDeclaration().asSignatureSimpleText();
        memberType = methodAnnotation.methodDeclaration().methodReturn().typeIdentifier();
        declaringType = methodAnnotation.methodDeclaration().declaringType();
        description = methodAnnotation.description();
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
