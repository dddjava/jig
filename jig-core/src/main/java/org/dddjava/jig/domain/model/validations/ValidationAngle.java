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

    @ReportItemFor(item = ReportItem.クラス名, order = 1)
    @ReportItemFor(item = ReportItem.クラス和名, order = 2)
    public TypeIdentifier typeIdentifier() {
        return validationAnnotatedMember.declaringType();
    }

    @ReportItemFor(item = ReportItem.汎用文字列, order = 3, label = "フィールドorメソッド")
    public String memberName() {
        return validationAnnotatedMember.asSimpleNameText();
    }

    @ReportItemFor(item = ReportItem.汎用文字列, order = 4, label = "アノテーション名")
    public String annotationName() {
        return validationAnnotatedMember.annotationType().asSimpleText();
    }

    @ReportItemFor(item = ReportItem.汎用文字列, order = 5, label = "アノテーション記述")
    public String annotationDescription() {
        return validationAnnotatedMember.annotationDescription().asText();
    }
}
