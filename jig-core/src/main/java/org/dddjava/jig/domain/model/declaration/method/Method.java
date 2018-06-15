package org.dddjava.jig.domain.model.declaration.method;

/**
 * メソッド
 */
public class Method {

    MethodDeclaration methodDeclaration;
    DecisionNumber decisionNumber;

    public Method(MethodDeclaration methodDeclaration, DecisionNumber decisionNumber) {
        this.methodDeclaration = methodDeclaration;
        this.decisionNumber = decisionNumber;
    }

    public MethodDeclaration declaration() {
        return methodDeclaration;
    }

    public DecisionNumber decisionNumber() {
        return decisionNumber;
    }

    public boolean hasDecision() {
        return decisionNumber.notZero();
    }
}
