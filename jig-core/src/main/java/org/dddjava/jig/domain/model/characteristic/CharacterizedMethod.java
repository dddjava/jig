package org.dddjava.jig.domain.model.characteristic;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodByteCode;

import java.util.Collection;
import java.util.HashSet;

import static org.dddjava.jig.domain.model.characteristic.MethodCharacteristic.HANDLER;

/**
 * 特徴付けられたメソッド
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

    public boolean has(MethodCharacteristic methodCharacteristic) {
        switch (methodCharacteristic) {
            case SERVICE_METHOD:
                return characterizedType.has(Characteristic.SERVICE);
            case REPOSITORY_METHOD:
                return characterizedType.has(Characteristic.REPOSITORY);
            case MAPPER_METHOD:
                return characterizedType.has(Characteristic.MAPPER);
            case MODEL_METHOD:
                return characterizedType.has(Characteristic.MODEL);
            case BOOL_QUERY:
                return methodDeclaration().returnType().isBoolean();
            case PUBLIC:
                return methodByteCode.accessor().isPublic();
            case NOT_PUBLIC:
                return methodByteCode.accessor().isNotPublic();
                // TODO
        }

        throw new IllegalArgumentException(methodCharacteristic.name());
    }

    public MethodCharacteristics characteristics() {
        Collection<MethodCharacteristic> collection = new HashSet<>();
        for (MethodCharacteristic characteristic : MethodCharacteristic.values()) {
            if (characteristic == HANDLER) continue; // TODO
            if (has(characteristic)) collection.add(characteristic);
        }
        return new MethodCharacteristics(collection);
    }
}
