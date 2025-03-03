package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.stream.Stream;

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

    @Override
    default Stream<TypeIdentifier> streamAssociatedTypes() {
        return Stream.of(jigFieldIdentifier().declaringTypeIdentifier());
    }
}

record GetAccess(JigFieldIdentifier jigFieldIdentifier) implements FieldAccess {
}

record SetAccess(JigFieldIdentifier jigFieldIdentifier) implements FieldAccess {
}

record UnknownAccess(JigFieldIdentifier jigFieldIdentifier) implements FieldAccess {
}
