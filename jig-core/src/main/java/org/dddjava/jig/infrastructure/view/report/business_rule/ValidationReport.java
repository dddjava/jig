package org.dddjava.jig.infrastructure.view.report.business_rule;

import org.dddjava.jig.domain.model.models.domains.validations.Validation;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.infrastructure.view.report.ReportItem;
import org.dddjava.jig.infrastructure.view.report.ReportItemFor;
import org.dddjava.jig.infrastructure.view.report.ReportTitle;

@ReportTitle("VALIDATION")
public class ValidationReport {

    Validation angle;

    public ValidationReport(Validation angle) {
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
