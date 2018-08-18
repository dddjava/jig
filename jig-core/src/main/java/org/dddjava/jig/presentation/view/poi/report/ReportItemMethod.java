package org.dddjava.jig.presentation.view.poi.report;

import org.dddjava.jig.presentation.view.report.ReportItemFor;

class ReportItemMethod {
    java.lang.reflect.Method method;
    ReportItemFor annotation;

    ReportItemMethod(java.lang.reflect.Method method, ReportItemFor annotation) {
        this.method = method;
        this.annotation = annotation;
    }
}
