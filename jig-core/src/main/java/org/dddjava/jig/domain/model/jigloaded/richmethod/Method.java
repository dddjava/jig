package org.dddjava.jig.domain.model.jigloaded.richmethod;

import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.declaration.method.Accessor;
import org.dddjava.jig.domain.model.declaration.method.DecisionNumber;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.jigsource.bytecode.MethodByteCode;

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
    UsingFields usingFields;
    UsingMethods usingMethods;
    MethodWorries methodWorries;

    public Method(MethodByteCode methodByteCode) {
        this.methodDeclaration = methodByteCode.methodDeclaration();
        this.nullDecision = methodByteCode.judgeNull();
        this.referenceNull = methodByteCode.referenceNull();
        this.decisionNumber = methodByteCode.decisionNumber();
        this.methodAnnotations = methodByteCode.annotatedMethods();
        this.accessor = methodByteCode.accessor();
        this.usingFields = new UsingFields(methodByteCode.usingFields().list());
        this.usingMethods = new UsingMethods(methodByteCode.usingMethods());
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
        return usingFields;
    }

    public UsingMethods usingMethods() {
        return usingMethods;
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
