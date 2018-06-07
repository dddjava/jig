package org.dddjava.jig.domain.model.services;

import org.dddjava.jig.domain.model.characteristic.*;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;
import org.dddjava.jig.domain.model.implementation.relation.MethodRelations;

/**
 * サービスの切り口
 */
public class ServiceAngle {

    MethodDeclaration methodDeclaration;
    Characteristics userCharacteristics;
    private final MethodDeclarations userServiceMethods;
    TypeIdentifiers usingFieldTypeIdentifiers;
    MethodDeclarations usingRepositoryMethods;
    private final MethodCharacteristics methodCharacteristics;

    public ServiceAngle(MethodDeclaration methodDeclaration, Characteristics userCharacteristics, MethodDeclarations userServiceMethods, TypeIdentifiers usingFieldTypeIdentifiers, MethodDeclarations usingRepositoryMethods, MethodCharacteristics methodCharacteristics) {
        this.methodDeclaration = methodDeclaration;
        this.userCharacteristics = userCharacteristics;
        this.userServiceMethods = userServiceMethods;
        this.usingFieldTypeIdentifiers = usingFieldTypeIdentifiers;
        this.usingRepositoryMethods = usingRepositoryMethods;
        this.methodCharacteristics = methodCharacteristics;
    }

    public MethodDeclaration method() {
        return methodDeclaration;
    }

    public TypeIdentifiers usingFields() {
        return usingFieldTypeIdentifiers;
    }

    public MethodDeclarations usingRepositoryMethods() {
        return usingRepositoryMethods;
    }

    public boolean usingFromController() {
        // TODO MethodCharacteristic.HANDLERで判別させたい
        return userCharacteristics.has(Characteristic.CONTROLLER);
    }

    public MethodDeclarations userServiceMethods() {
        return userServiceMethods;
    }

    public static ServiceAngle of(MethodDeclaration serviceMethod, MethodRelations methodRelations, CharacterizedTypes characterizedTypes, MethodUsingFields methodUsingFields, CharacterizedMethods characterizedMethods) {

        Characteristics userCharacteristics = characterizedTypes.stream()
                .filter(methodRelations.stream().filterTo(serviceMethod).fromTypeIdentifiers())
                .characteristics();

        MethodDeclarations userServiceMethods = methodRelations.stream().filterTo(serviceMethod)
                .filterFromTypeIsIncluded(characterizedTypes.stream().filter(Characteristic.SERVICE).typeIdentifiers())
                .fromMethods();

        TypeIdentifiers usingFieldTypeIdentifiers = methodUsingFields.stream()
                .filter(serviceMethod)
                .fields()
                .toTypeIdentifies();

        MethodDeclarations usingRepositoryMethods = methodRelations.stream().filterFrom(serviceMethod)
                .filterToTypeIsIncluded(characterizedTypes.stream().filter(Characteristic.REPOSITORY).typeIdentifiers())
                .toMethods();

        MethodCharacteristics methodCharacteristics = characterizedMethods.characteristicsOf(serviceMethod);

        return new ServiceAngle(serviceMethod, userCharacteristics, userServiceMethods, usingFieldTypeIdentifiers, usingRepositoryMethods, methodCharacteristics);
    }

    public MethodCharacteristics methodCharacteristics() {
        return methodCharacteristics;
    }
}
