package org.dddjava.jig.domain.model.implementation.relation;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;

/**
 * インタフェースを実装しているメソッド
 */
public class ImplementationMethod {

    MethodDeclaration implementationMethod;
    MethodDeclaration interfaceMethod;

    public ImplementationMethod(MethodDeclaration implementationMethod, MethodDeclaration interfaceMethod) {
        this.implementationMethod = implementationMethod;
        this.interfaceMethod = interfaceMethod;
    }

    public boolean interfaceMethodIs(MethodDeclaration methodDeclaration) {
        return interfaceMethod.sameIdentifier(methodDeclaration);
    }

    public MethodDeclaration implementationMethod() {
        return implementationMethod;
    }
}
