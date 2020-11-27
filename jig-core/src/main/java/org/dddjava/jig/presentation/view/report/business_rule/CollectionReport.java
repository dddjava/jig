package org.dddjava.jig.presentation.view.report.business_rule;

import org.dddjava.jig.domain.model.jigmodel.collections.CollectionAngle;
import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigType;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifiers;
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
    @ReportItemFor(ReportItem.フィールドの型)
    @ReportItemFor(ReportItem.メソッド数)
    @ReportItemFor(ReportItem.メソッド一覧)
    public JigType jigType() {
        return angle.jigType();
    }

    @ReportItemFor(ReportItem.使用箇所数)
    @ReportItemFor(ReportItem.使用箇所)
    public TypeIdentifiers userTypeIdentifiers() {
        return angle.userTypeIdentifiers();
    }
}
