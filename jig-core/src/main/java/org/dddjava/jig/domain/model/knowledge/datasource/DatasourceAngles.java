package org.dddjava.jig.domain.model.knowledge.datasource;

import org.dddjava.jig.domain.model.data.rdbaccess.SqlStatementId;
import org.dddjava.jig.domain.model.data.rdbaccess.SqlStatements;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.outputs.OutputImplementations;
import org.dddjava.jig.domain.model.information.relation.methods.CallerMethodsFactory;

import java.util.Comparator;
import java.util.List;

/**
 * データソースの切り口一覧
 */
public record DatasourceAngles(List<DatasourceAngle> list) {

    public static DatasourceAngles from(OutputImplementations outputImplementations, SqlStatements sqlStatements, CallerMethodsFactory callerMethodsFactory) {
        return new DatasourceAngles(outputImplementations.stream()
                .map(outputImplementation -> {
                    CallerMethods callerMethods = callerMethodsFactory.callerMethodsOf(outputImplementation.outputPortGateway().jigMethodId());

                    var crudTables = sqlStatements.filterRelationOn(sqlStatement -> {
                        SqlStatementId sqlStatementId = sqlStatement.sqlStatementId();
                        boolean matchesSelf = outputImplementation.outputPortGateway().jigMethodId().namespace().equals(sqlStatementId.namespace())
                                && outputImplementation.outputPortGateway().name().equals(sqlStatementId.id());
                        // namespaceはメソッドの型のFQNに該当し、idはメソッド名に該当するので、それを比較する。
                        return matchesSelf || outputImplementation.usingMethods()
                                .containsAny(methodCall -> methodCall.methodOwner().fqn().equals(sqlStatementId.namespace())
                                        && methodCall.methodName().equals(sqlStatementId.id()));
                    }).crudTables();

                    return new DatasourceAngle(outputImplementation, crudTables, callerMethods);
                })
                .sorted(Comparator.comparing(datasourceAngle -> datasourceAngle.interfaceMethod().jigMethodId().value()))
                .toList());
    }
}
