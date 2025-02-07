package org.dddjava.jig.domain.model.data.members;

/**
 * Fieldの扱うaccess_flagsのうち、可視性と所有関係を除くもの。
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.5">...</a>
 */
public enum JigFieldFlag {
    FINAL,
    TRANSIENT,
    VOLATILE,
    SYNTHETIC,
    ENUM
}
