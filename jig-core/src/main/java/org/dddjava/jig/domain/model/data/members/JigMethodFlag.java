package org.dddjava.jig.domain.model.data.members;

/**
 * Fieldの扱うaccess_flagsのうち、可視性と所有関係を除くもの。
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.6">...</a>
 */
public enum JigMethodFlag {
    SYNCHRONIZED,
    /**
     * コンパイラによって生成されるブリッジメソッド
     */
    BRIDGE,
    /**
     * 可変長引数を持つ
     */
    VARARGS,
    /**
     * nativeメソッド
     */
    NATIVE,
    /**
     * abstractメソッド
     */
    ABSTRACT,
    /**
     * strictfpメソッド。廃止されているが定義としてあるので列挙はしておく。
     */
    STRICT,
    /**
     * コンパイラによって生成される合成メソッド
     */
    SYNTHETIC,

    // -- JIG独自フラグ
    /**
     * インスタンスイニシャライザおよびコンストラクタ
     */
    INITIALIZER,
    /**
     * クラスイニシャライザ
     */
    STATIC_INITIALIZER,
    /**
     * enumを定義した際に生成されるvalueOfなど
     */
    ENUM_SUPPORT,
    /**
     * lambda式を記述した際にコンパイラによって生成される
     */
    LAMBDA_SUPPORT,
    /**
     * recordを定義した際に生成されるアクセサ
     */
    RECORD_COMPONENT_ACCESSOR
}
