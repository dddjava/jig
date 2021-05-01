package org.dddjava.jig.presentation.view.report.business_rule;

import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRulePackage;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.parts.package_.PackageIdentifier;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("PACKAGE")
public class PackageReport {

    BusinessRulePackage businessRulePackage;

    public PackageReport(BusinessRulePackage businessRulePackage) {
        this.businessRulePackage = businessRulePackage;
    }

    @ReportItemFor(ReportItem.パッケージ名)
    @ReportItemFor(ReportItem.パッケージ別名)
    public PackageIdentifier packageIdentifier() {
        return businessRulePackage.packageIdentifier();
    }

    @ReportItemFor(ReportItem.クラス数)
    public BusinessRules businessRules() {
        return businessRulePackage.businessRules();
    }
}
