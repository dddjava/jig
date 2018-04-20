package jig.infrastructure.onmemoryrepository;

import jig.domain.model.definition.field.FieldDefinition;
import jig.domain.model.definition.field.FieldDefinitions;
import jig.domain.model.definition.method.MethodDefinition;
import jig.domain.model.definition.method.MethodDefinitions;
import jig.domain.model.definition.method.MethodSignature;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;
import jig.domain.model.relation.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Stream;

@Repository
public class OnMemoryRelationRepository implements RelationRepository {

    final List<TypeRelation> memberTypes = new ArrayList<>();
    final List<TypeRelation> constants = new ArrayList<>();

    final Set<TypeMethodRelation> memberMethods = new HashSet<>();
    final Set<MethodTypeRelation> methodReturnTypes = new HashSet<>();
    final Set<MethodTypeRelation> methodParameterTypes = new HashSet<>();
    final Set<MethodTypeRelation> methodUseTypes = new HashSet<>();
    final Set<MethodRelation> methodImplementMethods = new HashSet<>();
    final Set<MethodRelation> methodUseMethods = new HashSet<>();

    @Override
    public void registerMethod(MethodDefinition methodDefinition) {
        memberMethods.add(new TypeMethodRelation(methodDefinition.declaringType(), methodDefinition));
    }

    @Override
    public void registerMethodParameter(MethodDefinition methodDefinition) {
        MethodSignature methodSignature = methodDefinition.methodSignature();
        methodSignature.arguments().forEach(argumentTypeIdentifier ->
                methodParameterTypes.add(new MethodTypeRelation(methodDefinition, argumentTypeIdentifier)));
    }

    @Override
    public void registerMethodReturnType(MethodDefinition methodDefinition, TypeIdentifier returnTypeIdentifier) {
        methodReturnTypes.add(new MethodTypeRelation(methodDefinition, returnTypeIdentifier));
    }

    @Override
    public void registerMethodUseMethod(MethodDefinition from, MethodDefinition to) {
        methodUseMethods.add(new MethodRelation(from, to));
    }

    @Override
    public void registerMethodUseType(MethodDefinition methodDefinition, TypeIdentifier typeIdentifier) {
        methodUseTypes.add(new MethodTypeRelation(methodDefinition, typeIdentifier));

    }

    @Override
    public void registerImplementation(MethodDefinition from, MethodDefinition to) {
        methodImplementMethods.add(new MethodRelation(from, to));
    }

    @Override
    public void registerField(FieldDefinition fieldDefinition) {
        memberTypes.add(new TypeRelation(fieldDefinition.declaringType(), fieldDefinition));
    }

    @Override
    public void registerConstants(FieldDefinition fieldDefinition) {
        constants.add(new TypeRelation(fieldDefinition.declaringType(), fieldDefinition));
    }

    @Override
    public void registerMethodUseField(MethodDefinition methodDefinition, FieldDefinition fieldDefinition) {
        // TODO とりあえず名前はわすれる
        registerMethodUseType(methodDefinition, fieldDefinition.typeIdentifier());
    }

    @Override
    public TypeIdentifier getReturnTypeOf(MethodDefinition methodDefinition) {
        return methodReturnTypes.stream()
                .filter(methodTypeRelation -> methodTypeRelation.methodIs(methodDefinition))
                .map(MethodTypeRelation::type)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(methodDefinition.asFullText()));
    }

    @Override
    public TypeIdentifiers findUseTypeOf(MethodDefinition methodDefinition) {
        return methodUseTypes.stream()
                .filter(methodTypeRelation -> methodTypeRelation.methodIs(methodDefinition))
                .map(MethodTypeRelation::type)
                .collect(TypeIdentifiers.collector());
    }

    @Override
    public MethodDefinitions findConcrete(MethodDefinition methodDefinition) {
        return methodImplementMethods.stream()
                .filter(methodRelation -> methodRelation.interfaceMethodIs(methodDefinition))
                .map(MethodRelation::concreteMethod)
                .collect(MethodDefinitions.collector());
    }

    @Override
    public MethodDefinitions findUseMethod(MethodDefinition methodDefinition) {
        return methodUseMethods.stream()
                .filter(methodRelation -> methodRelation.fromMethodIs(methodDefinition))
                .map(MethodRelation::to)
                .collect(MethodDefinitions.collector());
    }

    @Override
    public MethodDefinitions methodsOf(TypeIdentifier typeIdentifier) {
        return memberMethods.stream()
                .filter(typeMethodRelation -> typeMethodRelation.typeIs(typeIdentifier))
                .map(TypeMethodRelation::method)
                .collect(MethodDefinitions.collector());
    }

    @Override
    public TypeIdentifiers findFieldUsage(TypeIdentifier typeIdentifier) {
        return memberTypes.stream()
                .filter(typeRelation -> typeRelation.field().typeIdentifier().equals(typeIdentifier))
                .map(TypeRelation::from)
                .collect(TypeIdentifiers.collector());
    }

    @Override
    public MethodDefinitions findMethodUsage(TypeIdentifier typeIdentifier) {
        return Stream.of(methodReturnTypes, methodParameterTypes, methodUseTypes).flatMap(Set::stream)
                .filter(methodTypeRelation -> methodTypeRelation.typeIs(typeIdentifier))
                .map(MethodTypeRelation::method)
                .collect(MethodDefinitions.collector());
    }

    @Override
    public FieldDefinitions findConstants(TypeIdentifier type) {
        return constants.stream()
                .filter(typeRelation -> typeRelation.from().equals(type))
                .map(TypeRelation::field)
                .collect(FieldDefinitions.collector());
    }

    @Override
    public FieldDefinitions findFieldsOf(TypeIdentifier type) {
        return memberTypes.stream()
                .filter(typeRelation -> typeRelation.from().equals(type))
                .map(TypeRelation::field)
                .collect(FieldDefinitions.collector());
    }

    Map<TypeIdentifier, TypeIdentifiers> map = new HashMap<>();

    @Override
    public void registerDependency(TypeIdentifier typeIdentifier, TypeIdentifiers typeIdentifiers) {
        map.put(typeIdentifier, typeIdentifiers);
    }

    @Override
    public TypeIdentifiers findDependency(TypeIdentifier identifier) {
        return map.get(identifier);
    }
}
