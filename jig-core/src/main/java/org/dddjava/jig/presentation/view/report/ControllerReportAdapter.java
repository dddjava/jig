package org.dddjava.jig.presentation.view.report;

import org.dddjava.jig.domain.model.controllers.ControllerAngle;
import org.dddjava.jig.domain.model.declaration.method.Method;
import org.dddjava.jig.domain.model.report.ReportItem;
import org.dddjava.jig.domain.model.report.ReportItemFor;

@ReportTitle("CONTROLLER")
public class ControllerReportAdapter {

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.メソッドシグネチャ)
    @ReportItemFor(ReportItem.メソッド戻り値の型)
    public Method method(ControllerAngle angle) {
        return angle.method();
    }
}
