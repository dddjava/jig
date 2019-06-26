package org.dddjava.jig.domain.model.implementation.analyzed.unit.method;

import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.MethodByteCode;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;

/**
 * メソッドの気になるところ
 */
public enum MethodWorry {
    メンバを使用していない {
        @Override
        boolean judge(MethodByteCode methodByteCode) {
            return false;
        }
    },
    基本型の授受を行なっている {
        @Override
        boolean judge(MethodByteCode methodByteCode) {
            MethodDeclaration methodDeclaration = methodByteCode.methodDeclaration();
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
        boolean judge(MethodByteCode methodByteCode) {
            return methodByteCode.methodDeclaration().methodReturn().typeIdentifier().isBoolean();
        }
    },
    StreamAPIを使用している {
        @Override
        boolean judge(MethodByteCode methodByteCode) {
            UsingMethods usingMethods = new UsingMethods(methodByteCode.usingMethods());
            return usingMethods.containsStream();
        }
    };

    abstract boolean judge(MethodByteCode methodByteCode);
}
