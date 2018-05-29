package org.dddjava.jig.domain.model.characteristic;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodByteCode;

import java.util.Collection;
import java.util.HashSet;

/**
 * 特徴付きのメソッド
 */
public class CharacterizedMethod {

    private final MethodByteCode methodByteCode;
    private final CharacterizedType characterizedType;

    public CharacterizedMethod(MethodByteCode methodByteCode, CharacterizedType characterizedType) {
        this.methodByteCode = methodByteCode;
        this.characterizedType = characterizedType;
    }

    public MethodDeclaration methodDeclaration() {
        return methodByteCode.methodDeclaration;
    }

    public boolean hasDecision() {
        return methodByteCode.hasDecision();
    }

    public boolean has(MethodCharacteristic methodCharacteristic) {
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
            case MODEL_METHOD:
                return characterizedType.has(Characteristic.MODEL).isSatisfy();
            case BOOL_QUERY:
                return methodDeclaration().returnType().isBoolean();
        }

        throw new IllegalArgumentException(methodCharacteristic.name());
    }

    public MethodCharacteristics characteristics() {
        Collection<MethodCharacteristic> collection = new HashSet<>();

        if (hasDecision()) {
            collection.add(MethodCharacteristic.HAS_DECISION);
        }

        collection.add(methodByteCode.accessor());

        if (has(MethodCharacteristic.MODEL_METHOD)) {
            collection.add(MethodCharacteristic.MODEL_METHOD);
        }

        if (has(MethodCharacteristic.BOOL_QUERY)) {
            collection.add(MethodCharacteristic.BOOL_QUERY);
        }


        return new MethodCharacteristics(collection);
    }
}
