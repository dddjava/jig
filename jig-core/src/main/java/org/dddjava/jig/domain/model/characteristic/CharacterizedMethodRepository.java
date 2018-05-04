package org.dddjava.jig.domain.model.characteristic;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;

public interface CharacterizedMethodRepository {

    CharacterizedMethod get(MethodDeclaration methodDeclaration);

    void register(MethodCharacteristic methodCharacteristic, MethodDeclaration methodDeclaration);
}
