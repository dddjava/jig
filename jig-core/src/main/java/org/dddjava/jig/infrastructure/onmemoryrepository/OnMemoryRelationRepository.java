package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingField;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;
import org.dddjava.jig.domain.model.implementation.relation.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class OnMemoryRelationRepository implements RelationRepository {

    final List<FieldDeclaration> instanceFields = new ArrayList<>();
    final List<FieldDeclaration> staticFields = new ArrayList<>();

    final List<ImplementationMethod> methodImplementMethods = new ArrayList<>();

    final Map<TypeIdentifier, Set<MethodDeclaration>> typeUserMethods = new HashMap<>();

    @Override
    public void registerMethod(MethodDeclaration methodDeclaration) {
        methodDeclaration.methodSignature().arguments().forEach(argumentTypeIdentifier ->
                registerMethodUseType(methodDeclaration, argumentTypeIdentifier));
        registerMethodUseType(methodDeclaration, methodDeclaration.returnType());
    }

    private void registerMethodUseType(MethodDeclaration methodDeclaration, TypeIdentifier typeIdentifier) {
        if (!typeUserMethods.containsKey(typeIdentifier)) {
            typeUserMethods.put(typeIdentifier, new HashSet<>());
        }
        typeUserMethods.get(typeIdentifier).add(methodDeclaration);
    }

    @Override
    public void registerImplementation(MethodDeclaration implementationMethod, MethodDeclaration interfaceMethod) {
        methodImplementMethods.add(new ImplementationMethod(implementationMethod, interfaceMethod));
    }

    @Override
    public ImplementationMethods allImplementationMethods() {
        return new ImplementationMethods(methodImplementMethods);
    }

    @Override
    public void registerField(FieldDeclaration fieldDeclaration) {
        instanceFields.add(fieldDeclaration);
    }

    @Override
    public void registerConstants(FieldDeclaration fieldDeclaration) {
        staticFields.add(fieldDeclaration);
    }

    Map<MethodDeclaration, FieldDeclarations> methodUseFieldsMap = new HashMap<>();

    @Override
    public MethodUsingFields allMethodUsingFields() {
        return new MethodUsingFields(methodUseFieldsMap.entrySet().stream()
                .flatMap(entry -> entry.getValue().list().stream()
                        .map(value -> new MethodUsingField(entry.getKey(), value)))
                .collect(Collectors.toList()));
    }

    @Override
    public FieldDeclarations allFieldDeclarations() {
        return new FieldDeclarations(instanceFields);
    }

    @Override
    public FieldDeclarations allStaticFieldDeclarations() {
        return new FieldDeclarations(staticFields);
    }

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
                .filter(implementationMethod -> implementationMethod.interfaceMethodIs(methodDeclaration))
                .map(ImplementationMethod::implementationMethod)
                .collect(MethodDeclarations.collector());
    }

    Map<MethodDeclaration, MethodDeclarations> methodUseMethodsMap = new HashMap<>();

    @Override
    public MethodRelations allMethodRelations() {
        return new MethodRelations(methodUseMethodsMap.entrySet().stream()
                .flatMap(entry -> entry.getValue().list().stream()
                        .map(value -> new MethodRelation(entry.getKey(), value)))
                .collect(Collectors.toList()));
    }

    @Override
    public void registerMethodUseMethods(MethodDeclaration methodDeclaration, MethodDeclarations methodDeclarations) {
        methodUseMethodsMap.put(methodDeclaration, methodDeclarations);

        methodDeclarations.list().forEach(method ->
                registerMethodUseType(methodDeclaration, method.declaringType()));
    }

    @Override
    public MethodDeclarations findUseMethods(MethodDeclaration methodDeclaration) {
        return methodUseMethodsMap.get(methodDeclaration).distinct();
    }
}
