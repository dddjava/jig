package org.dddjava.jig.domain.model.decisions;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.report.ReportItem;
import org.dddjava.jig.domain.model.report.ReportItemFor;

public class StringComparingAngle {

    MethodDeclaration methodDeclaration;

    public StringComparingAngle(MethodDeclaration methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
    }

    @ReportItemFor(ReportItem.クラス名)
    public TypeIdentifier declaringType() {
        return methodDeclaration.declaringType();
    }

    @ReportItemFor(ReportItem.メソッドシグネチャ)
    public MethodDeclaration methodDeclaration() {
        return methodDeclaration;
    }
}
