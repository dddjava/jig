package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperation;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperations;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationsRepository;
import org.dddjava.jig.domain.model.information.members.JigMethod;
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
        Collection<JigMethod> tracingJigMethods,
        Collection<PersistenceOperation> persistenceOperations
) {
    private static final Logger logger = LoggerFactory.getLogger(OutputAdapterExecution.class);

    public static OutputAdapterExecution from(JigMethod jigMethod,
                                              JigTypes jigTypes,
                                              PersistenceOperationsRepository persistenceOperationsRepository) {
        Set<JigMethod> tracingJigMethods = collectTracingJigMethods(jigMethod, jigTypes, new LinkedHashSet<>());
        var persistenceOperations = resolvePersistenceOperations(tracingJigMethods, persistenceOperationsRepository);
        return new OutputAdapterExecution(jigMethod, tracingJigMethods, persistenceOperations);
    }

    public boolean uses(PersistenceOperationId persistenceOperationId) {
        return persistenceOperations.stream()
                .anyMatch(persistenceOperation -> persistenceOperation.persistenceOperationId().equals(persistenceOperationId));
    }

    private static Collection<PersistenceOperation> resolvePersistenceOperations(Collection<JigMethod> tracingJigMethods,
                                                                                 PersistenceOperationsRepository persistenceOperationsRepository) {
        return tracingJigMethods.stream()
                .flatMap(tracingJigMethod -> tracingJigMethod.usingMethods().invokedMethodStream()
                        .map(methodCall -> findPersistenceOperation(methodCall, tracingJigMethod, persistenceOperationsRepository))
                        .flatMap(Optional::stream))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private static Optional<PersistenceOperation> findPersistenceOperation(MethodCall methodCall,
                                                                           JigMethod tracingJigMethod,
                                                                           PersistenceOperationsRepository persistenceOperationsRepository) {
        return persistenceOperationsRepository.findByTypeId(methodCall.methodOwner())
                .flatMap(persistenceOperations -> findPersistenceOperation(
                        methodCall,
                        tracingJigMethod,
                        persistenceOperations));
    }

    private static Optional<PersistenceOperation> findPersistenceOperation(MethodCall methodCall,
                                                                           JigMethod tracingJigMethod,
                                                                           PersistenceOperations persistenceOperations) {
        PersistenceOperationId persistenceOperationId = toPersistenceOperationId(methodCall);
        Optional<PersistenceOperation> persistenceOperation = persistenceOperations.persistenceOperations().stream()
                .filter(operation -> operation.persistenceOperationId().equals(persistenceOperationId))
                .findAny();
        if (persistenceOperation.isEmpty()) {
            logger.warn("PersistenceOperationsは見つかりましたが、PersistenceOperationが見つかりませんでした。caller={} callee={} owner={} origin={}",
                    tracingJigMethod.fqn(),
                    methodCall.jigMethodId().value(),
                    methodCall.methodOwner().fqn(),
                    persistenceOperations.origin());
        }
        return persistenceOperation;
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

    private static PersistenceOperationId toPersistenceOperationId(MethodCall methodCall) {
        return PersistenceOperationId.fromTypeIdAndName(methodCall.methodOwner(), methodCall.methodName());
    }
}
