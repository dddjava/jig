package org.dddjava.jig.domain.model.implementation.bytecode;

/**
 * メソッドの種類
 */
public enum MethodKind {
    CONSTRUCTOR {
        @Override
        public void bind(MethodImplementation methodImplementation, ByteCode byteCode) {
            byteCode.registerConstructorSpecification(methodImplementation);
        }
    },
    STATIC_METHOD {
        @Override
        public void bind(MethodImplementation methodImplementation, ByteCode byteCode) {
            byteCode.registerStaticMethodSpecification(methodImplementation);
        }
    },
    INSTANCE_METHOD {
        @Override
        public void bind(MethodImplementation methodImplementation, ByteCode byteCode) {
            byteCode.registerInstanceMethodSpecification(methodImplementation);
        }
    };

    public static MethodKind methodKind(MethodImplementation methodImplementation) {
        if (methodImplementation.methodDeclaration.isConstructor()) {
            return CONSTRUCTOR;
        }
        if (methodImplementation.isStatic()) {
            return STATIC_METHOD;
        }
        return INSTANCE_METHOD;
    }

    public abstract void bind(MethodImplementation methodImplementation, ByteCode byteCode);
}
