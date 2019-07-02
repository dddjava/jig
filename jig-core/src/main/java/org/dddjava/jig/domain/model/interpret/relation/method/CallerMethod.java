package org.dddjava.jig.domain.model.interpret.relation.method;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;

/**
 * 呼び出し元メソッド
 */
public class CallerMethod {
    MethodDeclaration methodDeclaration;

    public CallerMethod(MethodDeclaration methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
    }

    public MethodDeclaration methodDeclaration() {
        return methodDeclaration;
    }
}
