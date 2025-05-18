package org.dddjava.jig.domain.model.data.members.instruction;

/**
 * 分岐やswitch、try-catchのターゲット
 */
public record JumpTarget(String id) implements Instruction {
}
