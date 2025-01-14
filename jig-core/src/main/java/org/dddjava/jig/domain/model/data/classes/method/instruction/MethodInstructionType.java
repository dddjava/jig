package org.dddjava.jig.domain.model.data.classes.method.instruction;

/**
 * メソッドに対する操作
 *
 * オペコードに対するASMのまとめた単位と近似する
 * https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html
 */
public enum MethodInstructionType {
    /**
     * GETSTATIC, PUTSTATIC, GETFIELD or PUTFIELD.
     */
    FIELD,
    /**
     * INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC or INVOKEINTERFACE.
     */
    METHOD,
    /**
     * LDC?
     */
    CLASS参照,
    /**
     * LOOKUPSWITCH
     */
    SWITCH,
    /**
     * IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE, GOTO, JSR, IFNULL or IFNONNULL.
     */
    JUMP,
    /**
     * IFNONNULL, IFNULL
     */
    NULL判定,
    /**
     * INVOKEDYNAMIC
     */
    InvokeDynamic,
    /**
     * ACONST_NULL
     */
    NULL参照
}
