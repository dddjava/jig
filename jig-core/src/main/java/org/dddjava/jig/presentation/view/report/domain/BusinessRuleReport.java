package org.dddjava.jig.presentation.view.report.domain;

import org.dddjava.jig.domain.model.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.Type;
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
    @ReportItemFor(ReportItem.クラス和名)
    public Type typeIdentifier() {
        return businessRule.type();
    }
}
