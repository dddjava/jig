package org.dddjava.jig.presentation.view.poi.report;

public interface ModelReporter<REPORT, MODEL> {

    REPORT report(MODEL model);
}
