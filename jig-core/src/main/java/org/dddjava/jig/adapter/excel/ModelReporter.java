package org.dddjava.jig.adapter.excel;

public interface ModelReporter<REPORT, MODEL> {

    REPORT report(MODEL model);
}
