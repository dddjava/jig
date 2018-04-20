package jig.infrastructure.onmemoryrepository;

import jig.domain.model.declaration.field.FieldDeclaration;
import jig.domain.model.declaration.field.FieldDeclarations;
import jig.domain.model.declaration.method.MethodDeclaration;
import jig.domain.model.declaration.method.MethodDeclarations;
import jig.domain.model.declaration.method.MethodSignature;
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
    public void registerMethod(MethodDeclaration methodDeclaration) {
        memberMethods.add(new TypeMethodRelation(methodDeclaration.declaringType(), methodDeclaration));
    }

    @Override
    public void registerMethodParameter(MethodDeclaration methodDeclaration) {
        MethodSignature methodSignature = methodDeclaration.methodSignature();
        methodSignature.arguments().forEach(argumentTypeIdentifier ->
                methodParameterTypes.add(new MethodTypeRelation(methodDeclaration, argumentTypeIdentifier)));
    }

    @Override
    public void registerMethodReturnType(MethodDeclaration methodDeclaration, TypeIdentifier returnTypeIdentifier) {
        methodReturnTypes.add(new MethodTypeRelation(methodDeclaration, returnTypeIdentifier));
    }

    @Override
    public void registerMethodUseMethod(MethodDeclaration from, MethodDeclaration to) {
        methodUseMethods.add(new MethodRelation(from, to));
    }

    @Override
    public void registerMethodUseType(MethodDeclaration methodDeclaration, TypeIdentifier typeIdentifier) {
        methodUseTypes.add(new MethodTypeRelation(methodDeclaration, typeIdentifier));

    }

    @Override
    public void registerImplementation(MethodDeclaration from, MethodDeclaration to) {
        methodImplementMethods.add(new MethodRelation(from, to));
    }

    @Override
    public void registerField(FieldDeclaration fieldDeclaration) {
        memberTypes.add(new TypeRelation(fieldDeclaration.declaringType(), fieldDeclaration));
    }

    @Override
    public void registerConstants(FieldDeclaration fieldDeclaration) {
        constants.add(new TypeRelation(fieldDeclaration.declaringType(), fieldDeclaration));
    }

    @Override
    public void registerMethodUseField(MethodDeclaration methodDeclaration, FieldDeclaration fieldDeclaration) {
        // TODO とりあえず名前はわすれる
        registerMethodUseType(methodDeclaration, fieldDeclaration.typeIdentifier());
    }

    @Override
    public TypeIdentifier getReturnTypeOf(MethodDeclaration methodDeclaration) {
        return methodReturnTypes.stream()
                .filter(methodTypeRelation -> methodTypeRelation.methodIs(methodDeclaration))
                .map(MethodTypeRelation::type)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(methodDeclaration.asFullText()));
    }

    @Override
    public TypeIdentifiers findUseTypeOf(MethodDeclaration methodDeclaration) {
        return methodUseTypes.stream()
                .filter(methodTypeRelation -> methodTypeRelation.methodIs(methodDeclaration))
                .map(MethodTypeRelation::type)
                .collect(TypeIdentifiers.collector());
    }

    @Override
    public MethodDeclarations findConcrete(MethodDeclaration methodDeclaration) {
        return methodImplementMethods.stream()
                .filter(methodRelation -> methodRelation.interfaceMethodIs(methodDeclaration))
                .map(MethodRelation::concreteMethod)
                .collect(MethodDeclarations.collector());
    }

    @Override
    public MethodDeclarations findUseMethod(MethodDeclaration methodDeclaration) {
        return methodUseMethods.stream()
                .filter(methodRelation -> methodRelation.fromMethodIs(methodDeclaration))
                .map(MethodRelation::to)
                .collect(MethodDeclarations.collector());
    }

    @Override
    public MethodDeclarations methodsOf(TypeIdentifier typeIdentifier) {
        return memberMethods.stream()
                .filter(typeMethodRelation -> typeMethodRelation.typeIs(typeIdentifier))
                .map(TypeMethodRelation::method)
                .collect(MethodDeclarations.collector());
    }

    @Override
    public TypeIdentifiers findFieldUsage(TypeIdentifier typeIdentifier) {
        return memberTypes.stream()
                .filter(typeRelation -> typeRelation.field().typeIdentifier().equals(typeIdentifier))
                .map(TypeRelation::from)
                .collect(TypeIdentifiers.collector());
    }

    @Override
    public MethodDeclarations findMethodUsage(TypeIdentifier typeIdentifier) {
        return Stream.of(methodReturnTypes, methodParameterTypes, methodUseTypes).flatMap(Set::stream)
                .filter(methodTypeRelation -> methodTypeRelation.typeIs(typeIdentifier))
                .map(MethodTypeRelation::method)
                .collect(MethodDeclarations.collector());
    }

    @Override
    public FieldDeclarations findConstants(TypeIdentifier type) {
        return constants.stream()
                .filter(typeRelation -> typeRelation.from().equals(type))
                .map(TypeRelation::field)
                .collect(FieldDeclarations.collector());
    }

    @Override
    public FieldDeclarations findFieldsOf(TypeIdentifier type) {
        return memberTypes.stream()
                .filter(typeRelation -> typeRelation.from().equals(type))
                .map(TypeRelation::field)
                .collect(FieldDeclarations.collector());
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
