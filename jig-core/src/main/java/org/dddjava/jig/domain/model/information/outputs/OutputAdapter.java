package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperation;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationsRepository;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 出力アダプタとなるクラス
 */
public record OutputAdapter(JigType jigType, Collection<OutputAdapterExecution> outputAdapterExecutions) {

    public static OutputAdapter from(JigType jigType, JigTypes jigTypes, PersistenceOperationsRepository persistenceOperationsRepository) {
        var outputAdapterExecutions = jigType.instanceJigMethodStream()
                .map(jigMethod -> new OutputAdapterExecution(
                        jigMethod,
                        findPersistenceOperations(jigMethod, jigTypes, persistenceOperationsRepository, new LinkedHashSet<>())))
                .toList();
        return new OutputAdapter(jigType, outputAdapterExecutions);
    }

    public Stream<OutputPort> implementsPortStream(JigTypes contextJigTypes) {
        return jigType().jigTypeHeader().interfaceTypeList()
                .stream()
                .flatMap(jigTypeReference -> contextJigTypes.resolveJigType(jigTypeReference.id()).stream())
                .map(OutputPort::new);
    }

    /**
     * 操作に対する実行を取り出す
     *
     * ポートはアダプタに依存しないので起点がポートだと探すことになるが、これが必要な理由はいまいちわからない。
     */
    public Optional<OutputAdapterExecution> findExecution(OutputPortOperation outputPortOperation) {
        return outputAdapterExecutions.stream()
                .filter(outputAdapterExecution -> outputPortOperation.matches(outputAdapterExecution.jigMethod()))
                .findAny();
    }

    private static Collection<PersistenceOperation> findPersistenceOperations(
            JigMethod jigMethod,
            JigTypes jigTypes,
            PersistenceOperationsRepository persistenceOperationsRepository,
            Set<JigMethodId> visitingMethodIds
    ) {
        if (!visitingMethodIds.add(jigMethod.jigMethodId())) {
            return Set.of();
        }
        try {
            Set<PersistenceOperation> persistenceOperations = jigMethod.usingMethods().invokedMethodStream()
                    .flatMap(methodCall -> Stream.concat(
                            persistenceOperationsRepository.findById(toPersistenceOperationId(methodCall)).stream(),
                            jigTypes.resolveJigMethod(methodCall.jigMethodId())
                                    .stream()
                                    .flatMap(calledMethod -> findPersistenceOperations(
                                            calledMethod,
                                            jigTypes,
                                            persistenceOperationsRepository,
                                            visitingMethodIds).stream())))
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
            return persistenceOperations;
        } finally {
            visitingMethodIds.remove(jigMethod.jigMethodId());
        }
    }

    private static PersistenceOperationId toPersistenceOperationId(MethodCall methodCall) {
        return PersistenceOperationId.fromTypeIdAndName(methodCall.methodOwner(), methodCall.methodName());
    }
}
