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
        Collection<PersistenceAccessor> persistenceAccessors
) {
    private static final Logger logger = LoggerFactory.getLogger(OutputAdapterExecution.class);

    public static OutputAdapterExecution from(JigMethod jigMethod,
                                              JigTypes jigTypes,
                                              PersistenceAccessorsRepository persistenceAccessorsRepository) {
        Set<JigMethod> tracingJigMethods = collectTracingJigMethods(jigMethod, jigTypes, new LinkedHashSet<>());
        var persistenceAccessors = resolvePersistenceAccessors(tracingJigMethods, jigTypes, persistenceAccessorsRepository);
        return new OutputAdapterExecution(jigMethod, tracingJigMethods, persistenceAccessors);
    }

    public boolean uses(PersistenceAccessorId persistenceAccessorId) {
        return persistenceAccessors.stream()
                .anyMatch(persistenceAccessor -> persistenceAccessor.persistenceAccessorId().equals(persistenceAccessorId));
    }

    private static Collection<PersistenceAccessor> resolvePersistenceAccessors(Collection<JigMethod> tracingJigMethods,
                                                                               JigTypes jigTypes,
                                                                               PersistenceAccessorsRepository persistenceAccessorsRepository) {
        return tracingJigMethods.stream()
                .flatMap(tracingJigMethod -> tracingJigMethod.usingMethods().invokedMethodStream()
                        .map(methodCall -> findPersistenceAccessor(methodCall, tracingJigMethod, jigTypes, persistenceAccessorsRepository))
                        .flatMap(Optional::stream))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private static Optional<PersistenceAccessor> findPersistenceAccessor(MethodCall methodCall,
                                                                         JigMethod tracingJigMethod,
                                                                         JigTypes jigTypes,
                                                                         PersistenceAccessorsRepository persistenceAccessorsRepository) {
        return persistenceAccessorsRepository.findByTypeId(methodCall.methodOwner())
                .or(() -> resolveSpringDataPersistenceAccessors(methodCall, tracingJigMethod, jigTypes, persistenceAccessorsRepository))
                .flatMap(persistenceAccessors -> findPersistenceAccessor(
                        methodCall,
                        tracingJigMethod,
                        persistenceAccessors));
    }

    private static Optional<PersistenceAccessors> resolveSpringDataPersistenceAccessors(MethodCall methodCall,
                                                                                        JigMethod tracingJigMethod,
                                                                                        JigTypes jigTypes,
                                                                                        PersistenceAccessorsRepository persistenceAccessorsRepository) {
        if (!SpringDataUtil.isSpringDataRepositoryType(methodCall.methodOwner())) {
            return Optional.empty();
        }
        List<PersistenceAccessors> knownTypeCandidates = springDataCandidatesFromKnownTypes(tracingJigMethod, jigTypes, persistenceAccessorsRepository);
        Optional<PersistenceAccessors> selected = selectSpringDataCandidate(knownTypeCandidates, methodCall);
        if (selected.isPresent()) {
            return selected;
        }
        return selectSpringDataCandidate(springDataCandidates(persistenceAccessorsRepository), methodCall);
    }

    private static List<PersistenceAccessors> springDataCandidatesFromKnownTypes(JigMethod tracingJigMethod,
                                                                                 JigTypes jigTypes,
                                                                                 PersistenceAccessorsRepository persistenceAccessorsRepository) {
        Set<TypeId> candidateTypeIds = Stream.concat(
                        tracingJigMethod.usingTypes().values().stream(),
                        jigTypes.resolveJigType(tracingJigMethod.declaringType()).stream()
                                .flatMap(jigType -> jigType.instanceJigFields().fields().stream())
                                .map(jigField -> jigField.typeId()))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        return persistenceAccessorsRepository.values().stream()
                .filter(ops -> ops.technology() == PersistenceAccessorTechnology.SPRING_DATA_JDBC)
                .filter(ops -> candidateTypeIds.contains(ops.typeId()))
                .toList();
    }

    private static List<PersistenceAccessors> springDataCandidates(PersistenceAccessorsRepository persistenceAccessorsRepository) {
        return persistenceAccessorsRepository.values().stream()
                .filter(ops -> ops.technology() == PersistenceAccessorTechnology.SPRING_DATA_JDBC)
                .toList();
    }

    private static Optional<PersistenceAccessors> selectSpringDataCandidate(List<PersistenceAccessors> candidates,
                                                                            MethodCall methodCall) {
        if (candidates.isEmpty()) return Optional.empty();
        if (candidates.size() == 1) return Optional.of(candidates.getFirst());

        List<PersistenceAccessors> nameMatched = candidates.stream()
                .filter(persistenceAccessors -> persistenceAccessors.persistenceAccessors().stream()
                        .anyMatch(operation -> operation.persistenceAccessorId().id().equals(methodCall.methodName())))
                .toList();
        if (nameMatched.size() == 1) {
            return Optional.of(nameMatched.getFirst());
        }
        return Optional.empty();
    }

    private static Optional<PersistenceAccessor> findPersistenceAccessor(MethodCall methodCall,
                                                                         JigMethod tracingJigMethod,
                                                                         PersistenceAccessors persistenceAccessors) {
        PersistenceAccessorId persistenceAccessorId = SpringDataUtil.toPersistenceAccessorId(methodCall);
        return persistenceAccessors.findPersistenceAccessorById(persistenceAccessorId)
                .or(() -> SpringDataUtil.generateCalledPersistenceAccessor(methodCall, persistenceAccessors))
                .or(() -> {
                    logger.warn("PersistenceAccessorsは見つかりましたが、PersistenceAccessorが見つかりませんでした。caller={} callee={} owner={} origin={}",
                            tracingJigMethod.fqn(),
                            methodCall.jigMethodId().value(),
                            methodCall.methodOwner().fqn(),
                            persistenceAccessors.technology());
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
