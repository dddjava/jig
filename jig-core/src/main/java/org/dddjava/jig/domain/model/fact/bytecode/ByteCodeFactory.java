package org.dddjava.jig.domain.model.fact.bytecode;

import org.dddjava.jig.domain.model.fact.source.binary.ClassSources;

/**
 * 対象から実装を取得するファクトリ
 */
public interface ByteCodeFactory {

    TypeByteCodes readFrom(ClassSources classSources);
}
