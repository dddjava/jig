package org.dddjava.jig.domain.model.characteristic;

import org.dddjava.jig.domain.model.implementation.bytecode.ByteCode;

/**
 * 解析コンテキスト
 */
public interface CharacteristicContext {

    boolean isModel(ByteCode byteCode);

    boolean isRepository(ByteCode byteCode);
}
