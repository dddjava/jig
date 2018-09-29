package org.dddjava.jig.domain.model.services;

import org.dddjava.jig.domain.model.characteristic.*;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodRelations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;

import java.util.stream.Stream;

/**
 * サービスの切り口
 */
public class ServiceAngle {

    MethodDeclaration methodDeclaration;
    Characteristics userCharacteristics;

    private final MethodDeclarations userServiceMethods;
    private final MethodDeclarations userControllerMethods;

    TypeIdentifiers usingFieldTypeIdentifiers;
    MethodDeclarations usingRepositoryMethods;
    private final MethodCharacteristics methodCharacteristics;
    boolean useStream;

    ServiceAngle(MethodDeclaration serviceMethod, MethodRelations methodRelations, CharacterizedTypes characterizedTypes, MethodUsingFields methodUsingFields, CharacterizedMethods characterizedMethods) {
        this.methodDeclaration = serviceMethod;
        this.userCharacteristics = characterizedTypes.stream()
                .filter(methodRelations.userMethodDeclaringTypesOf(serviceMethod))
                .characteristics();

        this.usingFieldTypeIdentifiers = methodUsingFields.usingFieldTypeIdentifiers(serviceMethod);
        this.usingRepositoryMethods = methodRelations.stream().filterFrom(serviceMethod)
                .filterToTypeIsIncluded(characterizedTypes.stream().filter(Characteristic.REPOSITORY).typeIdentifiers())
                .toMethods();
        this.methodCharacteristics = characterizedMethods.characteristicsOf(serviceMethod);

        this.userServiceMethods = methodRelations.stream().filterTo(serviceMethod)
                .filterFromTypeIsIncluded(characterizedTypes.stream().filter(Characteristic.SERVICE).typeIdentifiers())
                .fromMethods();
        this.userControllerMethods = methodRelations.stream().filterTo(serviceMethod)
                .filterFromTypeIsIncluded(characterizedTypes.stream().filter(Characteristic.CONTROLLER).typeIdentifiers())
                .fromMethods();

        MethodDeclarations usingMethods = methodRelations.usingMethodsOf(serviceMethod);
        this.useStream = usingMethods.list().stream().anyMatch(methodDeclaration -> methodDeclaration.returnType().equals(new TypeIdentifier(Stream.class)));
    }

    public TypeIdentifier declaringType() {
        return methodDeclaration.declaringType();
    }

    public MethodDeclaration method() {
        return methodDeclaration;
    }

    public boolean usingFromController() {
        // TODO MethodCharacteristic.HANDLERで判別させたい
        return userCharacteristics.has(Characteristic.CONTROLLER);
    }

    public String usingFields() {
        return usingFieldTypeIdentifiers.asSimpleText();
    }

    public String usingRepositoryMethods() {
        return usingRepositoryMethods.asSimpleText();
    }

    public boolean useStream() {
        return useStream;
    }

    public MethodDeclarations userServiceMethods() {
        return userServiceMethods;
    }

    public MethodCharacteristics methodCharacteristics() {
        return methodCharacteristics;
    }

    public MethodDeclarations userControllerMethods() {
        return userControllerMethods;
    }
}
