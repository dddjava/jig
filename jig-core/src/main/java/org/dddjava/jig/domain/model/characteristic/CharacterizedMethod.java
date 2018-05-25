package org.dddjava.jig.domain.model.characteristic;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodByteCode;

/**
 * 特徴付きのメソッド
 */
public class CharacterizedMethod {

    private final MethodByteCode methodByteCode;

    public CharacterizedMethod(MethodByteCode methodByteCode) {
        this.methodByteCode = methodByteCode;
    }

    public MethodDeclaration methodDeclaration() {
        return methodByteCode.methodDeclaration;
    }

    public boolean hasDecision() {
        return methodByteCode.hasDecision();
    }

    public boolean has(MethodCharacteristic methodCharacteristic, CharacterizedType characterizedType) {
        switch (methodCharacteristic) {
            case HAS_DECISION:
                return hasDecision();
            case SERVICE_METHOD:
                return characterizedType.has(Characteristic.SERVICE).isSatisfy();
            case REPOSITORY_METHOD:
                return characterizedType.has(Characteristic.REPOSITORY).isSatisfy();
            case MAPPER_METHOD:
                return characterizedType.has(Characteristic.MAPPER).isSatisfy();
            case HANDLER:
                // TODO
        }

        throw new IllegalArgumentException(methodCharacteristic.name());
    }
}
