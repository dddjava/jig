package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingField;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;
import org.dddjava.jig.domain.model.implementation.relation.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class OnMemoryRelationRepository implements RelationRepository {

    final List<FieldDeclaration> instanceFields = new ArrayList<>();
    final List<FieldDeclaration> staticFields = new ArrayList<>();

    final List<ImplementationMethod> methodImplementMethods = new ArrayList<>();

    @Override
    public void registerImplementation(MethodDeclaration implementationMethod, MethodDeclaration interfaceMethod) {
        methodImplementMethods.add(new ImplementationMethod(implementationMethod, interfaceMethod));
    }

    @Override
    public ImplementationMethods allImplementationMethods() {
        return new ImplementationMethods(methodImplementMethods);
    }

    @Override
    public FieldDeclarations allFieldDeclarations() {
        return new FieldDeclarations(instanceFields);
    }

    @Override
    public void registerField(FieldDeclaration fieldDeclaration) {
        instanceFields.add(fieldDeclaration);
    }

    @Override
    public FieldDeclarations allStaticFieldDeclarations() {
        return new FieldDeclarations(staticFields);
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
    public void registerMethodUseFields(MethodDeclaration methodDeclaration, FieldDeclarations fieldDeclarations) {
        methodUseFieldsMap.put(methodDeclaration, fieldDeclarations);
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
    }
}
