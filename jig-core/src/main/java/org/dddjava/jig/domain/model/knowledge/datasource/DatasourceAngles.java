package org.dddjava.jig.domain.model.knowledge.datasource;

import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatementId;
import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.outputs.OutputImplementations;
import org.dddjava.jig.domain.model.information.relation.methods.CallerMethodsFactory;

import java.util.Comparator;
import java.util.List;

/**
 * データソースの切り口一覧
 */
public record DatasourceAngles(List<DatasourceAngle> list) {

    public static DatasourceAngles from(OutputImplementations outputImplementations, MyBatisStatements myBatisStatements, CallerMethodsFactory callerMethodsFactory) {
        return new DatasourceAngles(outputImplementations.stream()
                .map(outputImplementation -> {
                    CallerMethods callerMethods = callerMethodsFactory.callerMethodsOf(outputImplementation.outputPortGateway().jigMethodId());

                    var crudTables = myBatisStatements.filterRelationOn(myBatisStatement -> {
                        MyBatisStatementId myBatisStatementId = myBatisStatement.myBatisStatementId();
                        boolean matchesSelf = outputImplementation.outputPortGateway().jigMethodId().namespace().equals(myBatisStatementId.namespace())
                                && outputImplementation.outputPortGateway().name().equals(myBatisStatementId.id());
                        // namespaceはメソッドの型のFQNに該当し、idはメソッド名に該当するので、それを比較する。
                        return matchesSelf || outputImplementation.usingMethods()
                                .containsAny(methodCall -> methodCall.methodOwner().fqn().equals(myBatisStatementId.namespace())
                                        && methodCall.methodName().equals(myBatisStatementId.id()));
                    }).crudTables();

                    return new DatasourceAngle(outputImplementation, crudTables, callerMethods);
                })
                .sorted(Comparator.comparing(datasourceAngle -> datasourceAngle.interfaceMethod().jigMethodId().value()))
                .toList());
    }
}
