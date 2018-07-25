package org.dddjava.jig.presentation.view.report;

import org.dddjava.jig.domain.model.datasources.DatasourceAngle;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.report.ReportItem;
import org.dddjava.jig.domain.model.report.ReportItemFor;

@ReportTitle("REPOSITORY")
public class RepositoryReportAdapter {

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス和名)
    public TypeIdentifier declaringType(DatasourceAngle angle) {
        return angle.declaringType();
    }

    @ReportItemFor(ReportItem.メソッドシグネチャ)
    @ReportItemFor(ReportItem.メソッド戻り値の型)
    public MethodDeclaration method(DatasourceAngle angle) {
        return angle.method();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "INSERT", order = 1)
    public String insertTables(DatasourceAngle angle) {
        return angle.insertTables();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "SELECT", order = 2)
    public String selectTables(DatasourceAngle angle) {
        return angle.selectTables();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "UPDATE", order = 3)
    public String updateTables(DatasourceAngle angle) {
        return angle.updateTables();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "DELETE", order = 4)
    public String deleteTables(DatasourceAngle angle) {
        return angle.deleteTables();
    }
}
