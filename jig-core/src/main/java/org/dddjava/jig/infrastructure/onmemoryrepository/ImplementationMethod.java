package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;

public class ImplementationMethod {

    MethodDeclaration implementationMethod;
    MethodDeclaration interfaceMethod;

    public ImplementationMethod(MethodDeclaration implementationMethod, MethodDeclaration interfaceMethod) {
        this.implementationMethod = implementationMethod;
        this.interfaceMethod = interfaceMethod;
    }

    public boolean interfaceMethodIs(MethodDeclaration methodDeclaration) {
        return interfaceMethod.equals(methodDeclaration);
    }

    public MethodDeclaration implementationMethod() {
        return implementationMethod;
    }
}
