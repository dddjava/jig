package org.dddjava.jig.domain.model.knowledge.datasource;

import org.dddjava.jig.domain.model.data.rdbaccess.PersistenceOperationId;
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
                    CallerMethods callerMethods = callerMethodsFactory.callerMethodsOf(outputImplementation.outputPortOperaionAsJigMethod().jigMethodId());

                    var crudTables = sqlStatements.filterRelationOn(sqlStatement -> {
                        PersistenceOperationId persistenceOperationId = sqlStatement.persistenceOperationId();
                        return outputPortOperationUseSQL(outputImplementation, persistenceOperationId) || outputAdapterExecutionUseSQL(outputImplementation, persistenceOperationId);
                    }).crudTables();

                    return new DatasourceAngle(outputImplementation, crudTables, callerMethods);
                })
                .sorted(Comparator.comparing(datasourceAngle -> datasourceAngle.interfaceMethod().jigMethodId().value()))
                .toList());
    }

    /**
     * OutputAdapterExecutionがDBアクセスしているかを判定する
     *
     * 使用しているメソッドがSQLステートメントかで判断する
     * TODO プライベートメソッドとか辿らないといけないような・・・
     */
    private static boolean outputAdapterExecutionUseSQL(OutputImplementation outputImplementation, PersistenceOperationId persistenceOperationId) {
        return outputImplementation.usingMethods()
                // namespaceはメソッドの型のFQNに該当し、idはメソッド名に該当するので、それを比較する。
                .containsAny(methodCall -> persistenceOperationId.matches(methodCall.methodOwner().fqn(), methodCall.methodName()));
    }

    /**
     * OutputPortOperationがDBアクセスするものかを判定する
     *
     * SpringDataJDBCを直接Serviceで使用している場合などにRepositoryインタフェースとSQLステートメントが一致する。
     */
    private static boolean outputPortOperationUseSQL(OutputImplementation outputImplementation, PersistenceOperationId persistenceOperationId) {
        var operationMethodId = outputImplementation.outputPortOperaionAsJigMethod().jigMethodId();
        // namespaceはメソッドの型のFQNに該当し、idはメソッド名に該当するので、それを比較する。
        return persistenceOperationId.matches(operationMethodId.namespace(), operationMethodId.name());
    }
}
