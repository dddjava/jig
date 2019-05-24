package org.dddjava.jig.domain.model.implementation.analyzed.bytecode;

import org.dddjava.jig.domain.model.implementation.raw.classfile.ClassSources;

/**
 * 対象から実装を取得するファクトリ
 */
public interface ByteCodeFactory {

    TypeByteCodes readFrom(ClassSources classSources);
}
