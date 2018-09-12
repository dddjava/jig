package org.dddjava.jig.presentation.view.report.domain;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.smells.MethodSmellAngle;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("注意メソッド")
public class MethodSmellReport {

    MethodSmellAngle angle;

    public MethodSmellReport(MethodSmellAngle angle) {
        this.angle = angle;
    }

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス和名)
    @ReportItemFor(ReportItem.メソッドシグネチャ)
    @ReportItemFor(ReportItem.メソッド戻り値の型)
    public MethodDeclaration methodDeclaration() {
        return angle.methodDeclaration();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "条件分岐数", order = 1)
    public String decisionNumber() {
        return angle.decisionNumber();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "フィールド未使用", order = 2)
    public boolean notUseField() {
        return angle.notUseField();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "基本型の授受", order = 3)
    public boolean primitiveInterface() {
        return angle.primitiveInterface();
    }
}
