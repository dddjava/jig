package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.members.JigFieldIdentifier;

public sealed interface FieldInstruction extends Instruction
        permits GetInstruction, SetInstruction, UnknownInstruction {
    JigFieldIdentifier jigFieldIdentifier();

    static FieldInstruction set(JigFieldIdentifier jigFieldIdentifier) {
        return new SetInstruction(jigFieldIdentifier);
    }

    static FieldInstruction get(JigFieldIdentifier jigFieldIdentifier) {
        return new GetInstruction(jigFieldIdentifier);
    }

    static FieldInstruction unknown(JigFieldIdentifier jigFieldIdentifier) {
        return new UnknownInstruction(jigFieldIdentifier);
    }
}

record GetInstruction(JigFieldIdentifier jigFieldIdentifier) implements FieldInstruction {
}

record SetInstruction(JigFieldIdentifier jigFieldIdentifier) implements FieldInstruction {
}

record UnknownInstruction(JigFieldIdentifier jigFieldIdentifier) implements FieldInstruction {
}
