package org.dddjava.jig.presentation.view.report.business_rule;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.jigpresentation.values.ValueAngle;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;

public class ValueReport {

    ValueAngle angle;

    public ValueReport(ValueAngle angle) {
        this.angle = angle;
    }

    @ReportItemFor(ReportItem.パッケージ名)
    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス別名)
    public TypeIdentifier typeIdentifier() {
        return angle.typeIdentifier();
    }

    @ReportItemFor(ReportItem.使用箇所数)
    @ReportItemFor(ReportItem.使用箇所)
    public TypeIdentifiers userTypeIdentifiers() {
        return angle.userTypeIdentifiers();
    }
}
