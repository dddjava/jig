package org.dddjava.jig.domain.model.knowledge.smell;

import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.information.jigobject.member.JigMethod;

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
                    || jigMethod.declaration().methodSignature().arguments().stream().anyMatch(ParameterizedType::isPrimitive);
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
    voidを返している {
        @Override
        boolean judge(JigMethod jigMethod) {
            return jigMethod.declaration().methodReturn().isVoid();
        }
    };

    abstract boolean judge(JigMethod method);
}
