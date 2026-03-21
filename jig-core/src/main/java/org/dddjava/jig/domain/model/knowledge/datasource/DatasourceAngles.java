package org.dddjava.jig.domain.model.knowledge.datasource;

import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperation;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperationId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorRepository;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationType;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.outbound.pair.OutboundImplementation;
import org.dddjava.jig.domain.model.information.outbound.pair.OutboundImplementations;
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

    public static DatasourceAngles from(OutboundImplementations outboundImplementations, PersistenceAccessorRepository persistenceAccessorRepository, CallerMethodsFactory callerMethodsFactory) {
        return new DatasourceAngles(outboundImplementations.stream()
                .map(outboundImplementation -> {
                    CallerMethods callerMethods = callerMethodsFactory.callerMethodsOf(outboundImplementation.outboundPortOperaionAsJigMethod().jigMethodId());

                    // 内部で呼び出している永続化操作を操作の種類ごとに収集する
                    Map<PersistenceOperationType, List<String>> map = persistenceAccessorRepository.values().stream()
                            .flatMap(ops -> ops.persistenceAccessorOperations().stream())
                            .filter(persistenceAccessor -> {
                                PersistenceAccessorOperationId persistenceAccessorOperationId = persistenceAccessor.id();
                                return outboundPortOperationUseSQL(outboundImplementation, persistenceAccessorOperationId)
                                        || outboundAdapterExecutionUseSQL(outboundImplementation, persistenceAccessorOperationId);
                            })
                            .collect(groupingBy(PersistenceAccessorOperation::statementOperationType,
                                    Collectors.collectingAndThen(Collectors.toList(),
                                            // テーブル名の重複を排除してソートしたリストにする
                                            l -> l.stream()
                                                    .flatMap(persistenceAccessor -> persistenceAccessor.targetOperationTypes().persistenceTargets().stream())
                                                    .map(persistenceOperation -> persistenceOperation.persistenceTarget().name())
                                                    .distinct()
                                                    .sorted()
                                                    .toList())));

                    return new DatasourceAngle(outboundImplementation, map, callerMethods);
                })
                .sorted(Comparator.comparing(datasourceAngle -> datasourceAngle.interfaceMethod().jigMethodId().value()))
                .toList());
    }

    /**
     * OutboundAdapterExecutionがDBアクセスしているかを判定する
     *
     * OutboundAdapterExecutionに紐づく永続化操作で判断する
     */
    private static boolean outboundAdapterExecutionUseSQL(OutboundImplementation outboundImplementation, PersistenceAccessorOperationId persistenceAccessorOperationId) {
        return outboundImplementation.outboundAdapterExecution().uses(persistenceAccessorOperationId);
    }

    /**
     * OutboundPortOperationがDBアクセスするものかを判定する
     *
     * SpringDataJDBCを直接Serviceで使用している場合などにRepositoryインタフェースとSQLステートメントが一致する。
     */
    private static boolean outboundPortOperationUseSQL(OutboundImplementation outboundImplementation, PersistenceAccessorOperationId persistenceAccessorOperationId) {
        var operationMethodId = outboundImplementation.outboundPortOperaionAsJigMethod().jigMethodId();
        // namespaceはメソッドの型のFQNに該当し、idはメソッド名に該当するので、それを比較する。
        return persistenceAccessorOperationId.matches(TypeId.valueOf(operationMethodId.namespace()), operationMethodId.name());
    }
}
