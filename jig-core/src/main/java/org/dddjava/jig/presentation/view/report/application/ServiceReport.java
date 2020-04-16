package org.dddjava.jig.presentation.view.report.application;

import org.dddjava.jig.domain.model.jigloaded.richmethod.Method;
import org.dddjava.jig.domain.model.jigloaded.richmethod.UsingFields;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngle;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceMethods;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("SERVICE")
public class ServiceReport {

    ServiceAngle angle;

    public ServiceReport(ServiceAngle angle) {
        this.angle = angle;
    }

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス別名)
    @ReportItemFor(ReportItem.メソッドシグネチャ)
    @ReportItemFor(ReportItem.メソッド別名)
    @ReportItemFor(ReportItem.メソッド戻り値の型)
    @ReportItemFor(ReportItem.メソッド戻り値の型の別名)
    @ReportItemFor(ReportItem.メソッド引数の型の別名)
    @ReportItemFor(ReportItem.分岐数)
    public Method method() {
        return angle.serviceMethod().method();
    }

    @ReportItemFor(ReportItem.イベントハンドラ)
    public boolean usingFromController() {
        return angle.usingFromController();
    }

    @ReportItemFor(ReportItem.使用しているフィールドの型)
    public UsingFields usingFields() {
        return angle.usingFields();
    }

    @ReportItemFor(value = ReportItem.メソッド一覧, label = "使用しているサービスのメソッド", order = 1)
    public ServiceMethods usingServiceMethod() {
        return angle.usingServiceMethods();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "使用しているリポジトリのメソッド", order = 2)
    public String usingRepositoryMethods() {
        return angle.usingRepositoryMethods().asSimpleText();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "null使用", order = 3)
    public boolean useNull() {
        return angle.useNull();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "stream使用", order = 4)
    public boolean useStream() {
        return angle.useStream();
    }
}
