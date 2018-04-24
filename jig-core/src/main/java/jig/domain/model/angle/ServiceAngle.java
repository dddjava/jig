package jig.domain.model.angle;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.Characteristics;
import jig.domain.model.characteristic.Satisfaction;
import jig.domain.model.declaration.method.MethodDeclaration;
import jig.domain.model.declaration.method.MethodDeclarations;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;

public class ServiceAngle {

    MethodDeclaration methodDeclaration;
    TypeIdentifier returnTypeIdentifier;
    Characteristics userCharacteristics;
    TypeIdentifiers usingFieldTypeIdentifiers;
    MethodDeclarations usingRepositoryMethods;

    public ServiceAngle(MethodDeclaration methodDeclaration, TypeIdentifier returnTypeIdentifier, Characteristics userCharacteristics, TypeIdentifiers usingFieldTypeIdentifiers, MethodDeclarations usingRepositoryMethods) {
        this.methodDeclaration = methodDeclaration;
        this.returnTypeIdentifier = returnTypeIdentifier;
        this.userCharacteristics = userCharacteristics;
        this.usingFieldTypeIdentifiers = usingFieldTypeIdentifiers;
        this.usingRepositoryMethods = usingRepositoryMethods;
    }

    public MethodDeclaration method() {
        return methodDeclaration;
    }

    public TypeIdentifier returnType() {
        return returnTypeIdentifier;
    }

    public TypeIdentifiers usingFields() {
        return usingFieldTypeIdentifiers;
    }

    public MethodDeclarations usingRepositoryMethods() {
        return usingRepositoryMethods;
    }

    public Satisfaction usingFromController() {
        return userCharacteristics.has(Characteristic.CONTROLLER);
    }
}
