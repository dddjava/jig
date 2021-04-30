package org.dddjava.jig.presentation.view.report.business_rule;

import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigType;
import org.dddjava.jig.domain.model.parts.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.parts.relation.class_.ClassRelations;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("COLLECTION")
public class CollectionReport {

    private final JigType jigType;
    private final ClassRelations classRelations;

    public CollectionReport(JigType jigType, ClassRelations classRelations) {
        this.jigType = jigType;
        this.classRelations = classRelations;
    }

    @ReportItemFor(ReportItem.パッケージ名)
    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス別名)
    @ReportItemFor(ReportItem.フィールドの型)
    @ReportItemFor(ReportItem.メソッド数)
    @ReportItemFor(ReportItem.メソッド一覧)
    public JigType jigType() {
        return jigType;
    }

    @ReportItemFor(ReportItem.使用箇所数)
    @ReportItemFor(ReportItem.使用箇所)
    public TypeIdentifiers userTypeIdentifiers() {
        return classRelations.collectTypeIdentifierWhichRelationTo(jigType().identifier());
    }
}
