package org.dddjava.jig.domain.model.validations;

import org.dddjava.jig.domain.basic.ReportItem;
import org.dddjava.jig.domain.basic.ReportItemFor;
import org.dddjava.jig.domain.model.declaration.annotation.ValidationAnnotatedMember;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

public class ValidationAngle {

    ValidationAnnotatedMember validationAnnotatedMember;

    public ValidationAngle(ValidationAnnotatedMember validationAnnotatedMember) {
        this.validationAnnotatedMember = validationAnnotatedMember;
    }

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス和名)
    public TypeIdentifier typeIdentifier() {
        return validationAnnotatedMember.declaringType();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, order = 11, label = "フィールドorメソッド")
    public String memberName() {
        return validationAnnotatedMember.asSimpleNameText();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, order = 12, label = "アノテーション名")
    public String annotationName() {
        return validationAnnotatedMember.annotationType().asSimpleText();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, order = 13, label = "アノテーション記述")
    public String annotationDescription() {
        return validationAnnotatedMember.annotationDescription().asText();
    }
}
