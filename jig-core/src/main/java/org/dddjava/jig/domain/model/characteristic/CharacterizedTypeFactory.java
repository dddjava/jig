package org.dddjava.jig.domain.model.characteristic;

import org.dddjava.jig.domain.model.implementation.bytecode.ByteCode;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 特徴付けられた型の生成器
 */
public interface CharacterizedTypeFactory {

    default CharacterizedType create(ByteCode byteCode) {
        return new CharacterizedType(
                byteCode.typeIdentifier(),
                Arrays.stream(Characteristic.values())
                        .filter(characteristic -> characteristic.matches(byteCode, this))
                        .collect(Collectors.toSet()));
    }

    boolean isModel(ByteCode byteCode);

    boolean isRepository(ByteCode byteCode);
}
