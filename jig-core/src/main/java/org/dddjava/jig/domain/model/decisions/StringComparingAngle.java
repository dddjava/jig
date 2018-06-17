package org.dddjava.jig.domain.model.decisions;

import org.dddjava.jig.domain.basic.ReportItem;
import org.dddjava.jig.domain.basic.ReportItemFor;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

public class StringComparingAngle {

    MethodDeclaration methodDeclaration;

    public StringComparingAngle(MethodDeclaration methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
    }

    @ReportItemFor(ReportItem.クラス名)
    public TypeIdentifier declaringType() {
        return methodDeclaration.declaringType();
    }

    @ReportItemFor(ReportItem.メソッド名)
    public MethodDeclaration methodDeclaration() {
        return methodDeclaration;
    }
}
