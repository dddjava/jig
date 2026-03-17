package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.persistence.*;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.outputs.springdata.SpringDataUtil;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

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
                                              PersistenceAccessorRepository persistenceAccessorRepository) {
        Set<JigMethod> tracingJigMethods = collectTracingJigMethods(jigMethod, jigTypes, new LinkedHashSet<>());
        Collection<PersistenceAccessorOperation> persistenceAccessors = collectPersistenceAccessorOperation(tracingJigMethods, jigTypes, persistenceAccessorRepository);
        return new OutputAdapterExecution(jigMethod, outputPortOperations, tracingJigMethods, persistenceAccessors);
    }

    public boolean uses(PersistenceAccessorOperationId persistenceAccessorOperationId) {
        return persistenceAccessorOperations.stream()
                .anyMatch(persistenceAccessor -> persistenceAccessor.persistenceAccessorOperationId().equals(persistenceAccessorOperationId));
    }

    /**
     * 呼び出している永続化アクセサ操作を収集する
     */
    private static Collection<PersistenceAccessorOperation> collectPersistenceAccessorOperation(Collection<JigMethod> tracingJigMethods,
                                                                                                JigTypes jigTypes,
                                                                                                PersistenceAccessorRepository persistenceAccessorRepository) {
        return tracingJigMethods.stream()
                .flatMap(jigMethod -> jigMethod.usingMethods().invokedMethodStream()
                        .map(methodCall -> findPersistenceAccessor(methodCall, jigMethod, jigTypes, persistenceAccessorRepository))
                        .flatMap(Optional::stream))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private static Optional<PersistenceAccessorOperation> findPersistenceAccessor(MethodCall methodCall,
                                                                                  JigMethod jigMethod,
                                                                                  JigTypes jigTypes,
                                                                                  PersistenceAccessorRepository persistenceAccessorRepository) {
        return persistenceAccessorRepository.findByTypeId(methodCall.methodOwner())
                .or(() -> resolveSpringDataPersistenceAccessors(methodCall, jigMethod, jigTypes, persistenceAccessorRepository))
                .flatMap(persistenceAccessors -> findPersistenceAccessor(
                        methodCall,
                        jigMethod,
                        persistenceAccessors));
    }

    private static Optional<PersistenceAccessor> resolveSpringDataPersistenceAccessors(MethodCall methodCall,
                                                                                       JigMethod jigMethod,
                                                                                       JigTypes jigTypes,
                                                                                       PersistenceAccessorRepository persistenceAccessorRepository) {
        if (!SpringDataUtil.isSpringDataRepositoryType(methodCall.methodOwner())) {
            return Optional.empty();
        }
        List<PersistenceAccessor> knownTypeCandidates = springDataCandidatesFromKnownTypes(jigMethod, jigTypes, persistenceAccessorRepository);
        Optional<PersistenceAccessor> selected = selectSpringDataCandidate(knownTypeCandidates, methodCall);
        if (selected.isPresent()) {
            return selected;
        }
        return selectSpringDataCandidate(springDataCandidates(persistenceAccessorRepository), methodCall);
    }

    private static List<PersistenceAccessor> springDataCandidatesFromKnownTypes(JigMethod jigMethod,
                                                                                JigTypes jigTypes,
                                                                                PersistenceAccessorRepository persistenceAccessorRepository) {
        // 関連している型をひっぱりだして永続化アクセサの候補とする
        // FIXME: これだとメソッドが使用していないフィールドの型も入ってしまうが、あとでメソッドつきあわせするから問題ない・・・？
        Set<TypeId> candidateTypeIds = Stream.concat(
                        // メソッドが使用している型すべて
                        jigMethod.usingTypes().values().stream(),
                        // メソッドが所属している型のインスタンスフィールドの型すべて
                        jigTypes.resolveJigType(jigMethod.declaringType()).stream()
                                .flatMap(jigType -> jigType.instanceJigFields().fields().stream())
                                .map(jigField -> jigField.typeId()))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        return persistenceAccessorRepository.values().stream()
                .filter(ops -> ops.technology() == PersistenceAccessorTechnology.SPRING_DATA_JDBC)
                .filter(ops -> candidateTypeIds.contains(ops.typeId()))
                .toList();
    }

    private static List<PersistenceAccessor> springDataCandidates(PersistenceAccessorRepository persistenceAccessorRepository) {
        return persistenceAccessorRepository.values().stream()
                .filter(ops -> ops.technology() == PersistenceAccessorTechnology.SPRING_DATA_JDBC)
                .toList();
    }

    private static Optional<PersistenceAccessor> selectSpringDataCandidate(List<PersistenceAccessor> candidates,
                                                                           MethodCall methodCall) {
        if (candidates.isEmpty()) return Optional.empty();
        if (candidates.size() == 1) return Optional.of(candidates.getFirst());

        List<PersistenceAccessor> nameMatched = candidates.stream()
                .filter(persistenceAccessors -> persistenceAccessors.persistenceAccessorOperations().stream()
                        .anyMatch(operation -> operation.persistenceAccessorOperationId().id().equals(methodCall.methodName())))
                .toList();
        if (nameMatched.size() == 1) {
            return Optional.of(nameMatched.getFirst());
        }
        return Optional.empty();
    }

    private static Optional<PersistenceAccessorOperation> findPersistenceAccessor(MethodCall methodCall,
                                                                                  JigMethod tracingJigMethod,
                                                                                  PersistenceAccessor persistenceAccessor) {
        PersistenceAccessorOperationId persistenceAccessorOperationId = SpringDataUtil.toPersistenceAccessorId(methodCall);
        return persistenceAccessor.findPersistenceAccessorById(persistenceAccessorOperationId)
                .or(() -> SpringDataUtil.generateCalledPersistenceAccessor(methodCall, persistenceAccessor))
                .or(() -> {
                    logger.warn("PersistenceAccessorsは見つかりましたが、PersistenceAccessorが見つかりませんでした。caller={} callee={} owner={} origin={}",
                            tracingJigMethod.fqn(),
                            methodCall.jigMethodId().value(),
                            methodCall.methodOwner().fqn(),
                            persistenceAccessor.technology());
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
