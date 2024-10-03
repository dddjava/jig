package org.dddjava.jig.infrastructure.view.poi.report;

import org.dddjava.jig.infrastructure.view.report.ReportItem;
import org.dddjava.jig.infrastructure.view.report.ReportItemFor;

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
        return reportItemFor.value().localizedText();
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
