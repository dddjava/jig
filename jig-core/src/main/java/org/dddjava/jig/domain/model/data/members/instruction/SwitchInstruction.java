package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.List;
import java.util.stream.Stream;

/**
 * switch命令
 *
 * LOOKUPSWITCH, TABLESWITCH命令を表現する
 */
public record SwitchInstruction(Kind kind, TargetInstruction defaultTarget, List<TargetInstruction> caseTargets) implements Instruction {

    private enum Kind {
        LOOKUP,
        TABLE
    }

    public static SwitchInstruction lookup(String defaultTarget, List<String> caseTargets) {
        return new SwitchInstruction(
                Kind.LOOKUP,
                new TargetInstruction(defaultTarget),
                caseTargets.stream().map(TargetInstruction::new).toList()
        );
    }

    public static SwitchInstruction table(String defaultTarget, List<String> caseTargets) {
        return new SwitchInstruction(
                Kind.TABLE,
                new TargetInstruction(defaultTarget),
                caseTargets.stream().map(TargetInstruction::new).toList()
        );
    }

    @Override
    public Stream<TypeIdentifier> streamAssociatedTypes() {
        return Stream.empty();
    }
}