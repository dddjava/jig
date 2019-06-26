package org.dddjava.jig.presentation.view.report.business_rule;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.method.CallerMethods;
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
    @ReportItemFor(ReportItem.クラス別名)
    @ReportItemFor(ReportItem.メソッドシグネチャ)
    @ReportItemFor(ReportItem.メソッド戻り値の型)
    public MethodDeclaration methodDeclaration() {
        return angle.methodDeclaration();
    }

    @ReportItemFor(ReportItem.使用箇所数)
    public CallerMethods toMeRelation() {
        return angle.callerMethods();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "フィールド未使用", order = 1)
    public boolean notUseField() {
        return angle.notUseField();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "基本型の授受", order = 2)
    public boolean primitiveInterface() {
        return angle.primitiveInterface();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "真偽値の返却", order = 3)
    public boolean returnsBoolean() {
        return angle.returnsBoolean();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "nullリテラルの使用", order = 4)
    public boolean referenceNull() {
        return angle.referenceNull();
    }
    @ReportItemFor(value = ReportItem.汎用真偽値, label = "nullによる判定", order = 5)
    public boolean nullDecision() {
        return angle.nullDecision();
    }
}
