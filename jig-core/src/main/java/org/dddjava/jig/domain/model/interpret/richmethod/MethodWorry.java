package org.dddjava.jig.domain.model.interpret.richmethod;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodReturn;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodByteCode;

/**
 * メソッドの気になるところ
 */
public enum MethodWorry {
    メンバを使用していない {
        @Override
        boolean judge(MethodByteCode methodByteCode) {
            // TODO 自インスタンスに対するメソッドやフィールドのインタラクションが存在しなければtrue
            return false;
        }
    },
    基本型の授受を行なっている {
        @Override
        boolean judgeDeclaration(MethodDeclaration methodDeclaration) {
            return methodDeclaration.methodReturn().isPrimitive()
                    || methodDeclaration.methodSignature().arguments().stream().anyMatch(TypeIdentifier::isPrimitive);
        }
    },
    NULLリテラルを使用している {
        @Override
        boolean judge(MethodByteCode methodByteCode) {
            return methodByteCode.referenceNull();
        }
    },
    NULL判定をしている {
        @Override
        boolean judge(MethodByteCode methodByteCode) {
            return methodByteCode.judgeNull();
        }
    },
    真偽値を返している {
        @Override
        boolean judgeDeclaration(MethodDeclaration methodDeclaration) {
            return methodDeclaration.methodReturn().typeIdentifier().isBoolean();
        }
    },
    StreamAPIを使用している {
        @Override
        boolean judge(MethodByteCode methodByteCode) {
            UsingMethods usingMethods = new UsingMethods(methodByteCode.usingMethods());
            return usingMethods.containsStream();
        }
    },
    voidを返している {
        @Override
        boolean judgeMethodReturn(MethodReturn methodReturn) {
            return methodReturn.isVoid();
        }
    }
    ;

    boolean judge(MethodByteCode methodByteCode) {
        return judgeDeclaration(methodByteCode.methodDeclaration());
    }

    boolean judgeDeclaration(MethodDeclaration methodDeclaration) {
        return judgeMethodReturn(methodDeclaration.methodReturn());
    }

    boolean judgeMethodReturn(MethodReturn methodReturn) {
        return false;
    }
}
