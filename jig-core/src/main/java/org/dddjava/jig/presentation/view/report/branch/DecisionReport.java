package org.dddjava.jig.presentation.view.report.branch;

import org.dddjava.jig.domain.model.angle.decisions.DecisionAngle;
import org.dddjava.jig.domain.model.unit.method.Method;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;

public class DecisionReport {

    DecisionAngle angle;

    public DecisionReport(DecisionAngle angle) {
        this.angle = angle;
    }

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.メソッドシグネチャ)
    @ReportItemFor(ReportItem.分岐数)
    public Method method() {
        return angle.method();
    }
}
