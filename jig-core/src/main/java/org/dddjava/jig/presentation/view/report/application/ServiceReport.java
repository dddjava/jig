package org.dddjava.jig.presentation.view.report.application;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.implementation.analyzed.unit.method.UsingFields;
import org.dddjava.jig.domain.model.services.ServiceAngle;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("SERVICE")
public class ServiceReport {

    ServiceAngle angle;
    String progressText;

    public ServiceReport(ServiceAngle angle, String progressText) {
        this.angle = angle;
        this.progressText = progressText;
    }

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス別名)
    @ReportItemFor(ReportItem.メソッドシグネチャ)
    @ReportItemFor(ReportItem.メソッド別名)
    @ReportItemFor(ReportItem.メソッド戻り値の型)
    @ReportItemFor(ReportItem.メソッド戻り値の型の別名)
    @ReportItemFor(ReportItem.メソッド引数の型の別名)
    public MethodDeclaration method() {
        return angle.method();
    }

    @ReportItemFor(ReportItem.イベントハンドラ)
    public boolean usingFromController() {
        return angle.usingFromController();
    }

    @ReportItemFor(ReportItem.使用しているフィールドの型)
    public UsingFields usingFields() {
        return angle.usingFields();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "使用しているリポジトリのメソッド", order = 2)
    public String usingRepositoryMethods() {
        return angle.usingRepositoryMethods();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "stream使用", order = 3)
    public boolean useStream() {
        return angle.useStream();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "進捗", order = 4)
    public String progress() {
        return progressText;
    }
}
