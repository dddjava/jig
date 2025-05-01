package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.stream.Stream;

/**
 * 分岐
 * switchは含まない。
 *
 * jump or branch instruction = jsr, jsr_w, goto, goto_w, ifeq, ifne, ifle, iflt, ifge, ifgt, ifnull, ifnonnull, if_icmpeq, if_icmpne, if_icmple, if_icmplt, if_icmpge, if_icmpgt, if_acmpeq, if_acmpne
 */
public record JumpOrBranchInstruction(Instruction target) implements Instruction {

    @Override
    public Stream<TypeIdentifier> streamAssociatedTypes() {
        return Stream.empty();
    }
}
