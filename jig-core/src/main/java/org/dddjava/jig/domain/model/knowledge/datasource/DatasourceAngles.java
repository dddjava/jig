package org.dddjava.jig.domain.model.knowledge.datasource;

import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperation;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperationId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorRepository;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationType;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapters;
import org.dddjava.jig.domain.model.information.outbound.OutboundPortOperation;
import org.dddjava.jig.domain.model.information.relation.methods.CallerMethodsFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

/**
 * データソースの切り口一覧
 */
public record DatasourceAngles(List<DatasourceAngle> list) {

    public static DatasourceAngles from(OutboundAdapters outboundAdapters, PersistenceAccessorRepository persistenceAccessorRepository, CallerMethodsFactory callerMethodsFactory) {
        // ポート操作ごとの全件走査を避けるため、永続化操作をIDで引けるようにしておく
        Map<PersistenceAccessorOperationId, List<PersistenceAccessorOperation>> operationMap = persistenceAccessorRepository.values().stream()
                .flatMap(ops -> ops.persistenceAccessorOperations().stream())
                .collect(groupingBy(PersistenceAccessorOperation::id));

        return new DatasourceAngles(outboundAdapters.stream()
                .flatMap(adapter -> adapter.outboundPortStream()
                        .flatMap(port -> port.operationStream()
                                .flatMap(portOp -> adapter.findExecution(portOp).stream()
                                        .map(exec -> {
                                            CallerMethods callerMethods = callerMethodsFactory.callerMethodsOf(portOp.jigMethod().jigMethodId());

                                            // 内部で呼び出している永続化操作を操作の種類ごとに収集する
                                            Map<PersistenceOperationType, List<String>> map = Stream.concat(
                                                            // OutboundPortOperationがDBアクセスするもの。
                                                            // SpringDataJDBCを直接Serviceで使用している場合などにRepositoryインタフェースとSQLステートメントが一致する。
                                                            Stream.of(portOperationId(portOp)),
                                                            // OutboundAdapterExecutionに紐づく永続化操作
                                                            exec.persistenceAccessorOperations().stream().map(PersistenceAccessorOperation::id))
                                                    .distinct()
                                                    .flatMap(operationId -> operationMap.getOrDefault(operationId, List.of()).stream())
                                                    .collect(groupingBy(PersistenceAccessorOperation::statementOperationType,
                                                            Collectors.collectingAndThen(Collectors.toList(),
                                                                    // テーブル名の重複を排除してソートしたリストにする
                                                                    l -> l.stream()
                                                                            .flatMap(persistenceAccessor -> persistenceAccessor.targetOperationTypes().persistenceTargets().stream())
                                                                            .map(persistenceOperation -> persistenceOperation.persistenceTarget().name())
                                                                            .distinct()
                                                                            .sorted()
                                                                            .toList())));

                                            return new DatasourceAngle(portOp, exec, port, map, callerMethods);
                                        }))))
                .sorted(Comparator.comparing(datasourceAngle -> datasourceAngle.interfaceMethod().jigMethodId().value()))
                .toList());
    }

    /**
     * OutboundPortOperationに対応する永続化操作のIDを求める
     *
     * namespaceはメソッドの型のFQNに該当し、idはメソッド名に該当する。
     */
    private static PersistenceAccessorOperationId portOperationId(OutboundPortOperation portOperation) {
        var operationMethodId = portOperation.jigMethod().jigMethodId();
        return PersistenceAccessorOperationId.fromTypeIdAndName(TypeId.valueOf(operationMethodId.namespace()), operationMethodId.name());
    }
}
