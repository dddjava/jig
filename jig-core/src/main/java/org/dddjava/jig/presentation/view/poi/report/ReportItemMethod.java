package org.dddjava.jig.presentation.view.poi.report;

import org.dddjava.jig.presentation.view.ResourceBundleJigDocumentContext;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;

import java.lang.reflect.InvocationTargetException;

public class ReportItemMethod implements Comparable<ReportItemMethod> {
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

    Object invoke(Object report) {
        try {
            return method.invoke(report);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("実装ミス", e);
        }
    }

    public ReportItem value() {
        return reportItemFor.value();
    }
}
