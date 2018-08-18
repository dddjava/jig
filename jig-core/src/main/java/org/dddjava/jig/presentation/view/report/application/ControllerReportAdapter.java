package org.dddjava.jig.presentation.view.report.application;

import org.dddjava.jig.domain.model.controllers.ControllerAngle;
import org.dddjava.jig.domain.model.declaration.method.Method;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("CONTROLLER")
public class ControllerReportAdapter {

    ControllerAngle angle;

    public ControllerReportAdapter(ControllerAngle angle) {
        this.angle = angle;
    }

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.メソッドシグネチャ)
    @ReportItemFor(ReportItem.メソッド戻り値の型)
    public Method method() {
        return angle.method();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "パス")
    public String path() {
        return angle.controllerAnnotations().pathTexts();
    }
}
