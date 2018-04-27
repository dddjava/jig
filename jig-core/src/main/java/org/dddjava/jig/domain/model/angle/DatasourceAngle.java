package org.dddjava.jig.domain.model.angle;

import org.dddjava.jig.domain.model.datasource.SqlType;
import org.dddjava.jig.domain.model.datasource.Sqls;
import org.dddjava.jig.domain.model.datasource.Tables;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.datasource.SqlType;
import org.dddjava.jig.domain.model.datasource.Sqls;
import org.dddjava.jig.domain.model.datasource.Tables;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;

public class DatasourceAngle {

    MethodDeclaration methodDeclaration;
    TypeIdentifier returnTypeIdentifier;
    Sqls sqls;

    public DatasourceAngle(MethodDeclaration methodDeclaration, TypeIdentifier returnTypeIdentifier, Sqls sqls) {
        this.methodDeclaration = methodDeclaration;
        this.returnTypeIdentifier = returnTypeIdentifier;
        this.sqls = sqls;
    }

    public MethodDeclaration method() {
        return methodDeclaration;
    }

    public TypeIdentifier returnType() {
        return returnTypeIdentifier;
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
