package org.dddjava.jig.domain.model.knowledge.datasource;

import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperation;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorOperationId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorRepository;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationType;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapter;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapters;
import org.dddjava.jig.domain.model.information.outbound.OutboundPort;
import org.dddjava.jig.domain.model.information.outbound.OutboundPortOperation;
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

    public static DatasourceAngles from(OutboundAdapters outboundAdapters, PersistenceAccessorRepository persistenceAccessorRepository, CallerMethodsFactory callerMethodsFactory) {
        return new DatasourceAngles(outboundAdapters.stream()
                .flatMap(adapter -> outboundPorts(adapter)
                        .flatMap(port -> port.operationStream()
                                .flatMap(portOp -> adapter.findExecution(portOp).stream()
                                        .map(exec -> {
                                            CallerMethods callerMethods = callerMethodsFactory.callerMethodsOf(portOp.jigMethod().jigMethodId());

                                            // 内部で呼び出している永続化操作を操作の種類ごとに収集する
                                            Map<PersistenceOperationType, List<String>> map = persistenceAccessorRepository.values().stream()
                                                    .flatMap(ops -> ops.persistenceAccessorOperations().stream())
                                                    .filter(persistenceAccessor -> {
                                                        PersistenceAccessorOperationId persistenceAccessorOperationId = persistenceAccessor.id();
                                                        return outboundPortOperationUseSQL(portOp, persistenceAccessorOperationId)
                                                                || outboundAdapterExecutionUseSQL(exec, persistenceAccessorOperationId);
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

                                            return new DatasourceAngle(portOp, exec, port, map, callerMethods);
                                        }))))
                .sorted(Comparator.comparing(datasourceAngle -> datasourceAngle.interfaceMethod().jigMethodId().value()))
                .toList());
    }

    private static java.util.stream.Stream<OutboundPort> outboundPorts(OutboundAdapter outboundAdapter) {
        // interfaceのRepository(Spring Data JDBCなど)は実装クラスが存在しないため、自身をoutput portとして扱う
        var jigType = outboundAdapter.jigType();
        if (jigType.jigTypeHeader().javaTypeDeclarationKind() == org.dddjava.jig.domain.model.data.types.JavaTypeDeclarationKind.INTERFACE) {
            return java.util.stream.Stream.of(new OutboundPort(jigType));
        }
        return outboundAdapter.implementsPortStream();
    }

    /**
     * OutboundAdapterExecutionがDBアクセスしているかを判定する
     *
     * OutboundAdapterExecutionに紐づく永続化操作で判断する
     */
    private static boolean outboundAdapterExecutionUseSQL(org.dddjava.jig.domain.model.information.outbound.OutboundAdapterExecution adapterExecution, PersistenceAccessorOperationId persistenceAccessorOperationId) {
        return adapterExecution.uses(persistenceAccessorOperationId);
    }

    /**
     * OutboundPortOperationがDBアクセスするものかを判定する
     *
     * SpringDataJDBCを直接Serviceで使用している場合などにRepositoryインタフェースとSQLステートメントが一致する。
     */
    private static boolean outboundPortOperationUseSQL(OutboundPortOperation portOperation, PersistenceAccessorOperationId persistenceAccessorOperationId) {
        var operationMethodId = portOperation.jigMethod().jigMethodId();
        // namespaceはメソッドの型のFQNに該当し、idはメソッド名に該当するので、それを比較する。
        return persistenceAccessorOperationId.matches(TypeId.valueOf(operationMethodId.namespace()), operationMethodId.name());
    }
}
