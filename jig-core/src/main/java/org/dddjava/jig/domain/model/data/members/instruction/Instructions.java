package org.dddjava.jig.domain.model.data.members.instruction;

import org.dddjava.jig.domain.model.data.members.JigFieldIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * メソッドで実施されている命令
 *
 * バイトコード上の順番を維持するため、Listで保持する
 */
public record Instructions(List<Instruction> instructions) {

    public List<InvokedMethod> instructMethods() {
        return instructions.stream()
                .flatMap(instruction -> {
                    if (instruction instanceof InvokedMethod invokedMethod) {
                        return Stream.of(invokedMethod);
                    }
                    if (instruction instanceof InvokeDynamicInstruction invokeDynamicInstruction) {
                        return Stream.of(invokeDynamicInstruction.invokedMethod());
                    }
                    return Stream.empty();
                })
                .toList();
    }

    private Stream<Instruction> simpleInstructionStream() {
        return instructions.stream()
                .filter(instruction -> instruction instanceof SimpleInstruction);
    }

    public boolean hasNullDecision() {
        return simpleInstructionStream().anyMatch(instruction -> instruction == SimpleInstruction.NULL判定);
    }

    public DecisionNumber decisionNumber() {
        var count = simpleInstructionStream()
                .filter(instruction -> instruction == SimpleInstruction.JUMP || instruction == SimpleInstruction.SWITCH)
                .count();
        return new DecisionNumber(Math.toIntExact(count));
    }

    public boolean hasNullReference() {
        return simpleInstructionStream().anyMatch(instruction -> instruction == SimpleInstruction.NULL参照);
    }

    public Stream<TypeIdentifier> associatedTypeStream() {
        return instructions.stream()
                .flatMap(instruction -> {
                    if (instruction instanceof ClassReference classReference) {
                        return Stream.of(classReference.typeIdentifier());
                    }
                    if (instruction instanceof FieldInstruction fieldInstruction) {
                        return Stream.of(fieldInstruction.jigFieldIdentifier().declaringTypeIdentifier());
                    }
                    if (instruction instanceof InvokedMethod invokedMethod) {
                        return Stream.concat(invokedMethod.extractTypeIdentifiers().stream(), Stream.of(invokedMethod.returnType()));
                    }
                    if (instruction instanceof InvokeDynamicInstruction invokeDynamicInstruction) {
                        return invokeDynamicInstruction.usingTypes();
                    }
                    return Stream.empty();
                });
    }

    public Collection<JigFieldIdentifier> fieldReferences() {
        return fieldReferenceStream().toList();
    }

    public Stream<JigFieldIdentifier> fieldReferenceStream() {
        return instructions.stream()
                .filter(instruction -> instruction instanceof FieldInstruction)
                .map(instruction -> (FieldInstruction) instruction)
                .map(FieldInstruction::jigFieldIdentifier);
    }

    public Stream<ClassReference> classReferenceStream() {
        return instructions.stream()
                .filter(instruction -> instruction instanceof ClassReference)
                .map(instruction -> (ClassReference) instruction);
    }

    public Stream<InvokedMethod> invokedMethodStream() {
        return instructions.stream()
                .filter(instruction -> instruction instanceof InvokedMethod)
                .map(instruction -> (InvokedMethod) instruction);
    }

    public Stream<InvokeDynamicInstruction> invokeDynamicInstructionStream() {
        return instructions.stream()
                .filter(instruction -> instruction instanceof InvokeDynamicInstruction)
                .map(instruction -> (InvokeDynamicInstruction) instruction);
    }
}
