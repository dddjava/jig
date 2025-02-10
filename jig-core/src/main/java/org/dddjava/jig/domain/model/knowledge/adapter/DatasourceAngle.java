package org.dddjava.jig.domain.model.knowledge.adapter;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.classes.rdbaccess.SqlType;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.method.CallerMethods;
import org.dddjava.jig.domain.model.information.method.JigMethod;
import org.dddjava.jig.domain.model.information.outputs.DatasourceMethod;

/**
 * データソースの切り口
 */
public class DatasourceAngle {

    MethodDeclaration methodDeclaration;
    MyBatisStatements myBatisStatements;
    JigMethod concreteMethod;

    CallerMethods callerMethods;

    public DatasourceAngle(DatasourceMethod datasourceMethod, MyBatisStatements allMyBatisStatements, CallerMethods callerMethods) {
        this.methodDeclaration = datasourceMethod.repositoryMethod().declaration();
        this.callerMethods = callerMethods;
        this.myBatisStatements = allMyBatisStatements.filterRelationOn(datasourceMethod.usingMethods());
        this.concreteMethod = datasourceMethod.concreteMethod();
    }

    public TypeIdentifier declaringType() {
        return methodDeclaration.declaringType();
    }

    public MethodDeclaration method() {
        return methodDeclaration;
    }

    public String insertTables() {
        return myBatisStatements.tables(SqlType.INSERT).asText();
    }

    public String selectTables() {
        return myBatisStatements.tables(SqlType.SELECT).asText();
    }

    public String updateTables() {
        return myBatisStatements.tables(SqlType.UPDATE).asText();
    }

    public String deleteTables() {
        return myBatisStatements.tables(SqlType.DELETE).asText();
    }

    public JigMethod concreteMethod() {
        return concreteMethod;
    }

    public CallerMethods callerMethods() {
        return callerMethods;
    }
}
