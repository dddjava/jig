package org.dddjava.jig.domain.model.data.classes.method.instruction;

import org.dddjava.jig.domain.model.data.classes.field.FieldDeclaration;
import org.dddjava.jig.domain.model.data.classes.field.FieldDeclarations;
import org.dddjava.jig.domain.model.data.classes.field.FieldType;
import org.dddjava.jig.domain.model.data.classes.method.*;
import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * メソッドで実施されている命令
 * <p>
 * バイトコード上の順番を維持する
 */
public record Instructions(List<Instruction> values) {

    public static Instructions newInstance() {
        return new Instructions(new ArrayList<>());
    }

    public void register(MethodInstructionType type) {
        values.add(new Instruction(type, null));
    }

    public void registerField(TypeIdentifier declaringType, TypeIdentifier fieldTypeIdentifier, String name) {
        values.add(new Instruction(MethodInstructionType.FIELD, new FieldReference(declaringType, fieldTypeIdentifier, name)));
    }

    public void registerMethod(InvokedMethod invokedMethod) {
        values.add(new Instruction(MethodInstructionType.METHOD, invokedMethod));
    }

    public void registerClassReference(TypeIdentifier typeIdentifier) {
        values.add(new Instruction(MethodInstructionType.CLASS参照, typeIdentifier));
    }

    public void registerInvokeDynamic(InvokeDynamicInstruction invokeDynamicInstruction) {
        values.add(new Instruction(MethodInstructionType.InvokeDynamic, invokeDynamicInstruction));
    }

    public MethodDeclarations instructMethods() {
        return filterType(MethodInstructionType.METHOD, MethodInstructionType.InvokeDynamic)
                .map(instruction -> {
                    if (instruction.detail() instanceof InvokedMethod invokedMethod) {
                        return invokedMethod;
                    }
                    if (instruction.detail() instanceof InvokeDynamicInstruction invokeDynamicInstruction) {
                        return invokeDynamicInstruction.invokedMethod();
                    }
                    throw new IllegalStateException();
                })
                .map(invokedMethod -> {
                    // 一旦変換しておく
                    return new MethodDeclaration(invokedMethod.methodOwner(),
                            new MethodSignature(invokedMethod.methodName(), invokedMethod.argumentTypes().toArray(TypeIdentifier[]::new)),
                            new MethodReturn(new ParameterizedType(invokedMethod.returnType())));
                })
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
                            case METHOD -> ((InvokedMethod) instruction.detail()).extractTypeIdentifiers().stream();
                            case CLASS参照 -> Stream.of((TypeIdentifier) instruction.detail());
                            case InvokeDynamic -> ((InvokeDynamicInstruction) instruction.detail()).usingTypes();
                            default -> Stream.empty();
                        }
                )
                .collect(Collectors.toSet());
    }

    public FieldDeclarations fieldReferences() {
        return new FieldDeclarations(filterType(MethodInstructionType.FIELD)
                .map(instruction -> ((FieldReference) instruction.detail()))
                .map(fieldReference -> new FieldDeclaration(
                        fieldReference.declaringType(),
                        new FieldType(fieldReference.fieldTypeIdentifier()),
                        fieldReference.name()
                ))
                .toList());
    }

    private Stream<Instruction> filterType(MethodInstructionType... methodInstructionTypes) {
        return values.stream().filter(instruction -> Set.of(methodInstructionTypes).contains(instruction.type()));
    }

}
