package org.dddjava.jig.domain.model.knowledge.smell;

import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.information.method.JigMethod;
import org.dddjava.jig.domain.model.information.type.JigType;

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
            return instructions.values().stream()
                    .noneMatch(instruction -> instruction.instructMethodOrFieldOwnerIs(contextJigType.typeIdentifier()));
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

    boolean judge(JigMethod method) {
        return false;
    }

    boolean judge(JigMethod method, JigType contextJigType) {
        return judge(method);
    }
}
