package org.dddjava.jig.presentation.view.report;

import org.dddjava.jig.domain.model.decisions.DecisionAngle;
import org.dddjava.jig.domain.model.declaration.method.Method;

public class DecisionReportAdapter {

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.メソッドシグネチャ)
    @ReportItemFor(ReportItem.分岐数)
    public Method method(DecisionAngle angle) {
        return angle.method();
    }
}
