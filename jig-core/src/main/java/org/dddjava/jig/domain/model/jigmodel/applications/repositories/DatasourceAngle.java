package org.dddjava.jig.domain.model.jigmodel.applications.repositories;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigloaded.datasource.SqlType;
import org.dddjava.jig.domain.model.jigloaded.datasource.Sqls;

/**
 * データソースの切り口
 */
public class DatasourceAngle {

    MethodDeclaration methodDeclaration;
    Sqls sqls;

    public DatasourceAngle(DatasourceMethod datasourceMethod, Sqls allSqls) {
        this.methodDeclaration = datasourceMethod.repositoryMethod().declaration();
        this.sqls = allSqls.filterRelationOn(datasourceMethod.usingMethods());
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
}
