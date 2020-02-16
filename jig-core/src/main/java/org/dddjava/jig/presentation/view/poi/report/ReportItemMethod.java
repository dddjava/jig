package org.dddjava.jig.presentation.view.poi.report;

import org.dddjava.jig.presentation.view.ResourceBundleJigDocumentContext;
import org.dddjava.jig.presentation.view.report.ReportItemFor;

class ReportItemMethod implements Comparable<ReportItemMethod> {
    java.lang.reflect.Method method;
    ReportItemFor reportItemFor;

    ReportItemMethod(java.lang.reflect.Method method, ReportItemFor reportItemFor) {
        this.method = method;
        this.reportItemFor = reportItemFor;
    }

    String label() {
        String label = reportItemFor.label();
        return label.isEmpty() ? name() : label;
    }

    private String name() {
        ResourceBundleJigDocumentContext context = ResourceBundleJigDocumentContext.getInstance();
        return context.reportLabel(reportItemFor.value());
    }

    @Override
    public int compareTo(ReportItemMethod o) {
        int compare = Integer.compare(this.reportItemFor.order(), o.reportItemFor.order());
        if (compare != 0) {
            return compare;
        }
        return reportItemFor.value().compareTo(o.reportItemFor.value());
    }
}
