package org.dddjava.jig.domain.model.smells;

import org.dddjava.jig.domain.model.declaration.method.Method;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;
import org.dddjava.jig.domain.model.report.ReportItem;
import org.dddjava.jig.domain.model.report.ReportItemFor;

public class MethodSmellAngle {

    Method method;
    MethodUsingFields methodUsingFields;

    public MethodSmellAngle(Method method, MethodUsingFields methodUsingFields) {
        this.method = method;
        this.methodUsingFields = methodUsingFields;
    }

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス和名)
    public TypeIdentifier typeIdentifier() {
        return methodDeclaration().declaringType();
    }

    @ReportItemFor(ReportItem.メソッドシグネチャ)
    @ReportItemFor(ReportItem.メソッド戻り値の型)
    public MethodDeclaration methodDeclaration() {
        return method.declaration();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "条件分岐数")
    public String decisionNumber() {
        return method.decisionNumber().asText();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "フィールド未使用")
    public boolean notUseField() {
        return methodUsingFields.usingFieldTypeIdentifiers(method.declaration()).empty();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "基本型の授受")
    public boolean primitiveInterface() {
        return method.declaration().returnType().isPrimitive()
                || method.declaration().methodSignature().arguments().stream().anyMatch(TypeIdentifier::isPrimitive);
    }

    public boolean hasSmell() {
        return notUseField() || primitiveInterface() || method.decisionNumber().notZero();
    }
}
