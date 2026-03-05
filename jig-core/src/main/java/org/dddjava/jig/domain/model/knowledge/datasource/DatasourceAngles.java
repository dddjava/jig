package org.dddjava.jig.domain.model.knowledge.datasource;

import org.dddjava.jig.domain.model.data.persistence.PersistenceOperation;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationsRepository;
import org.dddjava.jig.domain.model.data.persistence.SqlType;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.outputs.OutputImplementation;
import org.dddjava.jig.domain.model.information.outputs.OutputImplementations;
import org.dddjava.jig.domain.model.information.relation.methods.CallerMethodsFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * データソースの切り口一覧
 */
public record DatasourceAngles(List<DatasourceAngle> list) {

    public static DatasourceAngles from(OutputImplementations outputImplementations, PersistenceOperationsRepository persistenceOperationsRepository, CallerMethodsFactory callerMethodsFactory) {
        return new DatasourceAngles(outputImplementations.stream()
                .map(outputImplementation -> {
                    CallerMethods callerMethods = callerMethodsFactory.callerMethodsOf(outputImplementation.outputPortOperaionAsJigMethod().jigMethodId());

                    // 内部で呼び出している永続化操作を操作の種類ごとに収集する
                    Map<SqlType, List<String>> map = persistenceOperationsRepository.values().stream()
                            .flatMap(ops -> ops.persistenceOperations().stream())
                            .filter(persistenceOperation -> {
                                PersistenceOperationId persistenceOperationId = persistenceOperation.persistenceOperationId();
                                return outputPortOperationUseSQL(outputImplementation, persistenceOperationId)
                                        || outputAdapterExecutionUseSQL(outputImplementation, persistenceOperationId);
                            })
                            .collect(groupingBy(PersistenceOperation::sqlType,
                                    Collectors.collectingAndThen(Collectors.toList(),
                                            // テーブル名の重複を排除してソートしたリストにする
                                            l -> l.stream()
                                                    .flatMap(persistenceOperation -> persistenceOperation.persistenceTargets().persistenceTargets().stream())
                                                    .map(persistenceTarget -> persistenceTarget.name())
                                                    .distinct()
                                                    .sorted()
                                                    .toList())));

                    return new DatasourceAngle(outputImplementation, map, callerMethods);
                })
                .sorted(Comparator.comparing(datasourceAngle -> datasourceAngle.interfaceMethod().jigMethodId().value()))
                .toList());
    }

    /**
     * OutputAdapterExecutionがDBアクセスしているかを判定する
     *
     * OutputAdapterExecutionに紐づく永続化操作で判断する
     */
    private static boolean outputAdapterExecutionUseSQL(OutputImplementation outputImplementation, PersistenceOperationId persistenceOperationId) {
        return outputImplementation.outputAdapterExecution().uses(persistenceOperationId);
    }

    /**
     * OutputPortOperationがDBアクセスするものかを判定する
     *
     * SpringDataJDBCを直接Serviceで使用している場合などにRepositoryインタフェースとSQLステートメントが一致する。
     */
    private static boolean outputPortOperationUseSQL(OutputImplementation outputImplementation, PersistenceOperationId persistenceOperationId) {
        var operationMethodId = outputImplementation.outputPortOperaionAsJigMethod().jigMethodId();
        // namespaceはメソッドの型のFQNに該当し、idはメソッド名に該当するので、それを比較する。
        return persistenceOperationId.matches(TypeId.valueOf(operationMethodId.namespace()), operationMethodId.name());
    }
}
