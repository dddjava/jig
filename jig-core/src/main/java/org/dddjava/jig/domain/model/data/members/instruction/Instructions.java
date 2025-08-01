package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldId;
import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 命令リスト
 *
 * メソッド（コンストラクタやイニシャライザも含まれる。）で実装される一塊。
 * バイトコード上の順番を維持するため、Listで保持する。
 */
public record Instructions(List<Instruction> instructions) {

    public int cyclomaticComplexity() {
        return 1 + instructions.stream()
                .mapToInt(Instruction::cyclomaticComplexity)
                .sum();
    }

    public Stream<TypeId> associatedTypeStream() {
        return instructions.stream()
                .flatMap(Instruction::streamAssociatedTypes);
    }

    public Stream<JigFieldId> fieldReferenceStream() {
        // lambdaを展開する？
        return instructions.stream()
                .filter(instruction -> instruction instanceof FieldAccess)
                .map(instruction -> (FieldAccess) instruction)
                .map(FieldAccess::jigFieldId);
    }

    public boolean containsSimpleInstruction(SimpleInstruction simpleInstruction) {
        return instructions.contains(simpleInstruction);
    }

    public boolean containsAny(Predicate<Instruction> predicate) {
        return instructions.stream().anyMatch(predicate);
    }

    public Stream<MethodCall> methodCallStream() {
        return instructions.stream()
                .flatMap(Instruction::findMethodCall);
    }

    public Stream<MethodCall> lambdaInlinedMethodCallStream() {
        return instructions.stream()
                .flatMap(Instruction::lambdaInlinedMethodCallStream);
    }
}
