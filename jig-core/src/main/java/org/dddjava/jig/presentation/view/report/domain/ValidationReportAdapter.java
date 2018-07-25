package org.dddjava.jig.presentation.view.report.domain;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.validations.ValidationAngle;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("VALIDATION")
public class ValidationReportAdapter {

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス和名)
    public TypeIdentifier typeIdentifier(ValidationAngle angle) {
        return angle.typeIdentifier();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, order = 1, label = "メンバ名")
    public String memberName(ValidationAngle angle) {
        return angle.memberName();
    }

    @ReportItemFor(value = ReportItem.単純クラス名, label = "メンバクラス名", order = 2)
    public TypeIdentifier memberType(ValidationAngle angle) {
        return angle.memberType();
    }

    @ReportItemFor(value = ReportItem.単純クラス名, order = 3, label = "アノテーションクラス名")
    public TypeIdentifier annotationType(ValidationAngle angle) {
        return angle.annotationType();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, order = 4, label = "アノテーション記述")
    public String annotationDescription(ValidationAngle angle) {
        return angle.annotationDescription();
    }
}
