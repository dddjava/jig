package org.dddjava.jig.domain.model.knowledge.datasource;

import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatementId;
import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.outputs.OutputImplementation;

import java.util.stream.Stream;

/**
 * データソースの切り口
 */
public class DatasourceAngle {

    private final OutputImplementation outputImplementation;
    private final JigMethod interfaceMethod;
    private final CrudTables crudTables;
    private final JigMethod concreteMethod;

    private final CallerMethods callerMethods;

    public DatasourceAngle(OutputImplementation outputImplementation, MyBatisStatements allMyBatisStatements, CallerMethods callerMethods) {
        this.interfaceMethod = outputImplementation.outputPortGateway();
        this.outputImplementation = outputImplementation;
        this.callerMethods = callerMethods;
        var myBatisStatements = allMyBatisStatements.filterRelationOn(myBatisStatement -> {
            MyBatisStatementId myBatisStatementId = myBatisStatement.myBatisStatementId();
            // namespaceはメソッドの型のFQNに該当し、idはメソッド名に該当するので、それを比較する。
            return outputImplementation.usingMethods()
                    .containsAny(methodCall -> methodCall.methodOwner().fqn().equals(myBatisStatementId.namespace())
                            && methodCall.methodName().equals(myBatisStatementId.id()));
        });
        this.crudTables = myBatisStatements.crudTables();

        this.concreteMethod = outputImplementation.concreteMethod();
    }

    public TypeId declaringType() {
        return outputImplementation.interfaceJigType().id();
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

    public int cyclomaticComplexity() {
        return concreteMethod().instructions().cyclomaticComplexity();
    }

    public String insertTables() {
        return crudTables.create().asText();
    }

    public String selectTables() {
        return crudTables.read().asText();
    }

    public String updateTables() {
        return crudTables.update().asText();
    }

    public String deleteTables() {
        return crudTables.delete().asText();
    }

    public JigMethod concreteMethod() {
        return concreteMethod;
    }

    public CallerMethods callerMethods() {
        return callerMethods;
    }

    public String packageText() {
        return declaringType().packageId().asText();
    }

    public String typeSimpleName() {
        return declaringType().asSimpleText();
    }

    public String typeLabel() {
        return outputImplementation.interfaceJigType().label();
    }
}
