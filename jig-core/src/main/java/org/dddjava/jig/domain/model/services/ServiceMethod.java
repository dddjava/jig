package org.dddjava.jig.domain.model.services;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.richmethod.Method;
import org.dddjava.jig.domain.model.richmethod.MethodWorries;
import org.dddjava.jig.domain.model.richmethod.UsingFields;
import org.dddjava.jig.domain.model.richmethod.UsingMethods;

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

    public MethodWorries methodWorries() {
        return method.methodWorries();
    }
}
