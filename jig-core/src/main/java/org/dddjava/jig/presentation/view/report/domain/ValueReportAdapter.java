package org.dddjava.jig.presentation.view.report.domain;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.values.ValueAngle;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;

public class ValueReportAdapter {

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス和名)
    public TypeIdentifier typeIdentifier(ValueAngle angle) {
        return angle.typeIdentifier();
    }

    @ReportItemFor(ReportItem.使用箇所数)
    @ReportItemFor(ReportItem.使用箇所)
    public TypeIdentifiers userTypeIdentifiers(ValueAngle angle) {
        return angle.userTypeIdentifiers();
    }
}
