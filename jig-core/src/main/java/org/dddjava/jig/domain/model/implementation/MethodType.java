package org.dddjava.jig.domain.model.implementation;

public enum MethodType {
    CONSTRUCTOR {
        @Override
        public void bind(MethodSpecification methodSpecification, Specification specification) {
            specification.registerConstructorSpecification(methodSpecification);
        }
    },
    STATIC_METHOD {
        @Override
        public void bind(MethodSpecification methodSpecification, Specification specification) {
            specification.registerStaticMethodSpecification(methodSpecification);
        }
    },
    INSTANCE_METHOD {
        @Override
        public void bind(MethodSpecification methodSpecification, Specification specification) {
            specification.registerInstanceMethodSpecification(methodSpecification);
        }
    };

    public static MethodType methodType(MethodSpecification methodSpecification) {
        if (methodSpecification.methodDeclaration.isConstructor()) {
            return CONSTRUCTOR;
        }
        if (methodSpecification.isStatic()) {
            return STATIC_METHOD;
        }
        return INSTANCE_METHOD;
    }

    public abstract void bind(MethodSpecification methodSpecification, Specification specification);
}
