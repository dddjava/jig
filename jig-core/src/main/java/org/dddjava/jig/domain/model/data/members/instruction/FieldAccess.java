package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldId;
import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.stream.Stream;

/**
 * フィールドの使用
 */
public sealed interface FieldAccess extends Instruction
        permits GetAccess, SetAccess, UnknownAccess {

    TypeId fieldTypeId();

    JigFieldId jigFieldId();

    static FieldAccess set(TypeId typeId, JigFieldId jigFieldId) {
        return new SetAccess(typeId, jigFieldId);
    }

    static FieldAccess get(TypeId typeId, JigFieldId jigFieldId) {
        return new GetAccess(typeId, jigFieldId);
    }

    static FieldAccess unknown(TypeId typeId, JigFieldId jigFieldId) {
        return new UnknownAccess(typeId, jigFieldId);
    }

    @Override
    default Stream<TypeId> associatedTypeStream() {
        return Stream.of(fieldTypeId(), jigFieldId().declaringTypeId());
    }
}

record GetAccess(TypeId fieldTypeId, JigFieldId jigFieldId) implements FieldAccess {
}

record SetAccess(TypeId fieldTypeId, JigFieldId jigFieldId) implements FieldAccess {
}

record UnknownAccess(TypeId fieldTypeId, JigFieldId jigFieldId) implements FieldAccess {
}
