package org.dddjava.jig.domain.model.data.members.instruction;

/**
 * メソッドで行われる単純な命令
 *
 * 種類のみで追加の情報をJIGでは扱わない命令をまとめて扱う。
 */
public record SimpleInstruction(SimpleInstructionType type) {
}
