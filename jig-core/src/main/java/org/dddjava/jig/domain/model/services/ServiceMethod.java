package org.dddjava.jig.domain.model.services;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.unit.method.Method;

public class ServiceMethod {
    private final Method method;

    public ServiceMethod(Method method) {
        this.method = method;
    }

    public MethodDeclaration methodDeclaration() {
        return method.declaration();
    }
}
