package org.dddjava.jig.domain.model.booleans.model;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.report.ReportItem;
import org.dddjava.jig.domain.model.report.ReportItemFor;

/**
 * 真偽値を返すモデルの切り口
 */
public class BoolQueryAngle {
    MethodDeclaration method;
    MethodDeclarations usages;

    public BoolQueryAngle(MethodDeclaration method, MethodDeclarations usages) {
        this.method = method;
        this.usages = usages;
    }

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス和名)
    @ReportItemFor(ReportItem.メソッド名)
    @ReportItemFor(ReportItem.メソッド和名)
    public MethodDeclaration method() {
        return method;
    }

    @ReportItemFor(ReportItem.使用箇所数)
    @ReportItemFor(ReportItem.使用箇所)
    public TypeIdentifiers userTypeIdentifiers() {
        return usages.declaringTypes();
    }
}
