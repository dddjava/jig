package org.dddjava.jig.domain.model.knowledge.datasource;

import org.dddjava.jig.domain.model.data.rdbaccess.SqlStatementId;
import org.dddjava.jig.domain.model.data.rdbaccess.SqlStatements;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.outputs.OutputImplementation;
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
                        return gatewayUseSQL(outputImplementation, sqlStatementId) || invocationUseSQL(outputImplementation, sqlStatementId);
                    }).crudTables();

                    return new DatasourceAngle(outputImplementation, crudTables, callerMethods);
                })
                .sorted(Comparator.comparing(datasourceAngle -> datasourceAngle.interfaceMethod().jigMethodId().value()))
                .toList());
    }

    /**
     * InvocationがDBアクセスしているかを判定する
     *
     * 使用しているメソッドがSQLステートメントかで判断する
     * TODO プライベートメソッドとか辿らないといけないような・・・
     */
    private static boolean invocationUseSQL(OutputImplementation outputImplementation, SqlStatementId sqlStatementId) {
        return outputImplementation.usingMethods()
                // namespaceはメソッドの型のFQNに該当し、idはメソッド名に該当するので、それを比較する。
                .containsAny(methodCall -> sqlStatementId.matches(methodCall.methodOwner().fqn(), methodCall.methodName()));
    }

    /**
     * GatewayがDBアクセスするものかを判定する
     *
     * SpringDataJDBCを直接Serviceで使用している場合などにRepositoryインタフェースとSQLステートメントが一致する。
     */
    private static boolean gatewayUseSQL(OutputImplementation outputImplementation, SqlStatementId sqlStatementId) {
        var gatewayMethodId = outputImplementation.outputPortGateway().jigMethodId();
        // namespaceはメソッドの型のFQNに該当し、idはメソッド名に該当するので、それを比較する。
        return sqlStatementId.matches(gatewayMethodId.namespace(), gatewayMethodId.name());
    }
}
