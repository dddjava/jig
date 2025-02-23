package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.members.JigFieldIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * メソッドで実施されている命令
 *
 * バイトコード上の順番を維持するため、Listで保持する
 */
public record Instructions(List<Instruction> instructions) {

    public DecisionNumber decisionNumber() {
        var count = instructions.stream()
                .filter(instruction -> instruction instanceof BasicInstruction)
                .filter(instruction -> ((BasicInstruction) instruction).isBranch())
                .count();
        return new DecisionNumber(Math.toIntExact(count));
    }

    public Stream<TypeIdentifier> associatedTypeStream() {
        return instructions.stream()
                .flatMap(Instruction::streamAssociatedTypes);
    }

    public Stream<JigFieldIdentifier> fieldReferenceStream() {
        return instructions.stream()
                .filter(instruction -> instruction instanceof FieldAccess)
                .map(instruction -> (FieldAccess) instruction)
                .map(FieldAccess::jigFieldIdentifier);
    }

    public Stream<ClassReference> classReferenceStream() {
        return instructions.stream()
                .filter(instruction -> instruction instanceof ClassReference)
                .map(instruction -> (ClassReference) instruction);
    }

    public Stream<MethodCall> methodCallStream() {
        return instructions.stream()
                .flatMap(Instruction::findMethodCall);
    }

    public Stream<DynamicMethodCall> invokeDynamicInstructionStream() {
        return instructions.stream()
                .filter(instruction -> instruction instanceof DynamicMethodCall)
                .map(instruction -> (DynamicMethodCall) instruction);
    }

    public boolean containsAll(BasicInstruction... basicInstruction) {
        return Arrays.stream(basicInstruction).allMatch(instructions::contains);
    }

    public boolean containsAny(BasicInstruction... basicInstruction) {
        return Arrays.stream(basicInstruction).anyMatch(instructions::contains);
    }
}
