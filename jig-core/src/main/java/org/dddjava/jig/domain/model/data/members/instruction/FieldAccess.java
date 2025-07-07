package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldId;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.stream.Stream;

/**
 * フィールドの使用
 */
public sealed interface FieldAccess extends Instruction
        permits GetAccess, SetAccess, UnknownAccess {

    TypeIdentifier fieldTypeIdentifier();

    JigFieldId jigFieldId();

    static FieldAccess set(TypeIdentifier typeIdentifier, JigFieldId jigFieldId) {
        return new SetAccess(typeIdentifier, jigFieldId);
    }

    static FieldAccess get(TypeIdentifier typeIdentifier, JigFieldId jigFieldId) {
        return new GetAccess(typeIdentifier, jigFieldId);
    }

    static FieldAccess unknown(TypeIdentifier typeIdentifier, JigFieldId jigFieldId) {
        return new UnknownAccess(typeIdentifier, jigFieldId);
    }

    @Override
    default Stream<TypeIdentifier> streamAssociatedTypes() {
        return Stream.of(fieldTypeIdentifier(), jigFieldId().declaringTypeIdentifier());
    }
}

record GetAccess(TypeIdentifier fieldTypeIdentifier, JigFieldId jigFieldId) implements FieldAccess {
}

record SetAccess(TypeIdentifier fieldTypeIdentifier, JigFieldId jigFieldId) implements FieldAccess {
}

record UnknownAccess(TypeIdentifier fieldTypeIdentifier, JigFieldId jigFieldId) implements FieldAccess {
}
