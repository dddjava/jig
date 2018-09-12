package org.dddjava.jig.presentation.view.report.application;

import org.dddjava.jig.domain.model.datasources.DatasourceAngle;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("REPOSITORY")
public class RepositoryReport {

    DatasourceAngle angle;

    public RepositoryReport(DatasourceAngle angle) {
        this.angle = angle;
    }

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス和名)
    @ReportItemFor(ReportItem.メソッドシグネチャ)
    @ReportItemFor(ReportItem.メソッド戻り値の型)
    public MethodDeclaration method() {
        return angle.method();
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
}
