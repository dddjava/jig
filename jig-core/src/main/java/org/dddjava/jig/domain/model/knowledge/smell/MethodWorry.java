package org.dddjava.jig.domain.model.knowledge.smell;

import org.dddjava.jig.domain.model.data.members.instruction.BasicInstruction;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;

/**
 * メソッドの気になるところ
 */
public enum MethodWorry {
    メンバを使用していない {
        @Override
        boolean judge(JigMethod jigMethod, JigType contextJigType) {
            if (jigMethod.isAbstract()) {
                return false;
            }
            var instructions = jigMethod.instructions();
            if (instructions.fieldReferenceStream()
                    .anyMatch(fieldReference -> fieldReference.declaringTypeIdentifier().equals(contextJigType.id()))) {
                return false;
            }
            if (instructions.methodCallStream()
                    .filter(methodCall -> !methodCall.isLambda()) // lambdaの合成メソッドのownerは自身になるので除外しないと「使用している」になってしまう
                    .anyMatch(methodCall -> methodCall.methodOwner().equals(contextJigType.id()))) {
                return false;
            }
            // lambdaの中からも自身のメンバにアクセスしていない
            return instructions.invokeDynamicInstructionStream().noneMatch(invokeDynamicInstruction ->
                    // TODO invokeDynamicはLambdaの中を見ないと正しい判断はできないが、とりあえずusingで代用しておく。
                    invokeDynamicInstruction.streamAssociatedTypes().anyMatch(contextJigType.id()::equals)
            );
        }
    },
    基本型の授受を行なっている {
        @Override
        boolean judge(JigMethod jigMethod) {
            return jigMethod.methodReturnTypeReference().id().isPrimitive()
                    || jigMethod.methodArgumentTypeReferenceStream()
                    .anyMatch(jigTypeReference -> jigTypeReference.id().isPrimitive());
        }
    },
    NULLリテラルを使用している {
        @Override
        boolean judge(JigMethod jigMethod) {
            return jigMethod.instructions().containsAll(BasicInstruction.NULL参照);
        }
    },
    NULL判定をしている {
        @Override
        boolean judge(JigMethod jigMethod) {
            return jigMethod.instructions().containsAll(BasicInstruction.NULL判定);
        }
    },
    真偽値を返している {
        @Override
        boolean judge(JigMethod method) {
            return method.methodReturnTypeReference().id().isBoolean();
        }
    },
    voidを返している {
        @Override
        boolean judge(JigMethod jigMethod) {
            return jigMethod.methodReturnTypeReference().id().isVoid();
        }
    };

    boolean judge(JigMethod method) {
        return false;
    }

    boolean judge(JigMethod method, JigType contextJigType) {
        return judge(method);
    }
}
