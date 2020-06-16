package org.dddjava.jig.presentation.view.report.business_rule;

import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifiers;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("ALL")
public class BusinessRuleReport {

    BusinessRule businessRule;

    public BusinessRuleReport(BusinessRule businessRule) {
        this.businessRule = businessRule;
    }

    @ReportItemFor(ReportItem.パッケージ名)
    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス別名)
    public TypeDeclaration typeIdentifier() {
        return businessRule.type();
    }

    @ReportItemFor(ReportItem.使用箇所数)
    public TypeIdentifiers userTypeIdentifiers() {
        return businessRule.userTypes();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "public?")
    public boolean isPublic() {
        return businessRule.visibility().isPublic();
    }
}
