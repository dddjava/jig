package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.members.JigFieldIdentifier;

public sealed interface FieldAccess extends Instruction
        permits GetAccess, SetAccess, UnknownAccess {
    JigFieldIdentifier jigFieldIdentifier();

    static FieldAccess set(JigFieldIdentifier jigFieldIdentifier) {
        return new SetAccess(jigFieldIdentifier);
    }

    static FieldAccess get(JigFieldIdentifier jigFieldIdentifier) {
        return new GetAccess(jigFieldIdentifier);
    }

    static FieldAccess unknown(JigFieldIdentifier jigFieldIdentifier) {
        return new UnknownAccess(jigFieldIdentifier);
    }
}

record GetAccess(JigFieldIdentifier jigFieldIdentifier) implements FieldAccess {
}

record SetAccess(JigFieldIdentifier jigFieldIdentifier) implements FieldAccess {
}

record UnknownAccess(JigFieldIdentifier jigFieldIdentifier) implements FieldAccess {
}
