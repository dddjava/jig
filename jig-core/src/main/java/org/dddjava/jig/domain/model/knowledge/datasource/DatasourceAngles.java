package org.dddjava.jig.domain.model.knowledge.datasource;

import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperation;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperationId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorRepository;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationType;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.outputs.pair.OutputImplementation;
import org.dddjava.jig.domain.model.information.outputs.pair.OutputImplementations;
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

    public static DatasourceAngles from(OutputImplementations outputImplementations, PersistenceAccessorRepository persistenceAccessorRepository, CallerMethodsFactory callerMethodsFactory) {
        return new DatasourceAngles(outputImplementations.stream()
                .map(outputImplementation -> {
                    CallerMethods callerMethods = callerMethodsFactory.callerMethodsOf(outputImplementation.outputPortOperaionAsJigMethod().jigMethodId());

                    // 内部で呼び出している永続化操作を操作の種類ごとに収集する
                    Map<PersistenceOperationType, List<String>> map = persistenceAccessorRepository.values().stream()
                            .flatMap(ops -> ops.persistenceAccessorOperations().stream())
                            .filter(persistenceAccessor -> {
                                PersistenceAccessorOperationId persistenceAccessorOperationId = persistenceAccessor.persistenceAccessorOperationId();
                                return outputPortOperationUseSQL(outputImplementation, persistenceAccessorOperationId)
                                        || outputAdapterExecutionUseSQL(outputImplementation, persistenceAccessorOperationId);
                            })
                            .collect(groupingBy(PersistenceAccessorOperation::persistenceOperationType,
                                    Collectors.collectingAndThen(Collectors.toList(),
                                            // テーブル名の重複を排除してソートしたリストにする
                                            l -> l.stream()
                                                    .flatMap(persistenceAccessor -> persistenceAccessor.persistenceOperations().persistenceTargets().stream())
                                                    .map(persistenceOperation -> persistenceOperation.persistenceTarget().name())
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
    private static boolean outputAdapterExecutionUseSQL(OutputImplementation outputImplementation, PersistenceAccessorOperationId persistenceAccessorOperationId) {
        return outputImplementation.outputAdapterExecution().uses(persistenceAccessorOperationId);
    }

    /**
     * OutputPortOperationがDBアクセスするものかを判定する
     *
     * SpringDataJDBCを直接Serviceで使用している場合などにRepositoryインタフェースとSQLステートメントが一致する。
     */
    private static boolean outputPortOperationUseSQL(OutputImplementation outputImplementation, PersistenceAccessorOperationId persistenceAccessorOperationId) {
        var operationMethodId = outputImplementation.outputPortOperaionAsJigMethod().jigMethodId();
        // namespaceはメソッドの型のFQNに該当し、idはメソッド名に該当するので、それを比較する。
        return persistenceAccessorOperationId.matches(TypeId.valueOf(operationMethodId.namespace()), operationMethodId.name());
    }
}
