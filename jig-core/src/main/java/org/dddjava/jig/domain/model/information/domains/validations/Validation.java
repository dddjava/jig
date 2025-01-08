package org.dddjava.jig.domain.model.information.domains.validations;

import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

/**
 * バリデーション
 */
public class Validation {

    ValidationAnnotatedMember validationAnnotatedMember;

    public Validation(ValidationAnnotatedMember validationAnnotatedMember) {
        this.validationAnnotatedMember = validationAnnotatedMember;
    }

    public TypeIdentifier typeIdentifier() {
        return validationAnnotatedMember.declaringType();
    }

    public String memberName() {
        return validationAnnotatedMember.asSimpleNameText();
    }

    public TypeIdentifier memberType() {
        return validationAnnotatedMember.type();
    }

    public TypeIdentifier annotationType() {
        return validationAnnotatedMember.annotationType();
    }

    public String annotationDescription() {
        return validationAnnotatedMember.annotationDescription().asText();
    }
}
