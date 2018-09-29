package org.dddjava.jig.domain.model.characteristic;

import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 特徴付けられた型の生成器
 */
public interface CharacterizedTypeFactory {

    default CharacterizedType create(TypeByteCode typeByteCode) {
        return new CharacterizedType(
                typeByteCode.typeIdentifier(),
                Arrays.stream(Characteristic.values())
                        .filter(characteristic -> characteristic.matches(typeByteCode, this))
                        .collect(Collectors.toSet()));
    }

    boolean isRepository(TypeByteCode typeByteCode);
}
