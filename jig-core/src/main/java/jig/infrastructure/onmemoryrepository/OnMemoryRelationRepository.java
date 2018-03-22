package jig.infrastructure.onmemoryrepository;

import jig.domain.model.identifier.Identifier;
import jig.domain.model.identifier.Identifiers;
import jig.domain.model.identifier.MethodIdentifier;
import jig.domain.model.identifier.MethodIdentifiers;
import jig.domain.model.relation.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Repository
public class OnMemoryRelationRepository implements RelationRepository {

    final EnumMap<RelationType, Set<Relation>> map;

    final Set<TypeRelation> memberTypes = new HashSet<>();
    final Set<TypeMethodRelation> memberMethods = new HashSet<>();
    final Set<MethodTypeRelation> methodReturnTypes = new HashSet<>();
    final Set<MethodTypeRelation> methodParameterTypes = new HashSet<>();
    final Set<MethodTypeRelation> methodUseTypes = new HashSet<>();
    final Set<MethodRelation> methodImplementMethods = new HashSet<>();
    final Set<MethodRelation> methodUseMethods = new HashSet<>();

    public OnMemoryRelationRepository() {
        map = new EnumMap<>(RelationType.class);
        for (RelationType relationType : RelationType.values()) {
            map.put(relationType, new HashSet<>());
        }
    }

    private void register(Relation relation) {
        map.get(relation.relationType()).add(relation);
    }


    @Override
    public void registerMethod(Identifier classIdentifier, MethodIdentifier methodIdentifier) {
        memberMethods.add(new TypeMethodRelation(classIdentifier, methodIdentifier));
    }

    @Override
    public void registerMethodParameter(MethodIdentifier methodIdentifier, Identifier argumentTypeIdentifier) {
        methodParameterTypes.add(new MethodTypeRelation(methodIdentifier, argumentTypeIdentifier));
    }

    @Override
    public void registerMethodReturnType(MethodIdentifier methodIdentifier, Identifier returnTypeIdentifier) {
        methodReturnTypes.add(new MethodTypeRelation(methodIdentifier, returnTypeIdentifier));
    }

    @Override
    public void registerMethodUseMethod(MethodIdentifier from, MethodIdentifier to) {
        methodUseMethods.add(new MethodRelation(from, to));
    }

    @Override
    public void registerMethodUseType(MethodIdentifier methodIdentifier, Identifier identifier) {
        methodUseTypes.add(new MethodTypeRelation(methodIdentifier, identifier));

    }

    @Override
    public void registerImplementation(MethodIdentifier from, MethodIdentifier to) {
        methodImplementMethods.add(new MethodRelation(from, to));
    }

    @Override
    public void registerImplementation(Identifier identifier, Identifier interfaceIdentifier) {
        register(RelationType.IMPLEMENT.of(identifier, interfaceIdentifier));
    }

    @Override
    public void registerField(Identifier identifier, Identifier fieldClassIdentifier) {
        memberTypes.add(new TypeRelation(identifier, fieldClassIdentifier));
    }

    @Override
    public Identifier getReturnTypeOf(MethodIdentifier methodIdentifier) {
        return methodReturnTypes.stream()
                .filter(methodTypeRelation -> methodTypeRelation.methodIs(methodIdentifier))
                .map(MethodTypeRelation::type)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(methodIdentifier.asFullText()));
    }

    @Override
    public Identifiers findUseTypeOf(MethodIdentifier methodIdentifier) {
        return methodUseTypes.stream()
                .filter(methodTypeRelation -> methodTypeRelation.methodIs(methodIdentifier))
                .map(MethodTypeRelation::type)
                .collect(Identifiers.collector());
    }

    @Override
    public MethodIdentifiers findConcrete(MethodIdentifier methodIdentifier) {
        return methodImplementMethods.stream()
                .filter(methodRelation -> methodRelation.interfaceMethodIs(methodIdentifier))
                .map(MethodRelation::concreteMethod)
                .collect(MethodIdentifiers.collector());
    }

    @Override
    public MethodIdentifiers findUseMethod(MethodIdentifier methodIdentifier) {
        return methodUseMethods.stream()
                .filter(methodRelation -> methodRelation.fromMethodIs(methodIdentifier))
                .map(MethodRelation::to)
                .collect(MethodIdentifiers.collector());
    }

    @Override
    public MethodIdentifiers methodsOf(Identifier identifier) {
        return memberMethods.stream()
                .filter(typeMethodRelation -> typeMethodRelation.typeIs(identifier))
                .map(TypeMethodRelation::method)
                .collect(MethodIdentifiers.collector());
    }

    @Override
    public Identifiers findFieldUsage(Identifier identifier) {
        return memberTypes.stream()
                .filter(typeRelation -> typeRelation.isTo(identifier))
                .map(TypeRelation::from)
                .collect(Identifiers.collector());
    }

    @Override
    public MethodIdentifiers findMethodUsage(Identifier identifier) {
        return Stream.of(methodReturnTypes, methodParameterTypes, methodUseTypes).flatMap(Set::stream)
                .filter(methodTypeRelation -> methodTypeRelation.typeIs(identifier))
                .map(MethodTypeRelation::method)
                .collect(MethodIdentifiers.collector());
    }

    @Override
    public void registerDependency(Identifier from, Identifier to) {
        register(RelationType.DEPENDENCY.of(from, to));
    }

    @Override
    public Relations all() {
        return new Relations(map.values().stream().flatMap(Set::stream).collect(toList()));
    }

    @Override
    public Relations findTo(Identifier toIdentifier, RelationType type) {
        List<Relation> relations = map.get(type).stream()
                .filter(relation -> toIdentifier.equals(relation.to()))
                .collect(toList());
        return new Relations(relations);
    }
}
