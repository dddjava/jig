package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperation;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationsRepository;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 出力アダプタの実装
 */
public record OutputAdapterExecution(JigMethod jigMethod, Collection<JigMethod> tracingJigMethods) {

    public static OutputAdapterExecution from(JigMethod jigMethod, JigTypes jigTypes) {
        Set<JigMethod> tracingJigMethods = collectTracingJigMethods(jigMethod, jigTypes, new LinkedHashSet<>());
        return new OutputAdapterExecution(jigMethod, tracingJigMethods);
    }

    public boolean uses(PersistenceOperationId persistenceOperationId, PersistenceOperationsRepository persistenceOperationsRepository) {
        return resolvePersistenceOperations(persistenceOperationsRepository).stream()
                .anyMatch(persistenceOperation -> persistenceOperation.persistenceOperationId().equals(persistenceOperationId));
    }

    public Collection<PersistenceOperation> resolvePersistenceOperations(PersistenceOperationsRepository persistenceOperationsRepository) {
        return tracingJigMethods.stream()
                .flatMap(tracingJigMethod -> tracingJigMethod.usingMethods().invokedMethodStream()
                        .map(OutputAdapterExecution::toPersistenceOperationId)
                        .map(persistenceOperationsRepository::findById)
                        .flatMap(Optional::stream))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
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
