package org.dddjava.jig.domain.model.jigmodel.validations;

import org.dddjava.jig.domain.model.parts.annotation.ValidationAnnotatedMember;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;

/**
 * バリデーション分析の切り口
 */
public class ValidationAngle {

    ValidationAnnotatedMember validationAnnotatedMember;

    public ValidationAngle(ValidationAnnotatedMember validationAnnotatedMember) {
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
