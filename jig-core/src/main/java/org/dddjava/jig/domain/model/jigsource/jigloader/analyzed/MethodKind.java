package org.dddjava.jig.domain.model.jigsource.jigloader.analyzed;

/**
 * メソッドの種類
 */
public enum MethodKind {
    CONSTRUCTOR {
        @Override
        public void bind(MethodFact methodFact, TypeFact typeFact) {
            typeFact.registerConstructorFacts(methodFact);
        }
    },
    STATIC_METHOD {
        @Override
        public void bind(MethodFact methodFact, TypeFact typeFact) {
            typeFact.registerStaticMethodFacts(methodFact);
        }
    },
    INSTANCE_METHOD {
        @Override
        public void bind(MethodFact methodFact, TypeFact typeFact) {
            typeFact.registerInstanceMethodFacts(methodFact);
        }
    };

    public abstract void bind(MethodFact methodFact, TypeFact typeFact);
}
