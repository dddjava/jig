package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 命令リスト
 *
 * 主にメソッドで記述されるもの。コンストラクタやイニシャライザも含まれる。
 * バイトコード上の順番を維持するため、Listで保持する。
 */
public record Instructions(List<Instruction> instructions) {

    public int decisionCount() {
        return Math.toIntExact(instructions.stream()
                .filter(instruction -> instruction instanceof IfInstruction || instruction instanceof SwitchInstruction)
                .count());
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

    public Stream<MethodCall> methodCallStream() {
        return instructions.stream()
                .flatMap(Instruction::findMethodCall);
    }

    public boolean containsAnyBasicInstruction(BasicInstruction... basicInstruction) {
        return Arrays.stream(basicInstruction).anyMatch(instructions::contains);
    }

    public boolean containsAny(Predicate<Instruction> predicate) {
        return instructions.stream().anyMatch(predicate);
    }

    public Stream<MethodCall> lambdaInlinedMethodCallStream() {
        return instructions.stream()
                .flatMap(instruction -> instruction.lambdaInlinedMethodCallStream());
    }
}
