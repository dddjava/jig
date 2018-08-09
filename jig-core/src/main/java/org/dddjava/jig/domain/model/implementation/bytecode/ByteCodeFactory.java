package org.dddjava.jig.domain.model.implementation.bytecode;

/**
 * 対象から実装を取得するファクトリ
 */
public interface ByteCodeFactory {

    TypeByteCodes readFrom(ByteCodeSources byteCodeSources);
}
