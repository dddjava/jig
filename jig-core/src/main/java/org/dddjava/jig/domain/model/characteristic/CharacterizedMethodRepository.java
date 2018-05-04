package org.dddjava.jig.domain.model.characteristic;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;

public interface CharacterizedMethodRepository {

    CharacterizedMethod get(MethodDeclaration methodDeclaration);

    MethodDeclarations getCharacterizedMethods(MethodCharacteristic methodCharacteristic);

    void register(MethodCharacteristic methodCharacteristic, MethodDeclaration methodDeclaration);
}
