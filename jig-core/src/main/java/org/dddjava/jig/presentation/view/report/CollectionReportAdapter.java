package org.dddjava.jig.presentation.view.report;

import org.dddjava.jig.domain.model.collections.CollectionAngle;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

@ReportTitle("COLLECTION")
public class CollectionReportAdapter {

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス和名)
    public TypeIdentifier typeIdentifier(CollectionAngle angle) {
        return angle.typeIdentifier();
    }

    @ReportItemFor(ReportItem.使用箇所数)
    @ReportItemFor(ReportItem.使用箇所)
    public TypeIdentifiers userTypeIdentifiers(CollectionAngle angle) {
        return angle.userTypeIdentifiers();
    }

    @ReportItemFor(ReportItem.メソッド数)
    @ReportItemFor(ReportItem.メソッド一覧)
    public MethodDeclarations methods(CollectionAngle angle) {
        return angle.methods();
    }
}
