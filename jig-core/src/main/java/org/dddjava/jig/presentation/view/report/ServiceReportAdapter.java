package org.dddjava.jig.presentation.view.report;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.services.ServiceAngle;

@ReportTitle("SERVICE")
public class ServiceReportAdapter {

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス和名)
    public TypeIdentifier declaringType(ServiceAngle angle) {
        return angle.declaringType();
    }

    @ReportItemFor(ReportItem.メソッドシグネチャ)
    @ReportItemFor(ReportItem.メソッド和名)
    @ReportItemFor(ReportItem.メソッド戻り値の型)
    @ReportItemFor(ReportItem.メソッド戻り値の型の和名)
    @ReportItemFor(ReportItem.メソッド引数の型の和名)
    public MethodDeclaration method(ServiceAngle angle) {
        return angle.method();
    }

    @ReportItemFor(ReportItem.イベントハンドラ)
    public boolean usingFromController(ServiceAngle angle) {
        return angle.usingFromController();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "使用しているフィールドの型", order = 1)
    public String usingFields(ServiceAngle angle) {
        return angle.usingFields();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "使用しているリポジトリのメソッド", order = 2)
    public String usingRepositoryMethods(ServiceAngle angle) {
        return angle.usingRepositoryMethods();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "stream使用", order = 3)
    public boolean useStream(ServiceAngle angle) {
        return angle.useStream();
    }
}
