package org.dddjava.jig.presentation.view.report.domain;

import org.dddjava.jig.domain.model.booleans.model.BoolQueryAngle;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("真偽値を返すメソッド")
public class BoolQueryReportAdapter {

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス和名)
    @ReportItemFor(ReportItem.メソッドシグネチャ)
    @ReportItemFor(ReportItem.メソッド和名)
    public MethodDeclaration method(BoolQueryAngle angle) {
        return angle.method();
    }

    @ReportItemFor(ReportItem.使用箇所数)
    @ReportItemFor(ReportItem.使用箇所)
    public TypeIdentifiers userTypeIdentifiers(BoolQueryAngle angle) {
        return angle.userTypeIdentifiers();
    }
}
