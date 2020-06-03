package org.dddjava.jig.domain.model.jigmodel.applications.repositories;

import org.dddjava.jig.domain.model.jigmodel.datasource.SqlType;
import org.dddjava.jig.domain.model.jigmodel.datasource.Sqls;
import org.dddjava.jig.domain.model.jigmodel.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.jigmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.richmethod.Method;

/**
 * データソースの切り口
 */
public class DatasourceAngle {

    MethodDeclaration methodDeclaration;
    Sqls sqls;
    Method concreteMethod;

    public DatasourceAngle(DatasourceMethod datasourceMethod, Sqls allSqls) {
        this.methodDeclaration = datasourceMethod.repositoryMethod().declaration();
        this.sqls = allSqls.filterRelationOn(datasourceMethod.usingMethods());
        this.concreteMethod = datasourceMethod.concreteMethod();
    }

    public TypeIdentifier declaringType() {
        return methodDeclaration.declaringType();
    }

    public MethodDeclaration method() {
        return methodDeclaration;
    }

    public String insertTables() {
        return sqls.tables(SqlType.INSERT).asText();
    }

    public String selectTables() {
        return sqls.tables(SqlType.SELECT).asText();
    }

    public String updateTables() {
        return sqls.tables(SqlType.UPDATE).asText();
    }

    public String deleteTables() {
        return sqls.tables(SqlType.DELETE).asText();
    }

    public Method concreteMethod() {
        return concreteMethod;
    }
}
