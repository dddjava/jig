package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.stream.Stream;

/**
 * クラス参照
 *
 * .classなどでの使用。型引数は付かないので `TypeIdentifier` で扱う。
 */
public record ClassReference(TypeIdentifier typeIdentifier) implements Instruction {

    @Override
    public Stream<TypeIdentifier> streamAssociatedTypes() {
        return Stream.of(typeIdentifier);
    }
}
