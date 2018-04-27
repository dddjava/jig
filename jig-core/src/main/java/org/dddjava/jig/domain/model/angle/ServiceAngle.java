package org.dddjava.jig.domain.model.angle;

import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.characteristic.Characteristics;
import org.dddjava.jig.domain.model.characteristic.Satisfaction;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.characteristic.Characteristics;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;

public class ServiceAngle {

    MethodDeclaration methodDeclaration;
    TypeIdentifier returnTypeIdentifier;
    Characteristics userCharacteristics;
    private final MethodDeclarations userServiceMethods;
    TypeIdentifiers usingFieldTypeIdentifiers;
    MethodDeclarations usingRepositoryMethods;

    public ServiceAngle(MethodDeclaration methodDeclaration, TypeIdentifier returnTypeIdentifier, Characteristics userCharacteristics, MethodDeclarations userServiceMethods, TypeIdentifiers usingFieldTypeIdentifiers, MethodDeclarations usingRepositoryMethods) {
        this.methodDeclaration = methodDeclaration;
        this.returnTypeIdentifier = returnTypeIdentifier;
        this.userCharacteristics = userCharacteristics;
        this.userServiceMethods = userServiceMethods;
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

    public MethodDeclarations userServiceMethods() {
        return userServiceMethods;
    }
}
