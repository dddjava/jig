package org.dddjava.jig.domain.model.services;

import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.characteristic.Characteristics;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodRelations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;
import org.dddjava.jig.domain.model.implementation.bytecode.UsingFields;
import org.dddjava.jig.domain.model.unit.method.Method;
import org.dddjava.jig.domain.model.unit.method.Methods;

import java.util.stream.Stream;

/**
 * サービスの切り口
 */
public class ServiceAngle {

    MethodDeclaration methodDeclaration;
    Characteristics userCharacteristics;

    private final MethodDeclarations userServiceMethods;
    private final MethodDeclarations userControllerMethods;

    UsingFields usingFields;
    MethodDeclarations usingRepositoryMethods;
    boolean useStream;
    private boolean isPublic;

    ServiceAngle(ServiceMethod serviceMethod, MethodRelations methodRelations, CharacterizedTypes characterizedTypes, MethodUsingFields methodUsingFields, Methods methods) {
        this.methodDeclaration = serviceMethod.methodDeclaration();
        this.userCharacteristics = characterizedTypes.stream()
                .filter(methodRelations.userMethodDeclaringTypesOf(methodDeclaration))
                .characteristics();

        this.usingFields = methodUsingFields.usingFieldsOf(methodDeclaration);
        this.usingRepositoryMethods = methodRelations.stream().filterFrom(methodDeclaration)
                .filterToTypeIsIncluded(characterizedTypes.stream().filter(Characteristic.REPOSITORY).typeIdentifiers())
                .toMethods();

        Method method = methods.get(methodDeclaration);
        this.isPublic = method.isPublic();

        this.userServiceMethods = methodRelations.stream().filterTo(methodDeclaration)
                .filterFromTypeIsIncluded(characterizedTypes.stream().filter(Characteristic.SERVICE).typeIdentifiers())
                .fromMethods();
        this.userControllerMethods = methodRelations.stream().filterTo(methodDeclaration)
                .filterFromTypeIsIncluded(characterizedTypes.stream().filter(Characteristic.CONTROLLER).typeIdentifiers())
                .fromMethods();

        MethodDeclarations usingMethods = methodRelations.usingMethodsOf(methodDeclaration);
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

    public UsingFields usingFields() {
        return usingFields;
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

    public MethodDeclarations userControllerMethods() {
        return userControllerMethods;
    }

    public boolean isNotPublicMethod() {
        return !isPublic;
    }
}
