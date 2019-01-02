package org.dddjava.jig.domain.model.services;

import org.dddjava.jig.domain.model.implementation.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.implementation.unit.method.Method;
import org.dddjava.jig.domain.model.implementation.unit.method.UsingFields;
import org.dddjava.jig.domain.model.implementation.unit.method.UsingMethods;

/**
 * サービスメソッド
 */
public class ServiceMethod {
    private final Method method;

    public ServiceMethod(Method method) {
        this.method = method;
    }

    public MethodDeclaration methodDeclaration() {
        return method.declaration();
    }

    public boolean isPublic() {
        return method.isPublic();
    }

    public UsingFields methodUsingFields() {
        return method.usingFields();
    }

    public UsingMethods usingMethods() {
        return method.usingMethods();
    }
}
