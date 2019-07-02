package org.dddjava.jig.domain.model.interpret.relation.method;

/**
 * メソッドの使用しているメソッド
 */
public class MethodRelation {
    CallerMethod from;
    CalleeMethod to;

    public MethodRelation(CallerMethod from, CalleeMethod to) {
        this.from = from;
        this.to = to;
    }

    CallerMethod from() {
        return from;
    }

    public boolean calleeMethodIs(CalleeMethod calleeMethod) {
        return to.methodDeclaration.sameIdentifier(calleeMethod.methodDeclaration);
    }
}
