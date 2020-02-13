package org.dddjava.jig.domain.model.jigmodel.applications.services;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.jigloaded.richmethod.Method;
import org.dddjava.jig.domain.model.jigloaded.richmethod.MethodWorries;
import org.dddjava.jig.domain.model.jigloaded.richmethod.UsingFields;
import org.dddjava.jig.domain.model.jigloaded.richmethod.UsingMethods;

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

    public Method method() {
        return method;
    }

    public boolean isCall(MethodDeclaration methodDeclaration) {
        return method.usingMethods().methodDeclarations().contains(methodDeclaration);
    }
}
