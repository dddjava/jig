package org.dddjava.jig.domain.model.data.members.fields;

/**
 * フィールドの扱う属性のうちフラグで表現されるもの
 *
 * クラスファイルでは access_flags として扱われる。
 * 可視性と所有関係もフラグだが、この列挙からは除く。
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.5">4.5 Fields</a>
 */
public enum JigFieldFlag {
    FINAL,
    TRANSIENT,
    VOLATILE,
    SYNTHETIC,
    ENUM
}
