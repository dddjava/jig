package org.dddjava.jig.presentation.view.poi.report;

public interface ReporterProvider<REPORT, MODEL> {

    REPORT report(MODEL model);
}
