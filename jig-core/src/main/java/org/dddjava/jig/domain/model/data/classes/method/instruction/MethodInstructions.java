package org.dddjava.jig.domain.model.data.classes.method.instruction;

import org.dddjava.jig.domain.model.data.classes.method.DecisionNumber;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * メソッドで実施されている命令
 *
 * バイトコード上の順番を維持する
 */
public record MethodInstructions(List<MethodInstruction> values) {

    public static MethodInstructions newInstance() {
        return new MethodInstructions(new ArrayList<>());
    }

    public void register(MethodInstructionType type) {
        values.add(new MethodInstruction(type, null));
    }

    public void registerField(TypeIdentifier declaringType, TypeIdentifier fieldTypeIdentifier, String name) {
        values.add(new MethodInstruction(MethodInstructionType.FIELD, new FieldReference(declaringType, fieldTypeIdentifier, name)));
    }

    public void registerMethod(MethodDeclaration methodDeclaration) {
        values.add(new MethodInstruction(MethodInstructionType.METHOD, methodDeclaration));
    }

    public void registerClassReference(TypeIdentifier typeIdentifier) {
        values.add(new MethodInstruction(MethodInstructionType.CLASS参照, typeIdentifier));
    }

    public MethodDeclarations instructMethods() {
        return values.stream()
                .filter(instruction -> instruction.type() == MethodInstructionType.METHOD)
                .map(instruction -> (MethodDeclaration) instruction.detail())
                .collect(MethodDeclarations.collector());
    }

    public boolean hasMemberInstruction() {
        // dataじゃなくinformationかknowledgeに持っていきたい
        // FIXME 「自身のメンバアクセス」の条件になっていない。
        return values.stream().anyMatch(instruction ->
                instruction.type() == MethodInstructionType.METHOD || instruction.type() == MethodInstructionType.FIELD);
    }

    public boolean hasNullDecision() {
        return values.stream().anyMatch(instruction -> instruction.type() == MethodInstructionType.NULL判定);
    }

    public DecisionNumber decisionNumber() {
        var count = values.stream()
                .filter(instruction -> instruction.type() == MethodInstructionType.JUMP || instruction.type() == MethodInstructionType.SWITCH).count();
        return new DecisionNumber(Math.toIntExact(count));
    }

    public boolean hasNullReference() {
        return values.stream().anyMatch(instruction -> instruction.type() == MethodInstructionType.NULL参照);
    }

    public Set<TypeIdentifier> usingTypes() {
        return values.stream()
                .flatMap(instruction ->
                        switch (instruction.type()) {
                            case FIELD -> Stream.of(
                                    ((FieldReference) instruction.detail()).fieldTypeIdentifier(),
                                    ((FieldReference) instruction.detail()).declaringType()
                            );
                            case METHOD -> Stream.of(
                                    ((MethodDeclaration) instruction.detail()).declaringType(),
                                    ((MethodDeclaration) instruction.detail()).methodReturn().typeIdentifier()
                            );
                            case CLASS参照 -> Stream.of((TypeIdentifier) instruction.detail());
                            default -> Stream.empty();
                        }
                )
                .collect(Collectors.toSet());
    }
}
