package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.stream.Stream;

/**
 * 分岐
 *
 * ```
 *   ifeq, ifne, ifle, iflt, ifge, ifgt,
 *   ifnull, ifnonnull,
 *   if_icmpeq, if_icmpne, if_icmple, if_icmplt, if_icmpge, if_icmpgt,
 *   if_acmpeq, if_acmpne
 * ```
 *
 * `jsr, jsr_w, goto, goto_w` は jump or branch instructionで括られるが、これには含まない。
 * switchはtargetが複数ある分岐なので別で扱う。
 */
public record IfInstruction(TargetInstruction target) implements Instruction {

    public static Instruction from(String targetId) {
        return new IfInstruction(new TargetInstruction(targetId));
    }

    @Override
    public Stream<TypeIdentifier> streamAssociatedTypes() {
        return Stream.empty();
    }
}
