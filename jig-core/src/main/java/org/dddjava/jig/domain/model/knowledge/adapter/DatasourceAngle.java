package org.dddjava.jig.domain.model.knowledge.adapter;

import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.rdbaccess.SqlType;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.outputs.DatasourceMethod;

import java.util.stream.Stream;

/**
 * データソースの切り口
 */
public class DatasourceAngle {

    private final DatasourceMethod datasourceMethod;
    private final JigMethod interfaceMethod;
    MyBatisStatements myBatisStatements;
    JigMethod concreteMethod;

    CallerMethods callerMethods;

    public DatasourceAngle(DatasourceMethod datasourceMethod, MyBatisStatements allMyBatisStatements, CallerMethods callerMethods) {
        this.interfaceMethod = datasourceMethod.repositoryMethod();
        this.datasourceMethod = datasourceMethod;
        this.callerMethods = callerMethods;
        this.myBatisStatements = allMyBatisStatements.filterRelationOn(datasourceMethod.usingMethods().methodCalls());
        this.concreteMethod = datasourceMethod.concreteMethod();
    }

    public TypeIdentifier declaringType() {
        return datasourceMethod.interfaceJigType().id();
    }

    public JigMethod interfaceMethod() {
        return interfaceMethod;
    }

    public String nameAndArgumentSimpleText() {
        return interfaceMethod.nameAndArgumentSimpleText();
    }

    public JigTypeReference methodReturnTypeReference() {
        return interfaceMethod.methodReturnTypeReference();
    }

    public Stream<JigTypeReference> methodArgumentTypeReferenceStream() {
        return interfaceMethod.methodArgumentTypeReferenceStream();
    }

    public int decisionCount() {
        return concreteMethod().instructions().decisionCount();
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

    public String packageText() {
        return declaringType().packageIdentifier().asText();
    }
    public String typeSimpleName() {
        return declaringType().asSimpleText();
    }
    public String typeLabel() {
        return datasourceMethod.interfaceJigType().label();
    }
}
