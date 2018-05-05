package org.dddjava.jig.domain.model.specification;

public enum MethodType {
    CONSTRUCTOR {
        @Override
        public void linkMethodSpecificationToSpecification(MethodSpecification methodSpecification, Specification specification) {
            specification.registerConstructorSpecification(methodSpecification);
        }
    },
    STATIC_METHOD {
        @Override
        public void linkMethodSpecificationToSpecification(MethodSpecification methodSpecification, Specification specification) {
            specification.registerStaticMethodSpecification(methodSpecification);
        }
    },
    INSTANCE_METHOD {
        @Override
        public void linkMethodSpecificationToSpecification(MethodSpecification methodSpecification, Specification specification) {
            specification.registerInstanceMethodSpecification(methodSpecification);
        }
    };

    public abstract void linkMethodSpecificationToSpecification(MethodSpecification methodSpecification, Specification specification);
}
