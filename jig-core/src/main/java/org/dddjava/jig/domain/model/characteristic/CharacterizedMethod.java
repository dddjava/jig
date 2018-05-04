package org.dddjava.jig.domain.model.characteristic;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;

public class CharacterizedMethod {

    MethodDeclaration methodDeclaration;
    MethodCharacteristics methodCharacteristics;

    public CharacterizedMethod(MethodDeclaration methodDeclaration, MethodCharacteristics methodCharacteristics) {
        this.methodDeclaration = methodDeclaration;
        this.methodCharacteristics = methodCharacteristics;
    }
}
