package org.dddjava.jig.domain.model.data.classes.method.instruction;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

/**
 * メソッドで行われる命令
 *
 * バイトコードの全てを転写するのではなく、JIGとして意味のあるものを持つ
 */
public record Instruction(MethodInstructionType type, Object detail) {

    /**
     * 指定された型のメソッドやフィールドへのアクセスかを判定する
     */
    public boolean instructMethodOrFieldOwnerIs(TypeIdentifier typeIdentifier) {
        return switch (type) {
            case FIELD -> {
                if (detail instanceof FieldReference fieldReference) {
                    yield fieldReference.declaringType().equals(typeIdentifier);
                } else yield false; // ないはずだけど
            }
            case METHOD -> {
                if (detail instanceof InvokedMethod invokedMethod) {
                    yield invokedMethod.methodOwner().equals(typeIdentifier);
                } else yield false; // ないはずだけど
            }
            case InvokeDynamic -> {
                if (detail instanceof InvokeDynamicInstruction invokeDynamicInstruction) {
                    // TODO invokeDynamicはLambdaの中を見ないと正しい判断はできないが、とりあえずusingで代用しておく。
                    yield invokeDynamicInstruction.usingTypes().anyMatch(typeIdentifier::equals);
                } else yield false; // ないはずだけど
            }
            default -> false;
        };
    }
}
