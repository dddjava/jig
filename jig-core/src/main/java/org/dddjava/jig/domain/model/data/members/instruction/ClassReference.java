package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.stream.Stream;

public record ClassReference(TypeIdentifier typeIdentifier) implements Instruction {

    @Override
    public Stream<TypeIdentifier> streamAssociatedTypes() {
        return Stream.of(typeIdentifier);
    }
}
