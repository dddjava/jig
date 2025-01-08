package org.dddjava.jig.domain.model.data.classes.method.instruction;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;

import java.util.ArrayList;
import java.util.List;

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
}
