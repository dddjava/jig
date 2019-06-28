package org.dddjava.jig.presentation.view.report.application;

import org.dddjava.jig.domain.model.controllers.ControllerAngle;
import org.dddjava.jig.domain.model.richmethod.Method;
import org.dddjava.jig.domain.model.richmethod.UsingFields;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("CONTROLLER")
public class ControllerReport {

    ControllerAngle angle;
    String progressText;

    public ControllerReport(ControllerAngle angle, String progressText) {
        this.angle = angle;
        this.progressText = progressText;
    }

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス別名)
    @ReportItemFor(ReportItem.メソッドシグネチャ)
    @ReportItemFor(ReportItem.メソッド戻り値の型)
    public Method method() {
        return angle.method();
    }

    @ReportItemFor(ReportItem.使用しているフィールドの型)
    public UsingFields usingFields() {
        return angle.usingFields();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "パス", order = 1)
    public String path() {
        return angle.requestHandler().pathText();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "進捗", order = 2)
    public String progress() {
        return progressText;
    }
}
