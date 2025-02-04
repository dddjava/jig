package org.dddjava.jig.domain.model.data.types;

/**
 * 実装上の型の種類。
 * Java言語の型宣言におけるクラス（class, enum, record）とインタフェース（interface, @interface）に対応する。
 */
public enum JigTypeKind {
    CLASS,
    INTERFACE,
    ANNOTATION,
    ENUM,
    RECORD
}
