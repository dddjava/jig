package org.dddjava.jig.domain.model.data.members.methods;

/**
 * Methodの扱うaccess_flagsのうち、可視性と所有関係を除くもの。
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.6">4.6 Methods</a>
 */
public enum JigMethodFlag {
    /**
     * synchronizedメソッド
     */
    SYNCHRONIZED,
    /**
     * コンパイラによって生成されるブリッジメソッド
     */
    BRIDGE(true),
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
    SYNTHETIC(true),

    // -- JIG独自フラグ
    /**
     * インスタンスイニシャライザおよびコンストラクタ
     */
    INITIALIZER(true),
    /**
     * クラスイニシャライザ
     */
    STATIC_INITIALIZER(true),
    /**
     * enumを定義した際に生成されるvalueOfなど
     */
    ENUM_SUPPORT(true),
    /**
     * lambda式を記述した際にコンパイラによって生成される
     */
    LAMBDA_SUPPORT(true),
    /**
     * recordを定義した際に生成されるアクセサ
     */
    RECORD_COMPONENT_ACCESSOR(true);

    private final boolean compilerGenerated;

    JigMethodFlag() {
        this(false);
    }

    JigMethodFlag(boolean compilerGenerated) {
        this.compilerGenerated = compilerGenerated;
    }

    public boolean compilerGenerated() {
        return compilerGenerated;
    }
}
