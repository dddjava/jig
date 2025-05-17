package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.List;
import java.util.stream.Stream;

/**
 * switch命令
 *
 * バイトコード上は lookupswitch と tableswitch があるが、
 * 分岐条件をドキュメントで扱っていないのでこのクラス上での差はない。
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.10.1.9.tableswitch">tableswitch</a>
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.10.1.9.lookupswitch">lookupswitch</>a>
 */
public record SwitchInstruction(JumpTarget defaultTarget,
                                List<JumpTarget> caseTargets) implements Instruction {

    public static SwitchInstruction lookup(String defaultTarget, List<String> caseTargets) {
        return new SwitchInstruction(
                new JumpTarget(defaultTarget),
                caseTargets.stream().map(JumpTarget::new).toList()
        );
    }

    public static SwitchInstruction table(String defaultTarget, List<String> caseTargets) {
        return new SwitchInstruction(
                new JumpTarget(defaultTarget),
                caseTargets.stream().map(JumpTarget::new).toList()
        );
    }

    @Override
    public Stream<TypeIdentifier> streamAssociatedTypes() {
        return Stream.empty();
    }
}