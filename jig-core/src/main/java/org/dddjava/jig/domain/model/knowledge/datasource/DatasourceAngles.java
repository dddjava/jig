package org.dddjava.jig.domain.model.knowledge.datasource;

import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessor;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorsRepository;
import org.dddjava.jig.domain.model.data.persistence.SqlType;
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

    public static DatasourceAngles from(OutputImplementations outputImplementations, PersistenceAccessorsRepository persistenceAccessorsRepository, CallerMethodsFactory callerMethodsFactory) {
        return new DatasourceAngles(outputImplementations.stream()
                .map(outputImplementation -> {
                    CallerMethods callerMethods = callerMethodsFactory.callerMethodsOf(outputImplementation.outputPortOperaionAsJigMethod().jigMethodId());

                    // 内部で呼び出している永続化操作を操作の種類ごとに収集する
                    Map<SqlType, List<String>> map = persistenceAccessorsRepository.values().stream()
                            .flatMap(ops -> ops.persistenceAccessors().stream())
                            .filter(persistenceAccessor -> {
                                PersistenceAccessorId persistenceAccessorId = persistenceAccessor.persistenceAccessorId();
                                return outputPortOperationUseSQL(outputImplementation, persistenceAccessorId)
                                        || outputAdapterExecutionUseSQL(outputImplementation, persistenceAccessorId);
                            })
                            .collect(groupingBy(PersistenceAccessor::sqlType,
                                    Collectors.collectingAndThen(Collectors.toList(),
                                            // テーブル名の重複を排除してソートしたリストにする
                                            l -> l.stream()
                                                    .flatMap(persistenceAccessor -> persistenceAccessor.persistenceTargets().persistenceTargets().stream())
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
    private static boolean outputAdapterExecutionUseSQL(OutputImplementation outputImplementation, PersistenceAccessorId persistenceAccessorId) {
        return outputImplementation.outputAdapterExecution().uses(persistenceAccessorId);
    }

    /**
     * OutputPortOperationがDBアクセスするものかを判定する
     *
     * SpringDataJDBCを直接Serviceで使用している場合などにRepositoryインタフェースとSQLステートメントが一致する。
     */
    private static boolean outputPortOperationUseSQL(OutputImplementation outputImplementation, PersistenceAccessorId persistenceAccessorId) {
        var operationMethodId = outputImplementation.outputPortOperaionAsJigMethod().jigMethodId();
        // namespaceはメソッドの型のFQNに該当し、idはメソッド名に該当するので、それを比較する。
        return persistenceAccessorId.matches(TypeId.valueOf(operationMethodId.namespace()), operationMethodId.name());
    }
}
