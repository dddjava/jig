package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.List;
import java.util.stream.Stream;

public record InvokeDynamicInstruction(InvokedMethod invokedMethod, TypeIdentifier returnType,
                                       List<TypeIdentifier> argumentTypes) {

    Stream<TypeIdentifier> usingTypes() {
        return Stream.of(
                invokedMethod().extractTypeIdentifiers().stream(),
                argumentTypes().stream(),
                Stream.of(returnType())
        ).flatMap(stream -> stream);
    }
}
