package org.dddjava.jig.presentation.view.report.application;

import org.dddjava.jig.domain.model.models.frontends.HandlerMethod;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.relation.method.UsingFields;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("CONTROLLER")
public class ControllerReport {

    HandlerMethod handlerMethod;

    public ControllerReport(HandlerMethod handlerMethod) {
        this.handlerMethod = handlerMethod;
    }

    @ReportItemFor(ReportItem.パッケージ名)
    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス別名)
    @ReportItemFor(ReportItem.メソッドシグネチャ)
    @ReportItemFor(ReportItem.メソッド戻り値の型)
    @ReportItemFor(ReportItem.分岐数)
    public JigMethod method() {
        return handlerMethod.method();
    }

    @ReportItemFor(ReportItem.使用しているフィールドの型)
    public UsingFields usingFields() {
        return handlerMethod.method().usingFields();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "パス", order = 1)
    public String path() {
        return handlerMethod.pathText();
    }
}
