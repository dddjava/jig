package org.dddjava.jig.domain.model.data.members.instruction;

/**
 * 基本的な命令
 *
 * JIGではその存在を記録するのみで、パラメタを扱わないものです。
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html">JVMS/Chapter 4. The class File Formata>
 */
public enum BasicInstruction implements Instruction {
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
