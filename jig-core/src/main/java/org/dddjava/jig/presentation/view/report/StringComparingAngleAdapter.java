package org.dddjava.jig.presentation.view.report;

import org.dddjava.jig.domain.model.decisions.StringComparingAngle;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.report.ReportItem;
import org.dddjava.jig.domain.model.report.ReportItemFor;

@ReportTitle("文字列比較箇所")
public class StringComparingAngleAdapter {

    @ReportItemFor(ReportItem.クラス名)
    public TypeIdentifier declaringType(StringComparingAngle angle) {
        return angle.declaringType();
    }

    @ReportItemFor(ReportItem.メソッドシグネチャ)
    public MethodDeclaration methodDeclaration(StringComparingAngle angle) {
        return angle.methodDeclaration();
    }
}
