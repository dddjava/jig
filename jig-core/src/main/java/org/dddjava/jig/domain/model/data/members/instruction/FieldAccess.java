package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.stream.Stream;

/**
 * フィールドの使用
 */
public sealed interface FieldAccess extends Instruction
        permits GetAccess, SetAccess, UnknownAccess {

    TypeIdentifier fieldTypeIdentifier();

    JigFieldIdentifier jigFieldIdentifier();

    static FieldAccess set(TypeIdentifier typeIdentifier, JigFieldIdentifier jigFieldIdentifier) {
        return new SetAccess(typeIdentifier, jigFieldIdentifier);
    }

    static FieldAccess get(TypeIdentifier typeIdentifier, JigFieldIdentifier jigFieldIdentifier) {
        return new GetAccess(typeIdentifier, jigFieldIdentifier);
    }

    static FieldAccess unknown(TypeIdentifier typeIdentifier, JigFieldIdentifier jigFieldIdentifier) {
        return new UnknownAccess(typeIdentifier, jigFieldIdentifier);
    }

    @Override
    default Stream<TypeIdentifier> streamAssociatedTypes() {
        return Stream.of(fieldTypeIdentifier(), jigFieldIdentifier().declaringTypeIdentifier());
    }
}

record GetAccess(TypeIdentifier fieldTypeIdentifier, JigFieldIdentifier jigFieldIdentifier) implements FieldAccess {
}

record SetAccess(TypeIdentifier fieldTypeIdentifier, JigFieldIdentifier jigFieldIdentifier) implements FieldAccess {
}

record UnknownAccess(TypeIdentifier fieldTypeIdentifier, JigFieldIdentifier jigFieldIdentifier) implements FieldAccess {
}
