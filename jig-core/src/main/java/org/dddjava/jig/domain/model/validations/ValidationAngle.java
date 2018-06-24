package org.dddjava.jig.domain.model.validations;

import org.dddjava.jig.domain.model.declaration.annotation.ValidationAnnotatedMember;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.report.ReportItem;
import org.dddjava.jig.domain.model.report.ReportItemFor;

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

    @ReportItemFor(value = ReportItem.汎用文字列, order = 1, label = "メンバ名")
    public String memberName() {
        return validationAnnotatedMember.asSimpleNameText();
    }

    @ReportItemFor(value = ReportItem.単純クラス名, label = "メンバクラス名", order = 2)
    public TypeIdentifier memberType() {
        return validationAnnotatedMember.type();
    }

    @ReportItemFor(value = ReportItem.単純クラス名, order = 3, label = "アノテーションクラス名")
    public TypeIdentifier annotationType() {
        return validationAnnotatedMember.annotationType();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, order = 4, label = "アノテーション記述")
    public String annotationDescription() {
        return validationAnnotatedMember.annotationDescription().asText();
    }
}
