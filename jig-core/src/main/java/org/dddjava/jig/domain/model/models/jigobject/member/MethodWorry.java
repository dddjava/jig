package org.dddjava.jig.domain.model.models.jigobject.member;

import org.dddjava.jig.domain.model.parts.class_.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.class_.method.MethodReturn;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;

/**
 * メソッドの気になるところ
 */
public enum MethodWorry {
    メンバを使用していない {
        @Override
        boolean judge(JigMethod method) {
            return method.notUseMember();
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
        boolean judge(JigMethod method) {
            return method.referenceNull();
        }
    },
    NULL判定をしている {
        @Override
        boolean judge(JigMethod method) {
            return method.conditionalNull();
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
        boolean judge(JigMethod method) {
            return method.usingMethods().containsStream();
        }
    },
    voidを返している {
        @Override
        boolean judgeMethodReturn(MethodReturn methodReturn) {
            return methodReturn.isVoid();
        }
    };

    boolean judge(JigMethod method) {
        return judgeDeclaration(method.declaration());
    }

    boolean judgeDeclaration(MethodDeclaration methodDeclaration) {
        return judgeMethodReturn(methodDeclaration.methodReturn());
    }

    boolean judgeMethodReturn(MethodReturn methodReturn) {
        return false;
    }
}
