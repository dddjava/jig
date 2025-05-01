package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.stream.Stream;

/**
 * 分岐やswitch、try-catchのターゲット
 * Instructionではないが、実装都合で一旦Instructionにしておく。
 */
public record TargetInstruction(String id) implements Instruction {

    @Override
    public Stream<TypeIdentifier> streamAssociatedTypes() {
        return Stream.empty();
    }
}
