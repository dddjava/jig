package org.dddjava.jig.domain.model.data.types;

/**
 * 実装上の型の種類。
 * Java言語の型宣言におけるクラス（class, enum, record）とインタフェース（interface, @interface）に対応する。
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-8.html">Classes</a>
 * @see <a href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-9.html">Interfaces</a>
 */
public enum JigTypeKind {
    CLASS,
    INTERFACE,
    ANNOTATION,
    ENUM,
    RECORD
}
