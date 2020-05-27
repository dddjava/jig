package org.dddjava.jig.domain.model.jigloaded.richmethod;

import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.declaration.method.Accessor;
import org.dddjava.jig.domain.model.declaration.method.DecisionNumber;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.jigloaded.relation.method.MethodDepend;
import org.dddjava.jig.domain.model.jigloaded.relation.method.UsingFields;
import org.dddjava.jig.domain.model.jigloaded.relation.method.UsingMethods;

/**
 * メソッド
 */
public class Method {

    boolean nullDecision;
    boolean referenceNull;

    MethodDeclaration methodDeclaration;
    DecisionNumber decisionNumber;
    MethodAnnotations methodAnnotations;
    Accessor accessor;

    MethodDepend methodDepend;

    public Method(MethodDeclaration methodDeclaration, boolean nullDecision, boolean referenceNull, DecisionNumber decisionNumber, MethodAnnotations methodAnnotations, Accessor accessor, MethodDepend methodDepend) {
        this.methodDeclaration = methodDeclaration;
        this.nullDecision = nullDecision;
        this.referenceNull = referenceNull;
        this.decisionNumber = decisionNumber;
        this.methodAnnotations = methodAnnotations;
        this.accessor = accessor;
        this.methodDepend = methodDepend;
    }

    public MethodDeclaration declaration() {
        return methodDeclaration;
    }

    public DecisionNumber decisionNumber() {
        return decisionNumber;
    }

    public MethodAnnotations methodAnnotations() {
        return methodAnnotations;
    }

    public boolean isPublic() {
        return accessor.isPublic();
    }

    public UsingFields usingFields() {
        return methodDepend.usingFields();
    }

    public UsingMethods usingMethods() {
        return methodDepend.usingMethods();
    }

    public MethodWorries methodWorries() {
        return new MethodWorries(this);
    }

    public boolean conditionalNull() {
        return nullDecision;
    }

    public boolean referenceNull() {
        return referenceNull;
    }

    public boolean notUseMember() {
        return usingFields().empty() && usingMethods().empty();
    }
}
