package org.dddjava.jig.infrastructure.view.report.business_rule;

import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRulePackage;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;
import org.dddjava.jig.infrastructure.view.report.ReportItem;
import org.dddjava.jig.infrastructure.view.report.ReportItemFor;
import org.dddjava.jig.infrastructure.view.report.ReportTitle;

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
