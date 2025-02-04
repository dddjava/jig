package org.dddjava.jig.domain.model.data.types;

/**
 * Java言語によるクラス修飾子およびインスタンス修飾子のうち、可視性とアノテーションを除くもの。
 * JLSではstrictfpもあるが、使用しないので扱わない。
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-8.html#jls-8.1.1">ClassModifier</a>
 * @see <a href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-9.html#jls-9.1.1">InstanceModifier</a>
 */
public enum JigTypeModifier {
    ABSTRACT,
    FINAL,
    STATIC,
    SEALED,
    NON_SEALED
}
