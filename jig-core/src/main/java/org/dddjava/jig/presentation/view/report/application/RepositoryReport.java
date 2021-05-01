package org.dddjava.jig.presentation.view.report.application;

import org.dddjava.jig.domain.model.models.infrastructures.DatasourceAngle;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.class_.method.MethodDeclaration;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("REPOSITORY")
public class RepositoryReport {

    DatasourceAngle angle;

    public RepositoryReport(DatasourceAngle angle) {
        this.angle = angle;
    }

    @ReportItemFor(ReportItem.パッケージ名)
    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス別名)
    @ReportItemFor(ReportItem.メソッドシグネチャ)
    @ReportItemFor(ReportItem.メソッド戻り値の型)
    @ReportItemFor(ReportItem.メソッド引数の型の別名)
    @ReportItemFor(ReportItem.メソッド戻り値の型の別名)
    public MethodDeclaration method() {
        return angle.method();
    }

    @ReportItemFor(value = ReportItem.分岐数)
    public JigMethod concreteMethod() {
        return angle.concreteMethod();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "INSERT", order = 1)
    public String insertTables() {
        return angle.insertTables();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "SELECT", order = 2)
    public String selectTables() {
        return angle.selectTables();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "UPDATE", order = 3)
    public String updateTables() {
        return angle.updateTables();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "DELETE", order = 4)
    public String deleteTables() {
        return angle.deleteTables();
    }

    @ReportItemFor(value = ReportItem.汎用数値, label = "関連元クラス数", order = 11)
    public int classNumber() {
        return angle.callerMethods().toDeclareTypes().size();
    }

    @ReportItemFor(value = ReportItem.汎用数値, label = "関連元メソッド数", order = 12)
    public int methodNumber() {
        return angle.callerMethods().size();
    }
}
