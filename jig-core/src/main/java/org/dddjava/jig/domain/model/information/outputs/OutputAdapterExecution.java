package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.persistence.*;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.outputs.springdata.SpringDataUtil;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 出力アダプタの実装
 */
public record OutputAdapterExecution(
        JigMethod jigMethod,
        Collection<OutputPortOperation> implementOperations,
        Collection<JigMethod> tracingJigMethods,
        Collection<PersistenceAccessorOperation> persistenceAccessorOperations
) {
    private static final Logger logger = LoggerFactory.getLogger(OutputAdapterExecution.class);

    public static OutputAdapterExecution from(JigMethod jigMethod,
                                              Collection<OutputPortOperation> outputPortOperations,
                                              JigTypes jigTypes,
                                              ExternalAccessorRepositories accessorRepositories) {
        Set<JigMethod> tracingJigMethods = collectTracingJigMethods(jigMethod, jigTypes, new LinkedHashSet<>());
        Collection<PersistenceAccessorOperation> persistenceAccessorOperations = collectPersistenceAccessorOperation(tracingJigMethods, accessorRepositories.persistenceAccessorRepository());
        return new OutputAdapterExecution(jigMethod, outputPortOperations, tracingJigMethods, persistenceAccessorOperations);
    }

    public boolean uses(PersistenceAccessorOperationId persistenceAccessorOperationId) {
        return persistenceAccessorOperations.stream()
                .anyMatch(persistenceAccessor -> persistenceAccessor.persistenceAccessorOperationId().equals(persistenceAccessorOperationId));
    }

    /**
     * 呼び出している永続化アクセサ操作を収集する
     */
    private static Collection<PersistenceAccessorOperation> collectPersistenceAccessorOperation(Collection<JigMethod> tracingJigMethods,
                                                                                                PersistenceAccessorRepository persistenceAccessorRepository) {
        return tracingJigMethods.stream()
                .flatMap(jigMethod -> jigMethod.usingMethods().invokedMethodStream()
                        .map(methodCall -> findPersistenceAccessor(methodCall, jigMethod, persistenceAccessorRepository))
                        .flatMap(Optional::stream))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private static Optional<PersistenceAccessorOperation> findPersistenceAccessor(MethodCall methodCall,
                                                                                  JigMethod jigMethod,
                                                                                  PersistenceAccessorRepository persistenceAccessorRepository) {
        return persistenceAccessorRepository.findByTypeId(methodCall.methodOwner(), jigMethod.usingTypes().values())
                .flatMap(persistenceAccessor -> findPersistenceAccessor(methodCall, jigMethod, persistenceAccessor));
    }

    private static Optional<PersistenceAccessorOperation> findPersistenceAccessor(MethodCall methodCall,
                                                                                  JigMethod tracingJigMethod,
                                                                                  PersistenceAccessor persistenceAccessor) {
        PersistenceAccessorOperationId persistenceAccessorOperationId =
                PersistenceAccessorOperationId.fromTypeIdAndName(persistenceAccessor.typeId(), methodCall.methodName());
        return persistenceAccessor.findPersistenceAccessorById(persistenceAccessorOperationId)
                .or(() -> {
                    // SpringDataJDBCの場合はメソッド名から推測して作る
                    // ここにくるのはPersistenceAccessorに事前登録できてないケース、典型的には内部の共通インタフェースで定義されているメソッドで、JIGの読み取り対象外のもの。
                    // ひとまず動的に生成しているが、非効率ではあるのでPersistenceAccessorRepositoryに登録しておきたいところ。
                    if (persistenceAccessor.technology() == PersistenceAccessorTechnology.SPRING_DATA_JDBC) {
                        var persistenceOperationType = SpringDataUtil.inferSqlType(methodCall.methodName());
                        if (persistenceOperationType.isPresent()) {
                            return persistenceOperationType
                                    .map(sqlType -> PersistenceAccessorOperation.from(persistenceAccessorOperationId, sqlType, persistenceAccessor.defaultPersistenceTargets()));
                        }
                    }

                    // TODO 「不明」とかで載せたほうがよいかもしれない。
                    logger.warn("PersistenceAccessorはに対する操作が解決できませんでした。この関連はドキュメントから除外されます。caller={} callee={}",
                            tracingJigMethod.fqn(),
                            methodCall.jigMethodId().value());
                    return Optional.empty();
                });
    }

    /**
     * 関連メソッド収集
     * 起点のメソッドからメソッド呼び出しを辿って解決できる範囲のJigMethodを収集する。起点のメソッドも含む。
     */
    private static Set<JigMethod> collectTracingJigMethods(JigMethod jigMethod, JigTypes jigTypes, Set<JigMethodId> stopper) {
        // stopperにあるものは収集済みなのでスキップ（空を返す）
        if (!stopper.add(jigMethod.jigMethodId())) {
            return Set.of();
        }
        Set<JigMethod> resolved = jigMethod.usingMethods().invokedMethodStream()
                // MethodCallからJigMethodを引く
                // MEMO: メソッドIDの一致でみているのでinterfaceのMethodCallは実装をとれない
                .flatMap(methodCall -> jigTypes.resolveJigMethod(methodCall.jigMethodId()).stream())
                .flatMap(calledJigMethod -> collectTracingJigMethods(calledJigMethod, jigTypes, stopper).stream())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        resolved.add(jigMethod);
        return resolved;
    }

}
