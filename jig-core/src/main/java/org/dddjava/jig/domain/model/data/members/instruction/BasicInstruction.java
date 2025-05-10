package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.stream.Stream;

/**
 * 基本的な命令
 *
 * JIGではその存在を記録するのみで、パラメタを扱わないものです。
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html">JVMS/Chapter 4. The class File Format</a>
 */
public enum BasicInstruction implements Instruction {
    /**
     * LOOKUPSWITCH, TABLESWITCH
     */
    SWITCH,
    /**
     * IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE, IFNULL or IFNONNULL.
     */
    判定,
    /**
     * IFNONNULL, IFNULL
     */
    NULL判定,
    /**
     * ACONST_NULL
     */
    NULL参照,

    RETURN;

    public boolean isBranch() {
        return this == SWITCH || this == 判定 || this == NULL判定;
    }

    @Override
    public Stream<TypeIdentifier> streamAssociatedTypes() {
        return Stream.empty();
    }
}
