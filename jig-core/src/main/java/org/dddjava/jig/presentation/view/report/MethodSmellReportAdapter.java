package org.dddjava.jig.presentation.view.report;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.smells.MethodSmellAngle;

@ReportTitle("注意メソッド")
public class MethodSmellReportAdapter {

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス和名)
    public TypeIdentifier typeIdentifier(MethodSmellAngle angle) {
        return angle.typeIdentifier();
    }

    @ReportItemFor(ReportItem.メソッドシグネチャ)
    @ReportItemFor(ReportItem.メソッド戻り値の型)
    public MethodDeclaration methodDeclaration(MethodSmellAngle angle) {
        return angle.methodDeclaration();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "条件分岐数", order = 1)
    public String decisionNumber(MethodSmellAngle angle) {
        return angle.decisionNumber();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "フィールド未使用", order = 2)
    public boolean notUseField(MethodSmellAngle angle) {
        return angle.notUseField();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "基本型の授受", order = 3)
    public boolean primitiveInterface(MethodSmellAngle angle) {
        return angle.primitiveInterface();
    }
}
