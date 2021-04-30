package org.dddjava.jig.presentation.view.report.business_rule;

import org.dddjava.jig.domain.model.jigdocument.implementation.MethodSmell;
import org.dddjava.jig.domain.model.jigmodel.jigtype.member.MethodWorry;
import org.dddjava.jig.domain.model.parts.class_.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.relation.method.CallerMethods;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportMethodWorryOf;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("注意メソッド")
public class MethodSmellReport {

    MethodSmell angle;

    public MethodSmellReport(MethodSmell angle) {
        this.angle = angle;
    }

    @ReportItemFor(ReportItem.パッケージ名)
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

    @ReportMethodWorryOf(MethodWorry.メンバを使用していない)
    public boolean notUseMember() {
        return angle.notUseMember();
    }

    @ReportMethodWorryOf(MethodWorry.基本型の授受を行なっている)
    public boolean primitiveInterface() {
        return angle.primitiveInterface();
    }

    @ReportMethodWorryOf(MethodWorry.真偽値を返している)
    public boolean returnsBoolean() {
        return angle.returnsBoolean();
    }

    @ReportMethodWorryOf(MethodWorry.voidを返している)
    public boolean returnsVoid() {
        return angle.returnsVoid();
    }

    @ReportMethodWorryOf(MethodWorry.NULLリテラルを使用している)
    public boolean referenceNull() {
        return angle.referenceNull();
    }

    @ReportMethodWorryOf(MethodWorry.NULL判定をしている)
    public boolean nullDecision() {
        return angle.nullDecision();
    }
}
