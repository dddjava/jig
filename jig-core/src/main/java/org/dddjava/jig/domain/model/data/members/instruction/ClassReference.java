package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.stream.Stream;

/**
 * クラス参照
 *
 * .classなどでの使用。型引数は付かないので `TypeId` で扱う。
 */
public record ClassReference(TypeId targetTypeId) implements Instruction {

    @Override
    public Stream<TypeId> streamAssociatedTypes() {
        return Stream.of(targetTypeId);
    }
}
