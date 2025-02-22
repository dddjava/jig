package org.dddjava.jig.domain.model.data.members.instruction;

/**
 * メソッドに対する操作
 *
 * オペコードに対するASMのまとめた単位と近似する
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html">JVMS/Chapter 4. The class File Formata>
 */
public enum MethodInstructionType {
    /**
     * LOOKUPSWITCH
     */
    SWITCH,
    /**
     * IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE, IFNULL or IFNONNULL.
     */
    JUMP,
    /**
     * IFNONNULL, IFNULL
     */
    NULL判定,
    /**
     * ACONST_NULL
     */
    NULL参照
}
