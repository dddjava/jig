package org.dddjava.jig.domain.model.data.members.instruction;

/**
 * 単純な命令
 *
 * JIGではその存在を記録するのみで、パラメタを扱わないものです。
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html">JVMS/Chapter 4. The class File Format</a>
 */
public enum SimpleInstruction implements Instruction {

    NULL参照,

    RETURN
}
