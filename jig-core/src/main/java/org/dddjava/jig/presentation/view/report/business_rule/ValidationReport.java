package org.dddjava.jig.presentation.view.report.business_rule;

import org.dddjava.jig.domain.model.jigmodel.domains.validations.ValidationAngle;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("VALIDATION")
public class ValidationReport {

    ValidationAngle angle;

    public ValidationReport(ValidationAngle angle) {
        this.angle = angle;
    }

    @ReportItemFor(ReportItem.パッケージ名)
    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス別名)
    public TypeIdentifier typeIdentifier() {
        return angle.typeIdentifier();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, order = 1, label = "メンバ名")
    public String memberName() {
        return angle.memberName();
    }

    @ReportItemFor(value = ReportItem.単純クラス名, label = "メンバクラス名", order = 2)
    public TypeIdentifier memberType() {
        return angle.memberType();
    }

    @ReportItemFor(value = ReportItem.単純クラス名, order = 3, label = "アノテーションクラス名")
    public TypeIdentifier annotationType() {
        return angle.annotationType();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, order = 4, label = "アノテーション記述")
    public String annotationDescription() {
        return angle.annotationDescription();
    }
}
