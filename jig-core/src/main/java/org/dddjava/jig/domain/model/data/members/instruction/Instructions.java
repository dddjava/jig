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

    public List<MethodCall> instructMethods() {
        return instructions.stream()
                .flatMap(instruction -> {
                    if (instruction instanceof MethodCall methodCall) {
                        return Stream.of(methodCall);
                    }
                    if (instruction instanceof DynamicMethodCall dynamicMethodCall) {
                        return Stream.of(dynamicMethodCall.methodCall());
                    }
                    return Stream.empty();
                })
                .toList();
    }

    private Stream<Instruction> simpleInstructionStream() {
        return instructions.stream()
                .filter(instruction -> instruction instanceof BasicInstruction);
    }

    public boolean hasNullDecision() {
        return simpleInstructionStream().anyMatch(instruction -> instruction == BasicInstruction.NULL判定);
    }

    public DecisionNumber decisionNumber() {
        var count = simpleInstructionStream()
                .filter(instruction -> instruction == BasicInstruction.JUMP || instruction == BasicInstruction.SWITCH)
                .count();
        return new DecisionNumber(Math.toIntExact(count));
    }

    public boolean hasNullReference() {
        return simpleInstructionStream().anyMatch(instruction -> instruction == BasicInstruction.NULL参照);
    }

    public Stream<TypeIdentifier> associatedTypeStream() {
        return instructions.stream()
                .flatMap(instruction -> {
                    if (instruction instanceof ClassReference classReference) {
                        return Stream.of(classReference.typeIdentifier());
                    }
                    if (instruction instanceof FieldAccess fieldAccess) {
                        return Stream.of(fieldAccess.jigFieldIdentifier().declaringTypeIdentifier());
                    }
                    if (instruction instanceof MethodCall methodCall) {
                        return Stream.concat(methodCall.extractTypeIdentifiers().stream(), Stream.of(methodCall.returnType()));
                    }
                    if (instruction instanceof DynamicMethodCall dynamicMethodCall) {
                        return dynamicMethodCall.usingTypes();
                    }
                    return Stream.empty();
                });
    }

    public Collection<JigFieldIdentifier> fieldReferences() {
        return fieldReferenceStream().toList();
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

    public Stream<MethodCall> invokedMethodStream() {
        return instructions.stream()
                .filter(instruction -> instruction instanceof MethodCall)
                .map(instruction -> (MethodCall) instruction);
    }

    public Stream<DynamicMethodCall> invokeDynamicInstructionStream() {
        return instructions.stream()
                .filter(instruction -> instruction instanceof DynamicMethodCall)
                .map(instruction -> (DynamicMethodCall) instruction);
    }
}
