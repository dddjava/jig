package org.dddjava.jig.presentation.view.report.business_rule;

import org.dddjava.jig.domain.model.declaration.type.Type;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRule;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("ALL")
public class BusinessRuleReport {

    BusinessRule businessRule;

    public BusinessRuleReport(BusinessRule businessRule) {
        this.businessRule = businessRule;
    }

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス別名)
    public Type typeIdentifier() {
        return businessRule.type();
    }
}
