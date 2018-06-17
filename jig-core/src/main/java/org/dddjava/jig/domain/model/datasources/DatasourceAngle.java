package org.dddjava.jig.domain.model.datasources;

import org.dddjava.jig.domain.model.report.ReportItem;
import org.dddjava.jig.domain.model.report.ReportItemFor;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.ImplementationMethods;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodRelations;
import org.dddjava.jig.domain.model.implementation.datasource.SqlType;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;

/**
 * データソースの切り口
 */
public class DatasourceAngle {

    MethodDeclaration methodDeclaration;
    Sqls sqls;

    public DatasourceAngle(MethodDeclaration methodDeclaration, Sqls sqls) {
        this.methodDeclaration = methodDeclaration;
        this.sqls = sqls;
    }

    public static DatasourceAngle of(MethodDeclaration methodDeclaration, MethodDeclarations mapperMethods, ImplementationMethods implementationMethods, MethodRelations methodRelations, Sqls allSqls) {
        MethodDeclarations datasourceMethods = implementationMethods.stream()
                .filterInterfaceMethodIs(methodDeclaration)
                .concrete();

        MethodDeclarations usingMapperMethods = methodRelations.stream()
                .filterAnyFrom(datasourceMethods)
                .filterAnyTo(mapperMethods)
                .toMethods();

        Sqls sqls = allSqls.filterRelationOn(usingMapperMethods);
        return new DatasourceAngle(methodDeclaration, sqls);
    }

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス和名)
    public TypeIdentifier declaringType() {
        return methodDeclaration.declaringType();
    }

    @ReportItemFor(ReportItem.メソッド名)
    @ReportItemFor(ReportItem.メソッド戻り値の型)
    public MethodDeclaration method() {
        return methodDeclaration;
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "INSERT", order = 1)
    public String insertTables() {
        return sqls.tables(SqlType.INSERT).asText();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "SELECT", order = 2)
    public String selectTables() {
        return sqls.tables(SqlType.SELECT).asText();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "UPDATE", order = 3)
    public String updateTables() {
        return sqls.tables(SqlType.UPDATE).asText();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "DELETE", order = 4)
    public String deleteTables() {
        return sqls.tables(SqlType.DELETE).asText();
    }
}
