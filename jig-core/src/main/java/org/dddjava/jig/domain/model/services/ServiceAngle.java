package org.dddjava.jig.domain.model.services;

import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.characteristic.Characteristics;
import org.dddjava.jig.domain.model.characteristic.Satisfaction;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;

public class ServiceAngle {

    MethodDeclaration methodDeclaration;
    Characteristics userCharacteristics;
    private final MethodDeclarations userServiceMethods;
    TypeIdentifiers usingFieldTypeIdentifiers;
    MethodDeclarations usingRepositoryMethods;

    public ServiceAngle(MethodDeclaration methodDeclaration, Characteristics userCharacteristics, MethodDeclarations userServiceMethods, TypeIdentifiers usingFieldTypeIdentifiers, MethodDeclarations usingRepositoryMethods) {
        this.methodDeclaration = methodDeclaration;
        this.userCharacteristics = userCharacteristics;
        this.userServiceMethods = userServiceMethods;
        this.usingFieldTypeIdentifiers = usingFieldTypeIdentifiers;
        this.usingRepositoryMethods = usingRepositoryMethods;
    }

    public MethodDeclaration method() {
        return methodDeclaration;
    }

    public TypeIdentifier returnType() {
        return methodDeclaration.returnType();
    }

    public TypeIdentifiers usingFields() {
        return usingFieldTypeIdentifiers;
    }

    public MethodDeclarations usingRepositoryMethods() {
        return usingRepositoryMethods;
    }

    public Satisfaction usingFromController() {
        // TODO MethodCharacteristic.HANDLERで判別させたい
        return userCharacteristics.has(Characteristic.CONTROLLER);
    }

    public MethodDeclarations userServiceMethods() {
        return userServiceMethods;
    }
}
