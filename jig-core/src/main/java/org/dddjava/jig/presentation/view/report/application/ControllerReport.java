package org.dddjava.jig.presentation.view.report.application;

import org.dddjava.jig.domain.model.richmethod.Method;
import org.dddjava.jig.domain.model.richmethod.RequestHandlerMethod;
import org.dddjava.jig.domain.model.richmethod.UsingFields;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("CONTROLLER")
public class ControllerReport {

    RequestHandlerMethod requestHandlerMethod;
    String progressText;

    public ControllerReport(RequestHandlerMethod requestHandlerMethod, String progressText) {
        this.requestHandlerMethod = requestHandlerMethod;
        this.progressText = progressText;
    }

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス別名)
    @ReportItemFor(ReportItem.メソッドシグネチャ)
    @ReportItemFor(ReportItem.メソッド戻り値の型)
    public Method method() {
        return requestHandlerMethod.method();
    }

    @ReportItemFor(ReportItem.使用しているフィールドの型)
    public UsingFields usingFields() {
        return requestHandlerMethod.method().usingFields();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "パス", order = 1)
    public String path() {
        return requestHandlerMethod.pathText();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "進捗", order = 2)
    public String progress() {
        return progressText;
    }
}
