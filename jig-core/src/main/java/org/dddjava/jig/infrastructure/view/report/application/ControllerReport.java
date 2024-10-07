package org.dddjava.jig.infrastructure.view.report.application;

import org.dddjava.jig.domain.model.models.applications.entrypoints.EntrypointMethod;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.classes.method.UsingFields;
import org.dddjava.jig.infrastructure.view.report.ReportItem;
import org.dddjava.jig.infrastructure.view.report.ReportItemFor;
import org.dddjava.jig.infrastructure.view.report.ReportTitle;

@ReportTitle("CONTROLLER")
public class ControllerReport {

    EntrypointMethod entrypointMethod;

    public ControllerReport(EntrypointMethod entrypointMethod) {
        this.entrypointMethod = entrypointMethod;
    }

    @ReportItemFor(ReportItem.パッケージ名)
    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス別名)
    @ReportItemFor(ReportItem.メソッドシグネチャ)
    @ReportItemFor(ReportItem.メソッド戻り値の型)
    @ReportItemFor(ReportItem.分岐数)
    public JigMethod method() {
        return entrypointMethod.method();
    }

    @ReportItemFor(ReportItem.使用しているフィールドの型)
    public UsingFields usingFields() {
        return entrypointMethod.method().usingFields();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "パス", order = 1)
    public String path() {
        return entrypointMethod.pathText();
    }
}
