package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.relation.MethodRelation;
import org.dddjava.jig.domain.model.relation.RelationRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class OnMemoryRelationRepository implements RelationRepository {

    final List<FieldDeclaration> instanceFields = new ArrayList<>();
    final List<FieldDeclaration> staticFields = new ArrayList<>();

    final Set<MethodRelation> methodImplementMethods = new HashSet<>();

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
    public void registerMethodUseMethods(MethodDeclaration methodDeclaration, MethodDeclarations methodDeclarations) {
        methodUseMethodsMap.put(methodDeclaration, methodDeclarations);

        methodDeclarations.list().forEach(method ->
                registerMethodUseType(methodDeclaration, method.declaringType()));
    }

    @Override
    public MethodDeclarations findUseMethod(MethodDeclaration methodDeclaration) {
        return methodUseMethodsMap.get(methodDeclaration).distinct();
    }

    @Override
    public TypeIdentifiers findUserTypes(MethodDeclaration methodDeclaration) {
        return methodUseMethodsMap.entrySet().stream()
                .filter(entry -> entry.getValue().contains(methodDeclaration))
                .map(entry -> entry.getKey().declaringType())
                .collect(TypeIdentifiers.collector());
    }

    @Override
    public MethodDeclarations findUserMethods(MethodDeclaration methodDeclaration) {
        return methodUseMethodsMap.entrySet().stream()
                .filter(entry -> entry.getValue().contains(methodDeclaration))
                .map(Map.Entry::getKey)
                .collect(MethodDeclarations.collector());
    }

    TypeIdentifiers findFieldUsage(TypeIdentifier typeIdentifier) {
        return instanceFields.stream()
                .filter(fieldDeclaration -> fieldDeclaration.typeIdentifier().equals(typeIdentifier))
                .map(FieldDeclaration::declaringType)
                .collect(TypeIdentifiers.collector());
    }

    private MethodDeclarations findMethodUsage(TypeIdentifier typeIdentifier) {
        Set<MethodDeclaration> methodDeclarations = typeUserMethods.get(typeIdentifier);
        ArrayList<MethodDeclaration> list = new ArrayList<>();
        if (methodDeclarations != null) {
            list.addAll(methodDeclarations);
        }
        return new MethodDeclarations(list);
    }

    @Override
    public TypeIdentifiers findUserTypes(TypeIdentifier typeIdentifier) {
        TypeIdentifiers fieldUsage = findFieldUsage(typeIdentifier);
        TypeIdentifiers methodUsage = findMethodUsage(typeIdentifier).declaringTypes();
        return fieldUsage.merge(methodUsage);
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
}
