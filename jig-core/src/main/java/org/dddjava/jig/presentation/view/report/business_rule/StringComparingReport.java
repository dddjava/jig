package org.dddjava.jig.presentation.view.report.business_rule;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.fact.relation.method.CallerMethod;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("文字列比較箇所")
public class StringComparingReport {

    CallerMethod callerMethod;

    public StringComparingReport(CallerMethod callerMethod) {
        this.callerMethod = callerMethod;
    }

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.メソッドシグネチャ)
    public MethodDeclaration methodDeclaration() {
        return callerMethod.methodDeclaration();
    }
}
