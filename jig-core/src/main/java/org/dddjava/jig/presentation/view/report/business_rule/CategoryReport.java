package org.dddjava.jig.presentation.view.report.business_rule;

import org.dddjava.jig.domain.model.jigdocumenter.categories.CategoryAngle;
import org.dddjava.jig.domain.model.jigmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.declaration.type.TypeIdentifiers;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("ENUM")
public class CategoryReport {

    CategoryAngle angle;

    public CategoryReport(CategoryAngle angle) {
        this.angle = angle;
    }

    @ReportItemFor(ReportItem.パッケージ名)
    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス別名)
    public TypeIdentifier typeIdentifier() {
        return angle.typeIdentifier();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, order = 1, label = "定数宣言")
    public String constantsDeclarationsName() {
        return angle.constantsDeclarationsName();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, order = 2, label = "フィールド")
    public String fieldDeclarations() {
        return angle.fieldDeclarations();
    }

    @ReportItemFor(value = ReportItem.使用箇所数, order = 3)
    @ReportItemFor(value = ReportItem.使用箇所, order = 4)
    public TypeIdentifiers userTypeIdentifiers() {
        return angle.userTypeIdentifiers();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, order = 5, label = "パラメーター有り")
    public boolean hasParameter() {
        return angle.hasParameter();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, order = 6, label = "振る舞い有り")
    public boolean hasBehaviour() {
        return angle.hasBehaviour();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, order = 7, label = "多態")
    public boolean isPolymorphism() {
        return angle.isPolymorphism();
    }
}
