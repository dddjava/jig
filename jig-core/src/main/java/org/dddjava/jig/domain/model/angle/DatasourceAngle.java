package org.dddjava.jig.domain.model.angle;

import org.dddjava.jig.domain.model.implementation.datasource.SqlType;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.implementation.datasource.Tables;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;

public class DatasourceAngle {

    MethodDeclaration methodDeclaration;
    Sqls sqls;

    public DatasourceAngle(MethodDeclaration methodDeclaration, Sqls sqls) {
        this.methodDeclaration = methodDeclaration;
        this.sqls = sqls;
    }

    public MethodDeclaration method() {
        return methodDeclaration;
    }

    public TypeIdentifier returnType() {
        return methodDeclaration.returnType();
    }

    public Tables deleteTables() {
        return sqls.tables(SqlType.DELETE);
    }

    public Tables updateTables() {
        return sqls.tables(SqlType.UPDATE);
    }

    public Tables selectTables() {
        return sqls.tables(SqlType.SELECT);
    }

    public Tables insertTables() {
        return sqls.tables(SqlType.INSERT);
    }
}
