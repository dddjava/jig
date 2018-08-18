package org.dddjava.jig.presentation.view.report.domain;

import org.dddjava.jig.domain.model.decisions.StringComparingAngle;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("文字列比較箇所")
public class StringComparingAngleAdapter {

    StringComparingAngle angle;

    public StringComparingAngleAdapter(StringComparingAngle angle) {
        this.angle = angle;
    }

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.メソッドシグネチャ)
    public MethodDeclaration methodDeclaration() {
        return angle.methodDeclaration();
    }
}
