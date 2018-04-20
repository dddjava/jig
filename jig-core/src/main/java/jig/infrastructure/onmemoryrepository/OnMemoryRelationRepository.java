package jig.infrastructure.onmemoryrepository;

import jig.domain.model.declaration.field.FieldDeclaration;
import jig.domain.model.declaration.field.FieldDeclarations;
import jig.domain.model.declaration.method.MethodDeclaration;
import jig.domain.model.declaration.method.MethodDeclarations;
import jig.domain.model.declaration.method.MethodSignature;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;
import jig.domain.model.relation.MethodRelation;
import jig.domain.model.relation.MethodTypeRelation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.TypeMethodRelation;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Stream;

@Repository
public class OnMemoryRelationRepository implements RelationRepository {

    final List<FieldDeclaration> instanceFields = new ArrayList<>();
    final List<FieldDeclaration> staticFields = new ArrayList<>();

    final Set<TypeMethodRelation> memberMethods = new HashSet<>();
    final Set<MethodTypeRelation> methodReturnTypes = new HashSet<>();
    final Set<MethodTypeRelation> methodParameterTypes = new HashSet<>();
    final Set<MethodTypeRelation> methodUseTypes = new HashSet<>();
    final Set<MethodRelation> methodImplementMethods = new HashSet<>();

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
    public void registerMethodUseType(MethodDeclaration methodDeclaration, TypeIdentifier typeIdentifier) {
        methodUseTypes.add(new MethodTypeRelation(methodDeclaration, typeIdentifier));

    }

    @Override
    public void registerImplementation(MethodDeclaration from, MethodDeclaration to) {
        methodImplementMethods.add(new MethodRelation(from, to));
    }

    @Override
    public void registerField(FieldDeclaration fieldDeclaration) {
        instanceFields.add(fieldDeclaration);
    }

    @Override
    public void registerConstants(FieldDeclaration fieldDeclaration) {
        staticFields.add(fieldDeclaration);
    }

    @Override
    public TypeIdentifier getReturnTypeOf(MethodDeclaration methodDeclaration) {
        return methodReturnTypes.stream()
                .filter(methodTypeRelation -> methodTypeRelation.methodIs(methodDeclaration))
                .map(MethodTypeRelation::type)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(methodDeclaration.asFullText()));
    }

    Map<MethodDeclaration, FieldDeclarations> methodUseFieldsMap = new HashMap<>();

    @Override
    public void registerMethodUseFields(MethodDeclaration methodDeclaration, FieldDeclarations fieldDeclarations) {
        methodUseFieldsMap.put(methodDeclaration, fieldDeclarations);

        fieldDeclarations.list().forEach(fieldDeclaration ->
                registerMethodUseType(methodDeclaration, fieldDeclaration.typeIdentifier()));
    }

    @Override
    public FieldDeclarations findUseFields(MethodDeclaration methodDeclaration) {
        return methodUseFieldsMap.getOrDefault(methodDeclaration, FieldDeclarations.empty());
    }

    @Override
    public MethodDeclarations findConcrete(MethodDeclaration methodDeclaration) {
        return methodImplementMethods.stream()
                .filter(methodRelation -> methodRelation.interfaceMethodIs(methodDeclaration))
                .map(MethodRelation::concreteMethod)
                .collect(MethodDeclarations.collector());
    }

    Map<MethodDeclaration, MethodDeclarations> methodUseMethodsMap = new HashMap<>();

    @Override
    public void registerMethodUseMethod(MethodDeclaration methodDeclaration, MethodDeclarations methodDeclarations) {
        methodUseMethodsMap.put(methodDeclaration, methodDeclarations);
    }

    @Override
    public MethodDeclarations findUseMethod(MethodDeclaration methodDeclaration) {
        return methodUseMethodsMap.get(methodDeclaration);
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
        return instanceFields.stream()
                .filter(fieldDeclaration -> fieldDeclaration.typeIdentifier().equals(typeIdentifier))
                .map(FieldDeclaration::declaringType)
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
        return staticFields.stream()
                .filter(fieldDeclaration -> fieldDeclaration.declaringType().equals(type))
                .collect(FieldDeclarations.collector());
    }

    @Override
    public FieldDeclarations findFieldsOf(TypeIdentifier type) {
        return instanceFields.stream()
                .filter(fieldDeclaration -> fieldDeclaration.declaringType().equals(type))
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
