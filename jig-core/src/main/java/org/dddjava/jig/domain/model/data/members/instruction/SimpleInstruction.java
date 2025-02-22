package org.dddjava.jig.domain.model.data.members.instruction;

/**
 * メソッドで行われる単純な命令
 *
 * バイトコードの全てを転写するのではなく、JIGとして意味のあるものを持つ
 */
public record SimpleInstruction(MethodInstructionType type) {
}
