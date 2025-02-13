package org.dddjava.jig.domain.model.data.members;

/**
 * Fieldの扱うaccess_flagsのうち、可視性と所有関係を除くもの。
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.6">...</a>
 */
public enum JigMethodFlag {
    SYNCHRONIZED,
    BRIDGE,
    VARARGS,
    NATIVE,
    ABSTRACT,
    STRICT,
    SYNTHETIC,

    // -- JIG独自フラグ
    INITIALIZER,
    STATIC_INITIALIZER,
    ENUM_SUPPORT,
    LAMBDA_SUPPORT,
    RECORD_COMPONENT_ACCESSOR
}
