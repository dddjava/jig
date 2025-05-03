package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.stream.Stream;

public record TryCacthInstruction(TargetInstruction start,
                                  TargetInstruction end,
                                  TargetInstruction handler,
                                  String type) implements Instruction {

    @Override
    public Stream<TypeIdentifier> streamAssociatedTypes() {
        return Stream.empty();
    }
}
