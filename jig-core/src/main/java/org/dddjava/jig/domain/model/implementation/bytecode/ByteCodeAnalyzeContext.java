package org.dddjava.jig.domain.model.implementation.bytecode;

/**
 * 解析コンテキスト
 *
 * TODO 実装をどう特徴付けるかの話なのでimplementationからは取り除いた方が良さそう
 */
public interface ByteCodeAnalyzeContext {

    boolean isModel(ByteCode byteCode);

    boolean isRepository(ByteCode byteCode);
}
