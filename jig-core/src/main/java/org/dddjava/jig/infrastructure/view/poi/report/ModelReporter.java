package org.dddjava.jig.infrastructure.view.poi.report;

public interface ModelReporter<REPORT, MODEL> {

    REPORT report(MODEL model);
}
