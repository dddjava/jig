package org.dddjava.jig.domain.model.models.jigobject.member;

import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

/**
 * メソッドの気になるところ
 */
public enum MethodWorry {
    メンバを使用していない {
        @Override
        boolean judge(JigMethod jigMethod) {
            return jigMethod.notUseMember();
        }
    },
    基本型の授受を行なっている {
        @Override
        boolean judge(JigMethod jigMethod) {
            return jigMethod.declaration().methodReturn().isPrimitive()
                    || jigMethod.declaration().methodSignature().listArgumentTypeIdentifiers().stream().anyMatch(TypeIdentifier::isPrimitive);
        }
    },
    NULLリテラルを使用している {
        @Override
        boolean judge(JigMethod jigMethod) {
            return jigMethod.referenceNull();
        }
    },
    NULL判定をしている {
        @Override
        boolean judge(JigMethod jigMethod) {
            return jigMethod.conditionalNull();
        }
    },
    真偽値を返している {
        @Override
        boolean judge(JigMethod method) {
            return method.declaration().methodReturn().typeIdentifier().isBoolean();
        }
    },
    StreamAPIを使用している {
        @Override
        boolean judge(JigMethod jigMethod) {
            return jigMethod.usingMethods().containsStream();
        }
    },
    voidを返している {
        @Override
        boolean judge(JigMethod jigMethod) {
            return jigMethod.declaration().methodReturn().isVoid();
        }
    };

    abstract boolean judge(JigMethod method);
}
