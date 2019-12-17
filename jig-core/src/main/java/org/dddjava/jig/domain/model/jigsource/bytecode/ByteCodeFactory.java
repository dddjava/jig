package org.dddjava.jig.domain.model.jigsource.bytecode;

import org.dddjava.jig.domain.model.jigsource.source.binary.ClassSources;

/**
 * 対象から実装を取得するファクトリ
 */
public interface ByteCodeFactory {

    TypeByteCodes readFrom(ClassSources classSources);
}
