package jig.infrastructure.onmemoryrepository;

import jig.domain.model.identifier.field.FieldIdentifier;
import jig.domain.model.identifier.field.FieldIdentifiers;
import jig.domain.model.identifier.method.MethodIdentifier;
import jig.domain.model.identifier.method.MethodIdentifiers;
import jig.domain.model.identifier.method.MethodSignature;
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
    public void registerMethod(MethodIdentifier methodIdentifier) {
        memberMethods.add(new TypeMethodRelation(methodIdentifier.declaringType(), methodIdentifier));
    }

    @Override
    public void registerMethodParameter(MethodIdentifier methodIdentifier) {
        MethodSignature methodSignature = methodIdentifier.methodSignature();
        methodSignature.arguments().forEach(argumentTypeIdentifier ->
                methodParameterTypes.add(new MethodTypeRelation(methodIdentifier, argumentTypeIdentifier)));
    }

    @Override
    public void registerMethodReturnType(MethodIdentifier methodIdentifier, TypeIdentifier returnTypeIdentifier) {
        methodReturnTypes.add(new MethodTypeRelation(methodIdentifier, returnTypeIdentifier));
    }

    @Override
    public void registerMethodUseMethod(MethodIdentifier from, MethodIdentifier to) {
        methodUseMethods.add(new MethodRelation(from, to));
    }

    @Override
    public void registerMethodUseType(MethodIdentifier methodIdentifier, TypeIdentifier typeIdentifier) {
        methodUseTypes.add(new MethodTypeRelation(methodIdentifier, typeIdentifier));

    }

    @Override
    public void registerImplementation(MethodIdentifier from, MethodIdentifier to) {
        methodImplementMethods.add(new MethodRelation(from, to));
    }

    @Override
    public void registerField(TypeIdentifier typeIdentifier, FieldIdentifier fieldIdentifier) {
        memberTypes.add(new TypeRelation(typeIdentifier, fieldIdentifier));
    }

    @Override
    public void registerConstants(TypeIdentifier typeIdentifier, FieldIdentifier fieldIdentifier) {
        constants.add(new TypeRelation(typeIdentifier, fieldIdentifier));
    }

    @Override
    public void registerMethodUseField(MethodIdentifier methodIdentifier, FieldIdentifier fieldIdentifier) {
        // TODO とりあえず名前はわすれる
        registerMethodUseType(methodIdentifier, fieldIdentifier.typeIdentifier());
    }

    @Override
    public TypeIdentifier getReturnTypeOf(MethodIdentifier methodIdentifier) {
        return methodReturnTypes.stream()
                .filter(methodTypeRelation -> methodTypeRelation.methodIs(methodIdentifier))
                .map(MethodTypeRelation::type)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(methodIdentifier.asFullText()));
    }

    @Override
    public TypeIdentifiers findUseTypeOf(MethodIdentifier methodIdentifier) {
        return methodUseTypes.stream()
                .filter(methodTypeRelation -> methodTypeRelation.methodIs(methodIdentifier))
                .map(MethodTypeRelation::type)
                .collect(TypeIdentifiers.collector());
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
    public MethodIdentifiers methodsOf(TypeIdentifier typeIdentifier) {
        return memberMethods.stream()
                .filter(typeMethodRelation -> typeMethodRelation.typeIs(typeIdentifier))
                .map(TypeMethodRelation::method)
                .collect(MethodIdentifiers.collector());
    }

    @Override
    public TypeIdentifiers findFieldUsage(TypeIdentifier typeIdentifier) {
        return memberTypes.stream()
                .filter(typeRelation -> typeRelation.field().typeIdentifier().equals(typeIdentifier))
                .map(TypeRelation::from)
                .collect(TypeIdentifiers.collector());
    }

    @Override
    public MethodIdentifiers findMethodUsage(TypeIdentifier typeIdentifier) {
        return Stream.of(methodReturnTypes, methodParameterTypes, methodUseTypes).flatMap(Set::stream)
                .filter(methodTypeRelation -> methodTypeRelation.typeIs(typeIdentifier))
                .map(MethodTypeRelation::method)
                .collect(MethodIdentifiers.collector());
    }

    @Override
    public TypeIdentifiers findAllUsage(TypeIdentifier typeIdentifier) {
        TypeIdentifiers methodUsages = findMethodUsage(typeIdentifier).declaringTypes();
        TypeIdentifiers fieldUsages = findFieldUsage(typeIdentifier);
        return methodUsages.merge(fieldUsages);
    }

    @Override
    public FieldIdentifiers findConstants(TypeIdentifier type) {
        return constants.stream()
                .filter(typeRelation -> typeRelation.from().equals(type))
                .map(TypeRelation::field)
                .collect(FieldIdentifiers.collector());
    }

    @Override
    public FieldIdentifiers findFieldsOf(TypeIdentifier type) {
        return memberTypes.stream()
                .filter(typeRelation -> typeRelation.from().equals(type))
                .map(TypeRelation::field)
                .collect(FieldIdentifiers.collector());
    }
}
