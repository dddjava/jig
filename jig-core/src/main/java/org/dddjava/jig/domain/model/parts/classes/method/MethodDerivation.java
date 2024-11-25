package org.dddjava.jig.domain.model.parts.classes.method;

/**
 * メソッドの由来
 */
public enum MethodDerivation {
    CONSTRUCTOR,
    COMPILER_GENERATED,

    /**
     * recordのコンポーネントメソッド。
     * バイトコード上での判別はできないが、フィールド名とメソッド名が同一のものをこれと判断する。
     */
    RECORD_COMPONENT,

    PROGRAMMER
}
