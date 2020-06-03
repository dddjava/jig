package org.dddjava.jig.domain.model.jigmodel.relation.method;

import org.dddjava.jig.domain.model.jigmodel.declaration.method.MethodDeclaration;

public class CalleeMethod {
    MethodDeclaration methodDeclaration;

    public CalleeMethod(MethodDeclaration methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
    }
}
