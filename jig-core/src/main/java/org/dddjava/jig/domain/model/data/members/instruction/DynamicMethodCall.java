package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.List;
import java.util.stream.Stream;

public record DynamicMethodCall(MethodCall methodCall, TypeIdentifier returnType,
                                List<TypeIdentifier> argumentTypes) implements Instruction {

    @Override
    public Stream<MethodCall> findMethodCall() {
        return Stream.of(methodCall());
    }

    @Override
    public Stream<TypeIdentifier> streamAssociatedTypes() {
        return Stream.of(
                methodCall().streamAssociatedTypes(),
                argumentTypes().stream(),
                // voidは返さなくていいと思うんだ
                Stream.of(returnType())
        ).flatMap(stream -> stream);
    }
}
