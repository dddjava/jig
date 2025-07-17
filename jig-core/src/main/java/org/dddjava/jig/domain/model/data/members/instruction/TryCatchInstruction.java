package org.dddjava.jig.domain.model.data.members.instruction;

public record TryCatchInstruction(JumpTarget start,
                                  JumpTarget end,
                                  JumpTarget handler,
                                  String type) implements Instruction {
    @Override
    public int cyclomaticComplexity() {
        return 1;
    }
}
