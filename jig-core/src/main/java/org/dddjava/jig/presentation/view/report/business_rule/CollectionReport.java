package org.dddjava.jig.presentation.view.report.business_rule;

import org.dddjava.jig.domain.model.jigdocumenter.collections.CollectionAngle;
import org.dddjava.jig.domain.model.jigdocumenter.collections.CollectionField;
import org.dddjava.jig.domain.model.jigmodel.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.jigmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.declaration.type.TypeIdentifiers;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("COLLECTION")
public class CollectionReport {

    CollectionAngle angle;

    public CollectionReport(CollectionAngle angle) {
        this.angle = angle;
    }

    @ReportItemFor(ReportItem.パッケージ名)
    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス別名)
    public TypeIdentifier typeIdentifier() {
        return angle.typeIdentifier();
    }

    @ReportItemFor(ReportItem.使用箇所数)
    @ReportItemFor(ReportItem.使用箇所)
    public TypeIdentifiers userTypeIdentifiers() {
        return angle.userTypeIdentifiers();
    }

    @ReportItemFor(ReportItem.フィールドの型)
    public CollectionField field() {
        return angle.field();
    }

    @ReportItemFor(ReportItem.メソッド数)
    @ReportItemFor(ReportItem.メソッド一覧)
    public MethodDeclarations methods() {
        return angle.methods();
    }
}
