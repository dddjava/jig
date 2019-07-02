package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.implementation.source.binary.ClassSources;

/**
 * 対象から実装を取得するファクトリ
 */
public interface ByteCodeFactory {

    TypeByteCodes readFrom(ClassSources classSources);
}
