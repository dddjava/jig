package org.dddjava.jig.presentation.view.poi.report;

import org.dddjava.jig.domain.basic.ReportContext;
import org.dddjava.jig.domain.basic.ReportItemFor;

import java.lang.reflect.InvocationTargetException;

class ReportItemMethod implements Comparable<ReportItemMethod>, ItemConverter {
    ReportItemFor reportItemFor;
    java.lang.reflect.Method itemMethod;
    ConvertContext convertContext;

    public ReportItemMethod(ReportItemFor reportItemFor, java.lang.reflect.Method itemMethod, ConvertContext convertContext) {
        this.reportItemFor = reportItemFor;
        this.itemMethod = itemMethod;
        this.convertContext = convertContext;
    }

    @Override
    public int compareTo(ReportItemMethod o) {
        int compare = Integer.compare(this.reportItemFor.order(), o.reportItemFor.order());
        if (compare != 0) {
            return compare;
        }
        return reportItemFor.value().compareTo(o.reportItemFor.value());
    }

    @Override
    public String name() {
        String label = reportItemFor.label();
        return label.isEmpty() ? reportItemFor.value().name() : label;
    }

    @Override
    public String convert(Object angle) {
        try {
            Object item = itemMethod.invoke(angle);
            ReportContext reportContext = new ReportContext(item, convertContext);
            return reportItemFor.value().convert(reportContext);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("実装ミス");
        }
    }
}