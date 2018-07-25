package org.dddjava.jig.presentation.view.poi.report;

import org.dddjava.jig.domain.model.report.ReportItemFor;
import org.dddjava.jig.presentation.view.poi.report.handler.Handlers;

import java.lang.reflect.InvocationTargetException;

class AngleReportAdapterMethodInvoker implements Comparable<AngleReportAdapterMethodInvoker>, ItemConverter {
    private final Object adapter;
    ReportItemFor reportItemFor;
    java.lang.reflect.Method itemMethod;
    ConvertContext convertContext;

    public AngleReportAdapterMethodInvoker(Object adapter, ReportItemFor reportItemFor, java.lang.reflect.Method itemMethod, ConvertContext convertContext) {
        this.adapter = adapter;
        this.reportItemFor = reportItemFor;
        this.itemMethod = itemMethod;
        this.convertContext = convertContext;
    }

    @Override
    public int compareTo(AngleReportAdapterMethodInvoker o) {
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
        Handlers handlers = new Handlers(convertContext);
        try {
            Object item = itemMethod.invoke(adapter, angle);

            return handlers.handle(reportItemFor.value(), item);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("実装ミス", e);
        }
    }
}