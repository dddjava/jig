package org.dddjava.jig.domain.model.jigmodel.infrastructures;

import org.dddjava.jig.domain.model.jigmodel.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.class_.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.rdbaccess.SqlType;
import org.dddjava.jig.domain.model.parts.rdbaccess.Sqls;
import org.dddjava.jig.domain.model.parts.relation.method.CallerMethods;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;

/**
 * データソースの切り口
 */
public class DatasourceAngle {

    MethodDeclaration methodDeclaration;
    Sqls sqls;
    JigMethod concreteMethod;

    CallerMethods callerMethods;

    public DatasourceAngle(DatasourceMethod datasourceMethod, Sqls allSqls, CallerMethods callerMethods) {
        this.methodDeclaration = datasourceMethod.repositoryMethod().declaration();
        this.callerMethods = callerMethods;
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

    public JigMethod concreteMethod() {
        return concreteMethod;
    }

    public CallerMethods callerMethods() {
        return callerMethods;
    }
}
