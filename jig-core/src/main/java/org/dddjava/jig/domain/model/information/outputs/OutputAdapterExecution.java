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
        Collection<JigMethod> tracingJigMethods,
        Collection<PersistenceOperation> persistenceOperations
) {
    private static final Logger logger = LoggerFactory.getLogger(OutputAdapterExecution.class);

    public static OutputAdapterExecution from(JigMethod jigMethod,
                                              JigTypes jigTypes,
                                              PersistenceOperationsRepository persistenceOperationsRepository) {
        Set<JigMethod> tracingJigMethods = collectTracingJigMethods(jigMethod, jigTypes, new LinkedHashSet<>());
        var persistenceOperations = resolvePersistenceOperations(tracingJigMethods, jigTypes, persistenceOperationsRepository);
        return new OutputAdapterExecution(jigMethod, tracingJigMethods, persistenceOperations);
    }

    public boolean uses(PersistenceOperationId persistenceOperationId) {
        return persistenceOperations.stream()
                .anyMatch(persistenceOperation -> persistenceOperation.persistenceOperationId().equals(persistenceOperationId));
    }

    private static Collection<PersistenceOperation> resolvePersistenceOperations(Collection<JigMethod> tracingJigMethods,
                                                                                 JigTypes jigTypes,
                                                                                 PersistenceOperationsRepository persistenceOperationsRepository) {
        return tracingJigMethods.stream()
                .flatMap(tracingJigMethod -> tracingJigMethod.usingMethods().invokedMethodStream()
                        .map(methodCall -> findPersistenceOperation(methodCall, tracingJigMethod, jigTypes, persistenceOperationsRepository))
                        .flatMap(Optional::stream))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private static Optional<PersistenceOperation> findPersistenceOperation(MethodCall methodCall,
                                                                           JigMethod tracingJigMethod,
                                                                           JigTypes jigTypes,
                                                                           PersistenceOperationsRepository persistenceOperationsRepository) {
        return persistenceOperationsRepository.findByTypeId(methodCall.methodOwner())
                .or(() -> resolveSpringDataPersistenceOperations(methodCall, tracingJigMethod, jigTypes, persistenceOperationsRepository))
                .flatMap(persistenceOperations -> findPersistenceOperation(
                        methodCall,
                        tracingJigMethod,
                        persistenceOperations));
    }

    private static Optional<PersistenceOperations> resolveSpringDataPersistenceOperations(MethodCall methodCall,
                                                                                          JigMethod tracingJigMethod,
                                                                                          JigTypes jigTypes,
                                                                                          PersistenceOperationsRepository persistenceOperationsRepository) {
        if (!SpringDataUtil.isSpringDataRepositoryType(methodCall.methodOwner())) {
            return Optional.empty();
        }
        List<PersistenceOperations> knownTypeCandidates = springDataCandidatesFromKnownTypes(tracingJigMethod, jigTypes, persistenceOperationsRepository);
        Optional<PersistenceOperations> selected = selectSpringDataCandidate(knownTypeCandidates, methodCall);
        if (selected.isPresent()) {
            return selected;
        }
        return selectSpringDataCandidate(springDataCandidates(persistenceOperationsRepository), methodCall);
    }

    private static List<PersistenceOperations> springDataCandidatesFromKnownTypes(JigMethod tracingJigMethod,
                                                                                  JigTypes jigTypes,
                                                                                  PersistenceOperationsRepository persistenceOperationsRepository) {
        Set<TypeId> candidateTypeIds = Stream.concat(
                        tracingJigMethod.usingTypes().values().stream(),
                        jigTypes.resolveJigType(tracingJigMethod.declaringType()).stream()
                                .flatMap(jigType -> jigType.instanceJigFields().fields().stream())
                                .map(jigField -> jigField.typeId()))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        return persistenceOperationsRepository.values().stream()
                .filter(ops -> ops.origin() == PersistenceOperationsOrigin.SPRING_DATA_JDBC)
                .filter(ops -> candidateTypeIds.contains(ops.typeId()))
                .toList();
    }

    private static List<PersistenceOperations> springDataCandidates(PersistenceOperationsRepository persistenceOperationsRepository) {
        return persistenceOperationsRepository.values().stream()
                .filter(ops -> ops.origin() == PersistenceOperationsOrigin.SPRING_DATA_JDBC)
                .toList();
    }

    private static Optional<PersistenceOperations> selectSpringDataCandidate(List<PersistenceOperations> candidates,
                                                                             MethodCall methodCall) {
        if (candidates.isEmpty()) return Optional.empty();
        if (candidates.size() == 1) return Optional.of(candidates.getFirst());

        List<PersistenceOperations> nameMatched = candidates.stream()
                .filter(persistenceOperations -> persistenceOperations.persistenceOperations().stream()
                        .anyMatch(operation -> operation.persistenceOperationId().id().equals(methodCall.methodName())))
                .toList();
        if (nameMatched.size() == 1) {
            return Optional.of(nameMatched.getFirst());
        }
        return Optional.empty();
    }

    private static Optional<PersistenceOperation> findPersistenceOperation(MethodCall methodCall,
                                                                           JigMethod tracingJigMethod,
                                                                           PersistenceOperations persistenceOperations) {
        PersistenceOperationId persistenceOperationId = SpringDataUtil.toPersistenceOperationId(methodCall);
        return persistenceOperations.findPersistenceOperationById(persistenceOperationId)
                .or(() -> SpringDataUtil.generateCalledPersistenceOperation(methodCall, persistenceOperations))
                .or(() -> {
                    logger.warn("PersistenceOperationsは見つかりましたが、PersistenceOperationが見つかりませんでした。caller={} callee={} owner={} origin={}",
                            tracingJigMethod.fqn(),
                            methodCall.jigMethodId().value(),
                            methodCall.methodOwner().fqn(),
                            persistenceOperations.origin());
                    return Optional.empty();
                });
    }

    private static Set<JigMethod> collectTracingJigMethods(JigMethod jigMethod, JigTypes jigTypes, Set<JigMethodId> tracingMethodIds) {
        if (!tracingMethodIds.add(jigMethod.jigMethodId())) {
            return Set.of();
        }
        try {
            Set<JigMethod> resolved = jigMethod.usingMethods().invokedMethodStream()
                    .flatMap(methodCall -> jigTypes.resolveJigMethod(methodCall.jigMethodId()).stream())
                    .flatMap(calledJigMethod -> collectTracingJigMethods(calledJigMethod, jigTypes, tracingMethodIds).stream())
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
            resolved.add(jigMethod);
            return resolved;
        } finally {
            tracingMethodIds.remove(jigMethod.jigMethodId());
        }
    }

}
