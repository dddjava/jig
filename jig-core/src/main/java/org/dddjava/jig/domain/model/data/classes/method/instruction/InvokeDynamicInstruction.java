package org.dddjava.jig.domain.model.data.classes.method.instruction;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;

import java.util.List;
import java.util.stream.Stream;

public record InvokeDynamicInstruction(
        MethodDeclaration methodDeclaration,
        TypeIdentifier returnType,
        List<TypeIdentifier> argumentTypes) {

    Stream<TypeIdentifier> usingTypes() {
        return Stream.of(
                methodDeclaration.dependsTypes().stream(),
                argumentTypes.stream(),
                Stream.of(returnType)
        ).flatMap(stream -> stream);
    }
}
