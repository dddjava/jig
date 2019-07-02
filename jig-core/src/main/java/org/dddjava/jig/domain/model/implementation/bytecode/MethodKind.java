package org.dddjava.jig.domain.model.implementation.bytecode;

/**
 * メソッドの種類
 */
public enum MethodKind {
    CONSTRUCTOR {
        @Override
        public void bind(MethodByteCode methodByteCode, TypeByteCode typeByteCode) {
            typeByteCode.registerConstructorByteCodes(methodByteCode);
        }
    },
    STATIC_METHOD {
        @Override
        public void bind(MethodByteCode methodByteCode, TypeByteCode typeByteCode) {
            typeByteCode.registerStaticMethodByteCodes(methodByteCode);
        }
    },
    INSTANCE_METHOD {
        @Override
        public void bind(MethodByteCode methodByteCode, TypeByteCode typeByteCode) {
            typeByteCode.registerInstanceMethodByteCodes(methodByteCode);
        }
    };

    public static MethodKind methodKind(MethodByteCode methodByteCode) {
        if (methodByteCode.methodDeclaration.isConstructor()) {
            return CONSTRUCTOR;
        }
        if (methodByteCode.isStatic()) {
            return STATIC_METHOD;
        }
        return INSTANCE_METHOD;
    }

    public abstract void bind(MethodByteCode methodByteCode, TypeByteCode typeByteCode);
}
