package org.dddjava.jig.domain.model.data.classes.method.instruction;

/**
 * メソッドで行われる命令
 *
 * バイトコードの全てを転写するのではなく、JIGとして意味のあるものを持つ
 */
public record MethodInstruction(MethodInstructionType type, Object detail) {
}
