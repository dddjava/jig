package org.dddjava.jig.presentation.view.report.business_rule;

import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifier;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

@ReportTitle("PACKAGE")
public class PackageReport {

    PackageIdentifier packageIdentifier;

    public PackageReport(PackageIdentifier businessRule) {
        this.packageIdentifier = businessRule;
    }

    @ReportItemFor(ReportItem.パッケージ名)
    @ReportItemFor(ReportItem.パッケージ別名)
    public PackageIdentifier packageIdentifier() {
        return packageIdentifier;
    }
}
