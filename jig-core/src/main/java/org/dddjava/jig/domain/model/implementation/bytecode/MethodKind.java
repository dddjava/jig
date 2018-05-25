package org.dddjava.jig.domain.model.implementation.bytecode;

/**
 * メソッドの種類
 */
public enum MethodKind {
    CONSTRUCTOR {
        @Override
        public void bind(MethodByteCode methodByteCode, ByteCode byteCode) {
            byteCode.registerConstructorSpecification(methodByteCode);
        }
    },
    STATIC_METHOD {
        @Override
        public void bind(MethodByteCode methodByteCode, ByteCode byteCode) {
            byteCode.registerStaticMethodSpecification(methodByteCode);
        }
    },
    INSTANCE_METHOD {
        @Override
        public void bind(MethodByteCode methodByteCode, ByteCode byteCode) {
            byteCode.registerInstanceMethodSpecification(methodByteCode);
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

    public abstract void bind(MethodByteCode methodByteCode, ByteCode byteCode);
}
